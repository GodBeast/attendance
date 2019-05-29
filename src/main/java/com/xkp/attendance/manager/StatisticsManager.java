package com.xkp.attendance.manager;

import com.xkp.attendance.VO.AttendanceDataExcel;
import com.xkp.attendance.VO.StatisticsVO;
import com.xkp.attendance.entity.ClockIn;
import com.xkp.attendance.entity.Employee;
import com.xkp.attendance.mapper.EmployeeMapper;
import com.xkp.attendance.service.ClockInService;
import com.xkp.attendance.utils.DateUtil;
import org.apache.poi.hssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-24 09:35
 **/
@Component
public class StatisticsManager {

    @Autowired
    private ClockInService clockInService;

    @Autowired
    private EmployeeMapper employeeMapper;


    /**
     * 获取需要导出的数据
     *
     * @param map  获取所有员工当月的考勤信息
     * @param type 员工类型0:未知，1：管理员，2：办公室 ，3:工人
     * @return
     */
    public List<AttendanceDataExcel> getAttendanceDataExcel(Map<String, Map<Employee, StatisticsVO>> map, Integer type) {
        List<AttendanceDataExcel> list = new ArrayList<>();
        // 获取所有员工当月的考勤信息
        AttendanceDataExcel excel;
        StatisticsVO statisticsVO;
        Map<Employee, StatisticsVO> statisticsVOMap;
        for (Map.Entry<String, Map<Employee, StatisticsVO>> m : map.entrySet()) {
            statisticsVOMap = m.getValue();
            if (statisticsVOMap != null) {
                for (Map.Entry<Employee, StatisticsVO> statisticsVOEntry : statisticsVOMap.entrySet()) {
                    statisticsVO = statisticsVOEntry.getValue();
                    if (statisticsVO.getType() != null && statisticsVO.getType().equals(type)) {
                        excel = new AttendanceDataExcel();
                        excel.setUsername(statisticsVOEntry.getKey().getUsername());
                        excel.setUserCode(statisticsVOEntry.getKey().getUserCode().toString());
                        excel.setClockInDate(DateUtil.formatDate(statisticsVO.getClockInDate()));
                        excel.setClockInEndDate(DateUtil.formatDate(statisticsVO.getClockInEndDate()));
                        excel.setClockInStartDate(DateUtil.formatDate(statisticsVO.getClockInStartDate()));
                        // 考勤状态:0正常；1，缺卡，2：迟到 3:早退
                        excel.setClockInStatus(statisticsVO.getClockInStatus());
                        if (statisticsVO.getClockInStatus() == 0) {
                            excel.setClockInStatusName("正常");
                        }
                        if (statisticsVO.getClockInStatus() == 1) {
                            excel.setClockInStatusName("缺卡");
                        }
                        if (statisticsVO.getClockInStatus() == 2) {
                            excel.setClockInStatusName("迟到");
                        }
                        if (statisticsVO.getClockInStatus() == 3) {
                            excel.setClockInStatusName("早退");
                        }

                        excel.setOvertime(statisticsVO.getOvertime());
                        excel.setSubsidy(statisticsVO.getSubsidy());
                        excel.setType(statisticsVO.getType());
                        if (type == 1) {
                            excel.setTypeName("管理员");
                        }
                        if (type == 0) {
                            excel.setTypeName("未知");
                        }
                        if (type == 2) {
                            excel.setTypeName("办公室员工");
                        }
                        if (type == 3) {
                            excel.setTypeName("工人");
                        }

                        list.add(excel);
                    }
                }
            }

        }

        return list;
    }


    /**
     * 获取所有员工当月的考勤信息
     *
     * @param dateTime 月份
     * @return
     */
    public Map<String, Map<Employee, StatisticsVO>> checkClockInOfOneDay(Integer dateTime) {
        Date date = DateUtil.parseDate(dateTime.toString(), DateUtil.DEFAULT_PATTERN_MONTH);
        //获取一个月的每一天
        List<Date> dates = getEveryDayByMonth(Integer.parseInt(DateUtil.getYear(date)), Integer.parseInt(DateUtil.getMonth(date)));
        //获取所有员工
        List<Employee> employeeList = employeeMapper.selectAll();
        //时间区间内，员工每天的考勤状态
        Map<String, Map<Employee, StatisticsVO>> map = new HashMap<>(dates.size());

        //循环每一天
        dates.stream().forEach(x -> {
//            Date x = DateUtil.parseDate("2019-05-25 00:00:00", DateUtil.FULL_FORMAT_PATTERN);
            Map<String, Optional<ClockIn>> employStartTime = getEmployStartTime(x);

            //查出一天中，所有人的上班打卡数据
            //starMap.put(x, getEmployStartTime(x, 1));
            Map<Employee, StatisticsVO> empStatistics = new HashMap<>(employeeList.size());
            if (employStartTime.size() > 0) {
                for (Employee employee : employeeList) {

                    StatisticsVO statisticsVO = new StatisticsVO();
                    String empNo = employee.getUserCode().toString();
                    statisticsVO.setClockInDate(x)
                            .setUserCode(Integer.parseInt(empNo))
                            .setUsername(employee.getUsername())
                            .setType(employee.getType());
                    //赋值上班时间
                    if (employStartTime.containsKey(empNo) && employStartTime.get(empNo).isPresent()) {
                        statisticsVO.setClockInStartDate(employStartTime.get(empNo).get().getClockInDate());
                    } else {
                        statisticsVO.setClockInStartDate(null);
                    }
                    //赋值下班时间
                    statisticsVO.setClockInEndDate(getEmployEndTime(empNo,
                            employee.getType(),
                            statisticsVO.getClockInStartDate() == null ? x : statisticsVO.getClockInStartDate()));

                    /**
                     * 计算员工考勤状态，加班工时等
                     */
                    getClockInStatus(statisticsVO);
                    empStatistics.put(employee, statisticsVO);

                }

            }
            map.put(DateUtil.formatDate(x), empStatistics);
        });

        return map;
    }

    /**
     * 获取员工考勤状态和工时
     *
     * @param statisticsVO
     */
    private void getClockInStatus(StatisticsVO statisticsVO) {
        //白领的情况
        if (statisticsVO.getType() == 2) {
            /**
             * 如果上班时间或下班时间不为空，则计算工时
             */
            if (statisticsVO.getClockInStartDate() != null && statisticsVO.getClockInEndDate() != null) {
                Date whiteCollarFlexTime1 = getWhiteCollarFlexTime1(statisticsVO.getClockInDate());
                Date whiteCollarFlexTime2 = getWhiteCollarFlexTime2(statisticsVO.getClockInDate());
                /**
                 * 如果上班时间为8：30之前，则下班时间 - 8：30 计算工时
                 */
                if (statisticsVO.getClockInStartDate().before(whiteCollarFlexTime1)) {
                    BigDecimal hour = getWorkingHours(statisticsVO.getClockInStartDate(), getWhiteCollarFlexTime1(statisticsVO.getClockInDate()));

                    BigDecimal overTime = hour.subtract(BigDecimal.valueOf(9));
                    if (overTime.compareTo(BigDecimal.ZERO) < 0) {
                        statisticsVO.setClockInStatus(3);
                    } else {
                        statisticsVO.setClockInStatus(1);
                    }
                    statisticsVO.setOvertime(overTime);
                    /**
                     * 如果上班时间为8：30- 9：30 之间，则下班时间减去上班时间 计算工时
                     */
                } else if (statisticsVO.getClockInStartDate().after(whiteCollarFlexTime1) && statisticsVO.getClockInStartDate().before(whiteCollarFlexTime2)) {
                    BigDecimal hour = getWorkingHours(statisticsVO.getClockInStartDate(), statisticsVO.getClockInEndDate());

                    BigDecimal overTime = hour.subtract(BigDecimal.valueOf(9));
                    if (overTime.compareTo(BigDecimal.ZERO) < 0) {
                        statisticsVO.setClockInStatus(3);
                    } else {
                        statisticsVO.setClockInStatus(0);
                    }
                    statisticsVO.setOvertime(overTime);
                    /**
                     * 上班时间晚于9：30 则为迟到
                     */
                } else {
                    statisticsVO.setClockInStatus(2);
                    BigDecimal hour = getWorkingHours(statisticsVO.getClockInStartDate(), statisticsVO.getClockInEndDate());

                    BigDecimal overTime = hour.subtract(BigDecimal.valueOf(9));
                    statisticsVO.setOvertime(overTime);

                }
                /**
                 * 如果上班时间或下班时间为空，则为打卡异常，不计算当天工时；
                 */
            } else {
                statisticsVO.setClockInStatus(1);
            }
            /**
             * 工人工时计算
             */
        } else if (statisticsVO.getType() == 3) {
            if (statisticsVO.getClockInStartDate() == null || statisticsVO.getClockInEndDate() == null) {
                statisticsVO.setClockInStatus(1);
                return;
            }
            //0点班
            Date date1 = getWorkingTime1(statisticsVO.getClockInDate());
            //8点班
            Date date2 = getWorkingTime2(statisticsVO.getClockInDate());
            //16点班
            Date date3 = getWorkingTime3(statisticsVO.getClockInDate());
            //20点班
            Date date4 = getWorkingTime4(statisticsVO.getClockInDate());
            /**
             * 0点班，8点下班，考勤正常
             */
            if (statisticsVO.getClockInStartDate().after(getOneHourBefore(date1)) && statisticsVO.getClockInStartDate().before(date1)) {
                if (statisticsVO.getClockInStartDate().after(date2) && statisticsVO.getClockInStartDate().before(getOneHourAfter(date2))) {
                    statisticsVO.setClockInStatus(0)
                            .setSubsidy(true);
                }
                /**
                 * 8点班，判断4点正常下班还是八点下班 （区分两班作息还是三班作息），并算上加班工时
                 */
            } else if (statisticsVO.getClockInStartDate().after(getOneHourBefore(date2)) && statisticsVO.getClockInStartDate().before(date2)) {
                if (statisticsVO.getClockInEndDate().after(date3) && statisticsVO.getClockInEndDate().before(getOneHourAfter(date3))) {
                    statisticsVO.setClockInStatus(0);
                } else if (statisticsVO.getClockInEndDate().after(date4) && statisticsVO.getClockInEndDate().before(getOneHourAfter(date4))) {
                    statisticsVO.setClockInStatus(0)
                            .setOvertime(BigDecimal.valueOf(3.5));
                }
                /**
                 * 16点上班，0点正常下班则考勤正常
                 */
            } else if (statisticsVO.getClockInStartDate().after(getOneHourBefore(date3)) && statisticsVO.getClockInStartDate().before(date3)) {
                if (statisticsVO.getClockInStartDate().after(getHourAfter(date3, 8)) && statisticsVO.getClockInStartDate().before(getHourAfter(date3, 9))) {
                    statisticsVO.setClockInStatus(0)
                            .setSubsidy(true);
                }
                /**
                 * 20点上班为两班倒工作制，次日八点之后下班则为正常考勤
                 */
            } else if (statisticsVO.getClockInStartDate().after(getOneHourBefore(date4)) && statisticsVO.getClockInStartDate().before(date4)) {
                if (statisticsVO.getClockInEndDate().after(getHourAfter(date4, 12)) && statisticsVO.getClockInEndDate().before(getHourAfter(date4, 13))) {
                    statisticsVO.setOvertime(BigDecimal.valueOf(4))
                            .setClockInStatus(0)
                            .setSubsidy(true);
                }
                /**
                 * 其他情况均算考勤异常，交由人工处理
                 */
            } else {
                statisticsVO.setClockInStatus(1);
            }
        }

    }

    /**
     * 获取每人每天最早的打卡记录
     *
     * @param date 日期
     * @return
     */
    private Map<String, Optional<ClockIn>> getEmployStartTime(Date date) {
        List<ClockIn> startTimes = clockInService.findByDateAndType(date, 1);
        return startTimes.stream().collect(
                Collectors.groupingBy(ClockIn::getEmpNo, Collectors.minBy(Comparator.comparing(ClockIn::getClockInDate))));

    }

    /**
     * 计算员工下班打卡时间
     *
     * @param employNo
     * @param employType 员工类型
     * @param clockDate  日期
     */
    private Date getEmployEndTime(String employNo, Integer employType, Date clockDate) {
        //区分办公室员工和工人计算方式
        if (employType == 2) {
            List<ClockIn> clockIns = clockInService.findByUserAndType(employNo, clockDate, 2);
            Optional<ClockIn> clockIn = clockIns.stream().max(Comparator.comparing(ClockIn::getClockInDate));
            if (clockIn.isPresent()) {
                return clockIn.get().getClockInDate();
            } else {
                return null;
            }
            //工人：获取当天最晚下班时间，如果当天没有下班打卡，则获取第二天上班打卡之前的最晚下班打卡时间,第二天如果没有上班打卡记录，则直接取最晚下班打卡时间
        } else if (employType == 3) {
            List<ClockIn> clockIns = clockInService.findByUserAndType(employNo, clockDate, 2);
            if (CollectionUtils.isEmpty(clockIns)) {
                List<ClockIn> nextDayClockInEndTime = clockInService.findByUserAndType(employNo, DateUtil.getDateStart(DateUtil.addDays(clockDate, 1)), 2);
                List<ClockIn> nextDayClockInStartTime = clockInService.findByUserAndType(employNo, DateUtil.getDateStart(DateUtil.addDays(clockDate, 1)), 1);
                Optional<ClockIn> startTime = nextDayClockInStartTime.stream().min(Comparator.comparing(ClockIn::getClockInDate));
                if (startTime.isPresent()) {
                    Optional<ClockIn> endTime = nextDayClockInEndTime.stream().filter(end -> end.getClockInDate().before(startTime.get().getClockInDate()))
                            .max(Comparator.comparing(ClockIn::getClockInDate));
                    if (endTime.isPresent()) {
                        return endTime.get().getClockInDate();
                    } else {
                        return null;
                    }
                } else {
                    Optional<ClockIn> endTime = nextDayClockInEndTime.stream().max(Comparator.comparing(ClockIn::getClockInDate));
                    if (endTime.isPresent()) {
                        return endTime.get().getClockInDate();
                    } else {
                        return null;
                    }
                }
            } else {
                Optional<ClockIn> clockIn = clockIns.stream().max(Comparator.comparing(ClockIn::getClockInDate));
                return clockIn.get().getClockInDate();
            }
        }
        return null;
    }

    private List<Date> getEveryDayByMonth(Integer year, Integer month) {
        return DateUtil.dateSplit(getStartDateOfMonth(year, month), getEndDateOfMonth(year, month));
    }

    private Date getStartDateOfMonth(Integer year, Integer month) {
        return DateUtil.getDate(year, month - 1, 26);
    }

    private Date getEndDateOfMonth(Integer year, Integer month) {
        return DateUtil.getDate(year, month, 25);
    }

    //白领弹性最早上班时间
    private Date getWhiteCollarFlexTime1(Date date) {
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 08:30:00");
        return DateUtil.parseDate(s1);
    }

    //白领弹性最晚上班时间
    private Date getWhiteCollarFlexTime2(Date date) {
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 09:30:00");
        return DateUtil.parseDate(s1);
    }

    //白领弹性最早下班时间
    private Date getWhiteCollarFlexTime3(Date date) {
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 17:30:00");
        return DateUtil.parseDate(s1);
    }

    //白领弹性最晚下班时间
    private Date getWhiteCollarFlexTime4(Date date) {
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 18:30:00");
        return DateUtil.parseDate(s1);
    }

    //工人上班时间点
    private Date getWorkingTime1(Date date) {
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 00:00:00");
        return DateUtil.parseDate(s1, DateUtil.FULL_FORMAT_PATTERN);
    }

    //工人上班时间点
    private Date getWorkingTime2(Date date) {
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 08:00:00");
        return DateUtil.parseDate(s1, DateUtil.FULL_FORMAT_PATTERN);
    }

    //工人上班时间点
    private Date getWorkingTime3(Date date) {
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 16:00:00");
        return DateUtil.parseDate(s1, DateUtil.FULL_FORMAT_PATTERN);
    }

    //工人上班时间点
    private Date getWorkingTime4(Date date) {
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 20:00:00");
        return DateUtil.parseDate(s1, DateUtil.FULL_FORMAT_PATTERN);
    }

    //获取前一个小时时间
    private Date getOneHourBefore(Date date) {
        return DateUtil.addMins(date, -60);
    }

    //获取后一个小时时间
    private Date getOneHourAfter(Date date) {
        return DateUtil.addMins(date, 60);
    }

    //获取后N个小时
    private Date getHourAfter(Date date, Integer hour) {
        return DateUtil.addMins(date, 60 * hour);
    }

    //获取上班工时
    private BigDecimal getWorkingHours(Date startTime, Date endTime) {
        return BigDecimal.valueOf(startTime.getTime() - endTime.getTime()).divide(BigDecimal.valueOf(1000 * 60 * 60)).setScale(1);
    }

}

package com.xkp.attendance.manager;

import com.xkp.attendance.VO.StatisticsVO;
import com.xkp.attendance.entity.ClockIn;
import com.xkp.attendance.entity.Employee;
import com.xkp.attendance.mapper.EmployeeMapper;
import com.xkp.attendance.service.ClockInService;
import com.xkp.attendance.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
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

    public List<Map<String, Object>> checkClockInOfOneDay(){
        List<Date> dates = getEveryDayByMonth(2019, 5);
        Map<Date, Map<String, Optional<ClockIn>>> starMap = new HashMap<>(dates.size());

        Map<Date, Map<String, Optional<ClockIn>>> endMap = new HashMap<>(dates.size());

        List<Employee> employeeList = employeeMapper.selectAll();
        //循环每一天
        dates.stream().forEach(x -> {
            Map<String, Optional<ClockIn>> employStartTime = getEmployStartTime(x);
            //查出一天中，所有人的上班打卡数据
            //starMap.put(x, getEmployStartTime(x, 1));

            for (Employee employee:employeeList) {
                StatisticsVO statisticsVO = new StatisticsVO();
                String empNo = employee.getUserCode().toString();
                statisticsVO.setClockInDate(x)
                .setUserCode(Integer.parseInt(empNo))
                .setUsername(employee.getUsername());
                //赋值上班时间
                if(employStartTime.get(empNo).isPresent()){
                    statisticsVO.setClockInStartDate(employStartTime.get(empNo).get().getClockInDate());
                }else{
                    statisticsVO.setClockInStartDate(null);
                }
                //赋值下班时间
                statisticsVO.setClockInEndDate(getEmployEndTime(empNo,
                        employee.getType(),
                        statisticsVO.getClockInStartDate() == null?x:statisticsVO.getClockInStartDate()));

            }

        });
        return null;
    }

    /**
     * 获取员工考勤状态和工时
     * @param statisticsVO
     */
    private void getClockInStatus(StatisticsVO statisticsVO){
        //白领的情况
        if(statisticsVO.getType() == 2){
            if(statisticsVO.getClockInStartDate() != null && statisticsVO.getClockInEndDate() != null){
                Date whiteCollarFlexTime1 = getWhiteCollarFlexTime1(statisticsVO.getClockInDate());
                Date whiteCollarFlexTime2 = getWhiteCollarFlexTime2(statisticsVO.getClockInDate());
                //
                if(statisticsVO.getClockInStartDate().before(whiteCollarFlexTime1)){
                    BigDecimal hour = getWorkingHours(statisticsVO.getClockInStartDate(), getWhiteCollarFlexTime1(statisticsVO.getClockInDate()));

                    BigDecimal overTime = hour.subtract(BigDecimal.valueOf(9));
                    if(overTime.compareTo(BigDecimal.ZERO) < 0){
                        statisticsVO.setClockInStatus(3);
                    }else{
                        statisticsVO.setClockInStatus(1);
                    }
                    statisticsVO.setOvertime(overTime);

                }else if(statisticsVO.getClockInStartDate().after(whiteCollarFlexTime1) && statisticsVO.getClockInStartDate().before(whiteCollarFlexTime2)){
                    BigDecimal hour = getWorkingHours(statisticsVO.getClockInStartDate(), statisticsVO.getClockInEndDate());

                    BigDecimal overTime = hour.subtract(BigDecimal.valueOf(9));
                    if(overTime.compareTo(BigDecimal.ZERO) < 0){
                        statisticsVO.setClockInStatus(3);
                    }else{
                        statisticsVO.setClockInStatus(0);
                    }
                    statisticsVO.setOvertime(overTime);

                }else{
                    statisticsVO.setClockInStatus(2);
                    BigDecimal hour = getWorkingHours(statisticsVO.getClockInStartDate(), statisticsVO.getClockInEndDate());

                    BigDecimal overTime = hour.subtract(BigDecimal.valueOf(9));
                    statisticsVO.setOvertime(overTime);

                }
            }else{
                statisticsVO.setClockInStatus(1);
            }

        }else if(statisticsVO.getType() == 3){

        }

    }

    /**
     * 获取每人每天最早的打卡记录
     * @param date 日期
     * @return
     */
    private Map<String, Optional<ClockIn>> getEmployStartTime(Date date){
        List<ClockIn> startTimes = clockInService.findByDateAndType(date, 1);
        return startTimes.stream().collect(
                Collectors.groupingBy(ClockIn::getEmpNo, Collectors.minBy(Comparator.comparing(ClockIn::getClockInDate))));

    }

    /**
     * 计算员工下班打卡时间
     * @param employNo
     * @param employType 员工类型
     * @param clockDate 日期
     */
    private Date getEmployEndTime(String employNo, Integer employType, Date clockDate){
        //区分办公室员工和工人计算方式
        if(employType == 2){
            List<ClockIn> clockIns = clockInService.findByUserAndType(employNo, clockDate, 2);
            Optional<ClockIn> clockIn = clockIns.stream().max(Comparator.comparing(ClockIn::getClockInDate));
            if(clockIn.isPresent()){
                return clockIn.get().getClockInDate();
            }else{
                return null;
            }
            //工人：获取当天最晚下班时间，如果当天没有下班打卡，则获取第二天上班打卡之前的最晚下班打卡时间,第二天如果没有上班打卡记录，则直接取最晚下班打卡时间
        }else if(employType == 3){
            List<ClockIn> clockIns = clockInService.findByUserAndType(employNo, clockDate, 2);
            if(CollectionUtils.isEmpty(clockIns)){
                List<ClockIn> nextDayClockInEndTime = clockInService.findByUserAndType(employNo, DateUtil.getDateStart(DateUtil.addDays(clockDate, 1)), 2);
                List<ClockIn> nextDayClockInStartTime = clockInService.findByUserAndType(employNo, DateUtil.getDateStart(DateUtil.addDays(clockDate, 1)), 1);
                Optional<ClockIn> startTime = nextDayClockInStartTime.stream().min(Comparator.comparing(ClockIn::getClockInDate));
                if(startTime.isPresent()){
                    Optional<ClockIn> endTime = nextDayClockInEndTime.stream().filter(end -> end.getClockInDate().before(startTime.get().getClockInDate()))
                            .max(Comparator.comparing(ClockIn::getClockInDate));
                    if(endTime.isPresent()){
                        return endTime.get().getClockInDate();
                    }else{
                        return null;
                    }
                }else{
                    Optional<ClockIn> endTime =  nextDayClockInEndTime.stream().max(Comparator.comparing(ClockIn::getClockInDate));
                    if(endTime.isPresent()){
                        return endTime.get().getClockInDate();
                    }else{
                        return null;
                    }
                }
            }else{
                Optional<ClockIn> clockIn = clockIns.stream().max(Comparator.comparing(ClockIn::getClockInDate));
                return clockIn.get().getClockInDate();
            }
        }
        return null;
    }

    private List<Date> getEveryDayByMonth(Integer year, Integer month){
        return DateUtil.dateSplit(getStartDateOfMonth(year, month), getEndDateOfMonth(year, month));
    }

    private Date getStartDateOfMonth(Integer year,Integer month){
        return DateUtil.getDate(year, month-1, 26);
    }

    private Date getEndDateOfMonth(Integer year,Integer month){
        return DateUtil.getDate(year, month, 25);
    }

    //白领弹性最早上班时间
    private Date getWhiteCollarFlexTime1(Date date){
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 08:30:00");
        return DateUtil.parseDate(s1);
    }

    //白领弹性最晚上班时间
    private Date getWhiteCollarFlexTime2(Date date){
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 09:30:00");
        return DateUtil.parseDate(s1);
    }

    //白领弹性最早下班时间
    private Date getWhiteCollarFlexTime3(Date date){
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 17:30:00");
        return DateUtil.parseDate(s1);
    }

    //白领弹性最晚下班时间
    private Date getWhiteCollarFlexTime4(Date date){
        String s1 = DateUtil.formatDate(date, "yyyy-MM-dd 18:30:00");
        return DateUtil.parseDate(s1);
    }

    //获取上班工时
    private BigDecimal getWorkingHours(Date startTime, Date endTime){
        return BigDecimal.valueOf(startTime.getTime() - endTime.getTime()).divide(BigDecimal.valueOf(1000*60*60)).setScale(1);
    }

}

package com.xkp.attendance.controller;

import com.xkp.attendance.VO.AttendanceDataExcel;
import com.xkp.attendance.VO.Column;
import com.xkp.attendance.VO.StatisticsVO;
import com.xkp.attendance.VO.TitleEntity;
import com.xkp.attendance.entity.Employee;
import com.xkp.attendance.manager.StatisticsManager;
import com.xkp.attendance.model.enums.ClockInStatusEnum;
import com.xkp.attendance.model.enums.HolidayEnum;
import com.xkp.attendance.utils.DateUtil;
import com.xkp.attendance.utils.excel.ExcelTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: ExportExcelController
 * @ProjectName attendance
 * @Description: TODO
 * @Author xiaoli
 * @Date 2019-05-29 14:18
 */
@RestController
public class ExportExcelController {

    private static final Logger logger = LoggerFactory.getLogger(ExportExcelController.class);


    @Autowired
    StatisticsManager statisticsManager;

    @Value("${multipart.location-temp-url}")
    private String tmpLocation;

//    /**
//     * 按照月份导出考勤记录 格式：201902
//     *
//     * @param request
//     * @param response
//     * @param date
//     * @param type     员工类型0:未知，1：管理员，2：办公室 ，3:工人
//     */
//    @GetMapping(value = "/exportExcel/{date}/{type}")
//    @ResponseBody
//    public void exportFailureBillExcel(HttpServletRequest request, HttpServletResponse response,
//                                       @PathVariable(value = "date") Integer date,
//                                       @PathVariable(value = "type") Integer type) {
//        logger.debug("开始导出月份[{}]的考勤记录", date);
//        String fileName = date + "考勤记录.xlsx";
//        try {
//            // 获取需要导出的数据
//            List<AttendanceDataExcel> list = statisticsManager.getAttendanceDataExcel(date, type);
//            new ExportExcel(date + "考勤记录", AttendanceDataExcel.class, 2).setDataList(list).write(response, request, fileName).dispose();
//
//
//        } catch (IOException e) {
//            logger.error("导出月份[{}]的考勤记录异常", date, e);
//        }
//    }

    /**
     * 按照月份导出考勤记录 格式：201902
     *
     * @param date
     * @param type 员工类型0:未知，1：管理员，2：办公室 ，3:工人
     */
    @GetMapping(value = "/exportExcel/{date}/{type}")
    @ResponseBody
    public void exportFailureBillExcel1(@PathVariable(value = "date") Integer date,
                                        @PathVariable(value = "type") Integer type,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws Exception {
        logger.debug("开始导出月份[{}]的考勤记录", date);
        String title = date.toString() + type.toString() + "考勤记录";

        // 获取所有员工当月的考勤信息
        Map<String, Map<Employee, StatisticsVO>> stringMapMap = statisticsManager.checkClockInOfOneDay(date, type);


        // 排序
        Map<String, Map<Employee, StatisticsVO>> listMap = new LinkedHashMap<>();
        stringMapMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> listMap.put(x.getKey(), x.getValue()));

        // 获取需要导出的数据
        List<AttendanceDataExcel> list = statisticsManager.getAttendanceDataExcel(listMap, type);
        // 获取动态表头
        List<TitleEntity> titleList = getTitleList(listMap, title, type);

        Map<String, List<AttendanceDataExcel>> userMap = list.stream().collect(Collectors.groupingBy(AttendanceDataExcel::getUserCode));

        //单级的 行内数据
        int i = 1;
        List<AttendanceDataExcel> oneUserList;
        List<AttendanceDataExcel> oneDayUserList;
        AttendanceDataExcel attendanceDataExcel;
        List<Map<String, String>> rowList = new ArrayList<>();
        for (Map.Entry<String, List<AttendanceDataExcel>> map : userMap.entrySet()) {

            // 总加班小时数
            BigDecimal allOverTime = BigDecimal.ZERO;
            // 平时加班
            BigDecimal OT_normanl = BigDecimal.ZERO;
            // 周末加班
            BigDecimal OT_weekend = BigDecimal.ZERO;
            // 节假日加班
            BigDecimal OT_holiday = BigDecimal.ZERO;
            // 每周平时加班
            Map<Integer, BigDecimal> OT_normanl_map = new HashMap<>();
            // 每周周末加班
            Map<Integer, BigDecimal> OT_weekend_map = new HashMap<>();
            // 每周节假日加班
            Map<Integer, BigDecimal> OT_holiday_map = new HashMap<>();
            // 月薪
            BigDecimal salary;
            // 平时加班费用
            BigDecimal normanl_pay;
            // 周末加班费用
            BigDecimal weekend_pay;
            // 节假日加班费用
            BigDecimal holiday_pay;
            // 应出勤
            int day = 0;
            // 实际出勤
            int acctually = 0;


            Map m = new HashMap<String, String>();
            oneUserList = map.getValue();
            m.put("no", i);
            m.put("userCode", oneUserList.get(0).getUserCode());
            m.put("username", oneUserList.get(0).getUsername());
            m.put("payLevel", oneUserList.get(0).getPayLevel());
            m.put("cnName", oneUserList.get(0).getCnName());
            m.put("costCenter", oneUserList.get(0).getCostCenter());
            BigDecimal basicWage = (oneUserList.get(0).getBasicWage() == null ? BigDecimal.ZERO : oneUserList.get(0).getBasicWage());
            m.put("BasicWage", basicWage);
            Map<String, List<AttendanceDataExcel>> dateMap = oneUserList.stream().collect(Collectors.groupingBy(AttendanceDataExcel::getClockInDate));

            Integer key = 1;
            for (Map.Entry<String, Map<Employee, StatisticsVO>> mm : listMap.entrySet()) {
                oneDayUserList = dateMap.get(mm.getKey());
                if (CollectionUtils.isEmpty(oneDayUserList)) {
                    m.put("clockInStatusName" + mm.getKey(), "");
                } else {
                    m.put("clockInStatusName" + mm.getKey(), oneDayUserList.get(0).getClockInStatusName());
                    attendanceDataExcel = oneDayUserList.get(0);
                    // 加班时长
                    BigDecimal overtime = attendanceDataExcel.getOvertime();

                    if (overtime != null && overtime.compareTo(BigDecimal.ZERO) == 1) {
                        allOverTime = allOverTime.add(overtime);

                        int dayForWeek = DateUtil.dayForWeek(mm.getKey());

                        // 0 工作日, 1 休息日, 2 节假日, -1 为判断出错
                        int n = DateUtil.holidayType(mm.getKey());
                        if (n == HolidayEnum.WORKDAY.getValue()) {
                            OT_normanl = OT_normanl.add(overtime);

                            BigDecimal bigDecimal = OT_normanl_map.get(key);
                            if (bigDecimal == null) {
                                bigDecimal = BigDecimal.ZERO;
                            }
                            OT_normanl_map.put(key, bigDecimal.add(overtime));

                        }
                        if (n == HolidayEnum.WEEKEND.getValue()) {
                            OT_weekend = OT_weekend.add(overtime);

                            BigDecimal bigDecimal = OT_weekend_map.get(key);
                            if (bigDecimal == null) {
                                bigDecimal = BigDecimal.ZERO;
                            }
                            OT_weekend_map.put(key, bigDecimal.add(overtime));
                        }
                        if (n == HolidayEnum.HOLIDAY.getValue()) {
                            OT_holiday = OT_holiday.add(overtime);

                            BigDecimal bigDecimal = OT_holiday_map.get(key);
                            if (bigDecimal == null) {
                                bigDecimal = BigDecimal.ZERO;
                            }
                            OT_holiday_map.put(key, bigDecimal.add(overtime));
                        }

                        if (dayForWeek == 7) {
                            key++;
                        }
                    }

                    // 应出勤
                    if (!ClockInStatusEnum.REST.getValue().equals(attendanceDataExcel.getClockInStatus())) {
                        day++;
                    }
                    // 实际出勤
                    if (ClockInStatusEnum.NORMAL.getValue().equals(attendanceDataExcel.getClockInStatus())
                            || ClockInStatusEnum.LATE.getValue().equals(attendanceDataExcel.getClockInStatus())
                            || ClockInStatusEnum.EARLY.getValue().equals(attendanceDataExcel.getClockInStatus())) {
                        acctually++;
                    }


                }
            }
            normanl_pay = overtimePay(basicWage, 0, OT_normanl);
            weekend_pay = overtimePay(basicWage, 1, OT_weekend);
            holiday_pay = overtimePay(basicWage, 2, OT_holiday);
            salary = basicWage.add(normanl_pay).add(weekend_pay).add(holiday_pay);
            m.put("normanl_pay", normanl_pay);
            m.put("weekend_pay", weekend_pay);
            m.put("holiday_pay", holiday_pay);
            m.put("salary", salary);
            m.put("allOverTime", allOverTime);
            m.put("OT_normanl", OT_normanl);
            m.put("OT_weekend", OT_weekend);
            m.put("OT_holiday", OT_holiday);
            m.put("day", day);
            m.put("acctually", acctually);

            // 获取每周总加班时间与加班费
            Map<Integer, Map<String, BigDecimal>> everyWeekOverTime = everyWeekOverTime(getWeekNums(listMap), basicWage, OT_normanl_map, OT_weekend_map, OT_holiday_map);
            for (Map.Entry<Integer, Map<String, BigDecimal>> mapEntry : everyWeekOverTime.entrySet()) {

                m.put("overTime" + mapEntry.getKey(), mapEntry.getValue().get("overTime") == null ? BigDecimal.ZERO : mapEntry.getValue().get("overTime"));
                m.put("overPay" + mapEntry.getKey(), mapEntry.getValue().get("overPay") == null ? BigDecimal.ZERO : mapEntry.getValue().get("overPay"));
            }

            rowList.add(m);
            i++;
        }

        ExcelTool excelTool = new ExcelTool(title, 20, 20);
        List<Column> titleData = excelTool.columnTransformer(titleList, "t_id", "t_pid", "t_content", "t_fielName", "0");
        excelTool.exportExcel(titleData, rowList, request, response, true, true);

        logger.debug("导出月份[{}]的考勤记录结束", date);
    }

    /**
     * 获取每周总加班时间与加班费
     *
     * @param key
     * @param basicWage      基本工资
     * @param OT_normanl_map 每周平时加班时间
     * @param OT_weekend_map 每周周末加班时间
     * @param OT_holiday_map 每周节假日加班时间
     * @return <第几周，<总加班时间，总加班费用>></>
     */
    private Map<Integer, Map<String, BigDecimal>> everyWeekOverTime(Integer key,
                                                                    BigDecimal basicWage,
                                                                    Map<Integer, BigDecimal> OT_normanl_map,
                                                                    Map<Integer, BigDecimal> OT_weekend_map,
                                                                    Map<Integer, BigDecimal> OT_holiday_map) {
        Map<Integer, Map<String, BigDecimal>> mapMap = new HashMap<>();
        for (int i = 1; i <= key; i++) {
            Map<String, BigDecimal> map = new HashMap<>();

            // 平时加班
            BigDecimal OT_normanl = (OT_normanl_map.get(i) == null ? BigDecimal.ZERO : OT_normanl_map.get(i));
            // 周末加班
            BigDecimal OT_weekend = (OT_weekend_map.get(i) == null ? BigDecimal.ZERO : OT_weekend_map.get(i));
            // 节假日加班
            BigDecimal OT_holiday = (OT_holiday_map.get(i) == null ? BigDecimal.ZERO : OT_holiday_map.get(i));

            BigDecimal overTime = OT_normanl.add(OT_weekend).add(OT_holiday);
            BigDecimal overTimePay = overtimePay(basicWage, 0, OT_normanl).
                    add(overtimePay(basicWage, 1, OT_weekend)).
                    add(overtimePay(basicWage, 2, OT_holiday));
            map.put("overPay", overTimePay);
            map.put("overTime", overTime);

            mapMap.put(i, map);
        }
        return mapMap;
    }

    /**
     * @param basicWage 基本工资
     * @param type      0 工作日, 1-周末, 2 节假日；-1 平常
     * @param overTime  加班时长
     * @return
     */
    private BigDecimal overtimePay(BigDecimal basicWage, int type, BigDecimal overTime) {
        if (basicWage == null || overTime == null) {
            return BigDecimal.ZERO;
        }
        // 每小时工资
        BigDecimal overtimePay = basicWage.divide(new BigDecimal("21.75"), 2, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal("8"), 2, BigDecimal.ROUND_HALF_UP);
        // 平常1.5倍，周末两倍，节假日3倍
        if (type == 1) {
            return overtimePay.multiply(new BigDecimal(2)).multiply(overTime).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        if (type == 2) {
            return overtimePay.multiply(new BigDecimal(3)).multiply(overTime).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        if (type == 0) {
            return overtimePay.multiply(new BigDecimal("1.5")).multiply(overTime).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return overtimePay.multiply(overTime).setScale(2, BigDecimal.ROUND_HALF_UP);

    }

    /**
     * 获取当月有几周
     *
     * @param listMap
     * @return
     * @throws Exception
     */
    private Integer getWeekNums(Map<String, Map<Employee, StatisticsVO>> listMap) throws Exception {
        Integer key = 1;
        for (Map.Entry<String, Map<Employee, StatisticsVO>> m : listMap.entrySet()) {
            int dayForWeek = DateUtil.dayForWeek(m.getKey());
            if (dayForWeek == 7) {
                key++;
            }
        }
        return key;

    }

    /**
     * 获取动态表头
     *
     * @param listMap
     * @param title
     * @param type    员工类型0:未知，1：管理员，2：办公室 ，3:工人
     * @return
     */
    private List<TitleEntity> getTitleList(Map<String, Map<Employee, StatisticsVO>> listMap, String title, Integer type) throws Exception {
        List<TitleEntity> titleList = new ArrayList<>();
        TitleEntity titleEntity0 = new TitleEntity("0", null, title, null);
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("1", "0", "编号", "");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("1_1", "1", "NO", "no");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("2", "0", "姓名", "");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("2_1", "2", "name", "username");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("21", "0", "中文名", "");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("21_1", "21", "cnName", "cnName");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("211", "0", "成本中心", "");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("211_1", "211", "costCenter", "costCenter");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("22", "0", "应出勤", "");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("22_1", "22", "Day", "day");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("221", "0", "实际出勤", "");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("221_1", "221", "Acctually", "acctually");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("222", "0", "薪资等级", "payLevel");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("223", "0", "基本工资", "BasicWage");
        titleList.add(titleEntity0);
        titleEntity0 = new TitleEntity("224", "0", "月薪", "salary");
        titleList.add(titleEntity0);

        if (type == 3) {
            titleEntity0 = new TitleEntity("225", "0", "平常加班工资", "normanl_pay");
            titleList.add(titleEntity0);
            titleEntity0 = new TitleEntity("226", "0", "周末加班工资", "weekend_pay");
            titleList.add(titleEntity0);
            titleEntity0 = new TitleEntity("227", "0", "节假日加班工资", "holiday_pay");
            titleList.add(titleEntity0);
            titleEntity0 = new TitleEntity("3", "0", "总加班小时数", "allOverTime");
            titleList.add(titleEntity0);
            titleEntity0 = new TitleEntity("4", "0", "平时加班", "");
            titleList.add(titleEntity0);
            titleEntity0 = new TitleEntity("4_1", "4", "OT_normanl", "OT_normanl");
            titleList.add(titleEntity0);
            titleEntity0 = new TitleEntity("5", "0", "周末加班", "");
            titleList.add(titleEntity0);
            titleEntity0 = new TitleEntity("5_1", "5", "OT_weekend", "OT_weekend");
            titleList.add(titleEntity0);
            titleEntity0 = new TitleEntity("6", "0", "节假日加班", "");
            titleList.add(titleEntity0);
            titleEntity0 = new TitleEntity("6_1", "6", "OT_holiday", "OT_holiday");
            titleList.add(titleEntity0);

            // 获取当月有几周
            Integer weekNums = getWeekNums(listMap);

            for (int i = 1; i <= weekNums; i++) {

                titleEntity0 = new TitleEntity("week" + i, "0", "第" + i + "周", "");
                titleList.add(titleEntity0);
                titleEntity0 = new TitleEntity("overTime" + i, "week" + i, "加班时长", "overTime" + i);
                titleList.add(titleEntity0);
                titleEntity0 = new TitleEntity("overPay" + i, "week" + i, "加班工资", "overPay" + i);
                titleList.add(titleEntity0);
            }
        }


        for (Map.Entry<String, Map<Employee, StatisticsVO>> m : listMap.entrySet()) {
            titleEntity0 = new TitleEntity(m.getKey(), "0", m.getKey(), "");
            titleList.add(titleEntity0);
            // 星期几
            String weekDay = DateUtil.getWeekday(m.getKey());
            titleEntity0 = new TitleEntity(m.getKey() + weekDay, m.getKey(), weekDay, "clockInStatusName" + m.getKey());
            titleList.add(titleEntity0);
        }

        return titleList;
    }


}

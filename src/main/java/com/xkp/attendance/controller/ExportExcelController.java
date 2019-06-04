package com.xkp.attendance.controller;

import com.xkp.attendance.VO.AttendanceDataExcel;
import com.xkp.attendance.VO.Column;
import com.xkp.attendance.VO.StatisticsVO;
import com.xkp.attendance.VO.TitleEntity;
import com.xkp.attendance.entity.Employee;
import com.xkp.attendance.manager.StatisticsManager;
import com.xkp.attendance.model.enums.ClockInStatusEnum;
import com.xkp.attendance.model.enums.HolidayEnum;
import com.xkp.attendance.utils.APIHelper;
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
                                        @PathVariable(value = "type") Integer type) throws Exception {
        logger.debug("开始导出月份[{}]的考勤记录", date);
        String title = date.toString() + type.toString() +"考勤记录";

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
        List<TitleEntity> titleList = getTitleList(listMap, title);

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
            // 应出勤
            int day = 0;
            // 实际出勤
            int acctually = 0;


            Map m = new HashMap<String, String>();
            oneUserList = map.getValue();
            m.put("no", i);
            m.put("username", oneUserList.get(0).getUsername());
            m.put("payLevel", "薪资等级");
            m.put("cnName", "中文名称");
            m.put("costCenter", "成本中心");
            m.put("BasicWage", "基本工资");
            m.put("salary", "月薪");
            Map<String, List<AttendanceDataExcel>> dateMap = oneUserList.stream().collect(Collectors.groupingBy(AttendanceDataExcel::getClockInDate));
            for (Map.Entry<String, Map<Employee, StatisticsVO>> mm : listMap.entrySet()) {
                oneDayUserList = dateMap.get(mm.getKey());
                if (CollectionUtils.isEmpty(oneDayUserList)) {
                    m.put("clockInStatusName" + mm.getKey(), "缺勤");
                } else {
                    m.put("clockInStatusName" + mm.getKey(), oneDayUserList.get(0).getClockInStatusName());
                    attendanceDataExcel = oneDayUserList.get(0);
                    BigDecimal overtime = attendanceDataExcel.getOvertime();
                    if (overtime != null && overtime.compareTo(BigDecimal.ZERO) == 1) {
                        allOverTime = allOverTime.add(overtime);

                        // 0 工作日, 1 休息日, 2 节假日, -1 为判断出错
                        int n = APIHelper.holidayType(DateUtil.parseDate(mm.getKey()));
                        if (n == HolidayEnum.WORKDAY.getValue()) {
                            OT_normanl = OT_normanl.add(overtime);
                        }
                        if (n == HolidayEnum.WEEKEND.getValue()) {
                            OT_weekend = OT_weekend.add(overtime);
                        }
                        if (n == HolidayEnum.HOLIDAY.getValue()) {
                            OT_holiday = OT_holiday.add(overtime);
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
            m.put("allOverTime", allOverTime);
            m.put("OT_normanl", OT_normanl);
            m.put("OT_weekend", OT_weekend);
            m.put("OT_holiday", OT_holiday);
            m.put("day", day);
            m.put("acctually", acctually);
            rowList.add(m);
            i++;
        }

        ExcelTool excelTool = new ExcelTool(title, 20, 20);
        List<Column> titleData = excelTool.columnTransformer(titleList, "t_id", "t_pid", "t_content", "t_fielName", "0");
        excelTool.exportExcel(titleData, rowList, tmpLocation + title + ".xls", true, true);
    }

    /**
     * 获取动态表头
     *
     * @param listMap
     * @param title
     * @return
     */
    private List<TitleEntity> getTitleList(Map<String, Map<Employee, StatisticsVO>> listMap, String title) {
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
        for (Map.Entry<String, Map<Employee, StatisticsVO>> m : listMap.entrySet()) {
            titleEntity0 = new TitleEntity(m.getKey(), "0", m.getKey(), "");
            titleList.add(titleEntity0);
            // 星期几
            String weekDay = DateUtil.getWeekday(m.getKey());
            titleEntity0 = new TitleEntity(m.getKey() + weekDay, m.getKey(), weekDay, "clockInStatusName" + m.getKey());
            titleList.add(titleEntity0);
        }
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

        return titleList;
    }


}

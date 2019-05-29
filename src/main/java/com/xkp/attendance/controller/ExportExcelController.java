package com.xkp.attendance.controller;

import com.xkp.attendance.VO.AttendanceDataExcel;
import com.xkp.attendance.VO.Column;
import com.xkp.attendance.VO.StatisticsVO;
import com.xkp.attendance.VO.TitleEntity;
import com.xkp.attendance.entity.Employee;
import com.xkp.attendance.manager.StatisticsManager;
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

    @GetMapping(value = "/exportExcel/{date}/{type}")
    @ResponseBody
    public void exportFailureBillExcel1(@PathVariable(value = "date") Integer date,
                                        @PathVariable(value = "type") Integer type) throws Exception {
        logger.debug("开始导出月份[{}]的考勤记录", date);
        String title = date + "考勤记录";

        // 获取所有员工当月的考勤信息
        Map<String, Map<Employee, StatisticsVO>> stringMapMap = statisticsManager.checkClockInOfOneDay(date);


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
        List<Map<String, String>> rowList = new ArrayList<>();
        for (Map.Entry<String, List<AttendanceDataExcel>> map : userMap.entrySet()) {
            Map m = new HashMap<String, String>();
            oneUserList = map.getValue();
            m.put("no", i);
            m.put("username", oneUserList.get(0).getUsername());
            Map<String, List<AttendanceDataExcel>> dateMap = oneUserList.stream().collect(Collectors.groupingBy(AttendanceDataExcel::getClockInDate));
            for (Map.Entry<String, Map<Employee, StatisticsVO>> mm : listMap.entrySet()) {
                oneDayUserList = dateMap.get(mm.getKey());
                if (CollectionUtils.isEmpty(oneDayUserList)) {
                    m.put("clockInStatusName" + mm.getKey(), "无");
                } else {
                    m.put("clockInStatusName" + mm.getKey(), "有有有");
                }
            }
            rowList.add(m);
            i++;
        }

        ExcelTool excelTool = new ExcelTool(title, 20, 20);
        List<Column> titleData = excelTool.columnTransformer(titleList, "t_id", "t_pid", "t_content", "t_fielName", "0");
        excelTool.exportExcel(titleData, rowList, "/Users/xiaoli/tmp/" + title + ".xls", true, true);
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
        TitleEntity titleEntity1 = new TitleEntity("1", "0", "编号", "");
        TitleEntity titleEntity11 = new TitleEntity("1_1", "1", "NO", "no");
        TitleEntity titleEntity2 = new TitleEntity("2", "0", "姓名", "");
        TitleEntity titleEntity22 = new TitleEntity("2_1", "2", "name", "username");
        titleList.add(titleEntity0);
        titleList.add(titleEntity1);
        titleList.add(titleEntity11);
        titleList.add(titleEntity2);
        titleList.add(titleEntity22);
        for (Map.Entry<String, Map<Employee, StatisticsVO>> m : listMap.entrySet()) {
            titleEntity0 = new TitleEntity(m.getKey(), "0", m.getKey(), "");
            titleList.add(titleEntity0);
            titleEntity0 = new TitleEntity(m.getKey() + "周几", m.getKey(), "周几", "clockInStatusName" + m.getKey());
            titleList.add(titleEntity0);
        }
        return titleList;
    }


}

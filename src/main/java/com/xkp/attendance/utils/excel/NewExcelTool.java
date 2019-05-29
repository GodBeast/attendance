package com.xkp.attendance.utils.excel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class NewExcelTool {

    private static final Logger logger = LoggerFactory.getLogger(NewExcelTool.class);



    /**
     * 处理 workbook 和 file
     * @param workbook
     * @throws IOException
     */
    private static void handlerOutputStream(FileOutputStream outputStream, HSSFWorkbook workbook) throws IOException {
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();

    }
    /**
     * 创建EXCEL压缩包
     *
     * @param fileName    excel名称
     * @param title       标题
     * @param headerNames 表头内容
     * @param values      内容
     * @param keys        内容对应的字段
     * @throws IOException
     */
    public static void createExcelToFile(String fileName,
                                         String title,
                                         List<String> headerNames,
                                         List<List<Map<String, String>>> values,
                                         List<String> keys,
                                         FileOutputStream outputStream) throws IOException {
        //创建工作簿
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("sheet" + fileName);

        // 样式列表
        Map<String, CellStyle> styles = ExportExcel.createStyles(wb);
        int rowNumParam = 0;
        // 设置标题
        if (StringUtils.isNotEmpty(title)) {
            Row row = sheet.createRow(rowNumParam);
            Cell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
            CellStyle styleTitle = styles.get("title");
            cell.setCellStyle(styleTitle);
            cell.setCellValue(title);
//            sheet.autoSizeColumn(0);  // 自动设置行宽
            //横向：合并第一行的第firstCol列到第lastCol列
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headerNames.size() - 1));
            rowNumParam++;
        }

        // 设置表头
        sheet.setDefaultColumnWidth(20);
        Row row0 = sheet.createRow(rowNumParam);
        if (CollectionUtils.isNotEmpty(headerNames)) {
            for (int ii = 0; ii < headerNames.size(); ii++) {
                Cell cell_1 = row0.createCell(ii, Cell.CELL_TYPE_STRING);
                cell_1.setCellStyle(styles.get("header"));
                cell_1.setCellValue(headerNames.get(ii));
            }
        }

        // 开始设置内容
        CellStyle style1 = wb.createCellStyle();
        style1.setAlignment(CellStyle.ALIGN_CENTER);
        style1.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        // 设置单元格边框及颜色
        style1.setBorderBottom((short) 1);
        style1.setBorderLeft((short) 1);
        style1.setBorderRight((short) 1);
        style1.setBorderTop((short) 1);
        if (CollectionUtils.isNotEmpty(values)) {
            for (int rowNum = rowNumParam; rowNum < values.size() + rowNumParam; rowNum++) {
                Row row = sheet.createRow(rowNum + 1);
                for (int k = 0; k < keys.size(); k++) {
                    Cell cell = row.createCell(k, Cell.CELL_TYPE_STRING);
                    cell.setCellStyle(getContentStyle(wb));
                    /**内容 **/
                    if (values.get(rowNum - rowNumParam).get(0).get(keys.get(k)) != null) {
                        cell.setCellValue(values.get(rowNum - rowNumParam).get(0).get(keys.get(k)));
                    } else {
                        cell.setCellValue("");
                    }
                    sheet.setDefaultColumnWidth(20);
                }
            }
        }

        try {
            handlerOutputStream(outputStream,wb);
        } catch (Exception e) {
            logger.error("It cause Error on WRITTING excel workbook: ",e);
        }

    }

    public static CellStyle getContentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        // 设置单元格字体
        Font headerFont = workbook.createFont(); // 字体
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
        headerFont.setFontName("仿宋");
        style.setWrapText(true);
        style.setFont(headerFont);
        // 设置单元格边框及颜色
        style.setBorderBottom((short) 1);
        style.setBorderLeft((short) 1);
        style.setBorderRight((short) 1);
        style.setBorderTop((short) 1);
        style.setWrapText(true);
        return style;
    }

    public static CellStyle getHeadStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        // 设置单元格字体
        Font headerFont = workbook.createFont(); // 字体
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
        headerFont.setFontName("仿宋");
        style.setWrapText(true);
        style.setFont(headerFont);
        // 设置单元格边框及颜色
        style.setBorderBottom((short) 1);
        style.setBorderLeft((short) 1);
        style.setBorderRight((short) 1);
        style.setBorderTop((short) 1);
        style.setWrapText(true);
        return style;
    }

    public static CellStyle getTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        // 设置单元格字体
        Font headerFont = workbook.createFont(); // 字体
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setFontName("仿宋");
        style.setFont(headerFont);
        style.setWrapText(true);

        // 设置单元格边框及颜色
        style.setBorderBottom((short) 1);
        style.setBorderLeft((short) 1);
        style.setBorderRight((short) 1);
        style.setBorderTop((short) 1);
        style.setWrapText(true);
        return style;
    }

}
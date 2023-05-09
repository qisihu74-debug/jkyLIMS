package com.lims.manage.erp.util;

import com.lims.manage.erp.vo.ExcelInsertVo;
import com.lims.manage.erp.vo.OriginalRecordDataVo;
import com.lims.manage.erp.vo.TemplateSampleVo;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Map;

/**
 * @Author: DLC
 * @Date: 2023/4/24 17:22
 */
public class ExcelReplaceUtil {

    /**
     * excel 原始记录模板标识符 按单元格进行替换
     *
     * @param sheet
     * @param map
     */
    public static void ExcelReplace(XSSFSheet sheet, Map<String, OriginalRecordDataVo> map) {
        OriginalRecordDataVo originalRecordDataVo = map.get("result");
        int lastRowNum = sheet.getLastRowNum(); //获取表格内容的最后一行的行数
        //rowBegin代表要开始读取的行号，下面这个循环的作用是读取每一行内容
        for (int x = 1; x <= lastRowNum; ++x) {
            XSSFRow row = sheet.getRow(x);//获取每一行
            int columnNum = row.getLastCellNum();//获取每一行的最后一列的列号，即总列数
            for (int y = 0; y < columnNum; ++y) {
                XSSFCell cell = row.getCell(y);//获取每个单元格
                if (cell != null) {
                    //设置单元格类型
                    cell.setCellType(cell.CELL_TYPE_STRING);
                    //获取单元格数据
                    String cellValue = cell.getStringCellValue();
                    if (!cellValue.equals("") && cellValue.equals("${result.recordNumber}")) {
                        // 记录编号：
                        cell.setCellValue(originalRecordDataVo.getRecordNumber());
                    }
                    if (!cellValue.equals("") && cellValue.equals("${result.projectName}")) {
                        // 工程名称：
                        cell.setCellValue(originalRecordDataVo.getProjectName());
                    }
                    if (!cellValue.equals("") && cellValue.equals("${result.projectLocation}")) {
                        // 工程部位/用途：
                        cell.setCellValue(originalRecordDataVo.getProjectLocation());
                    }
                    if (!cellValue.equals("") && cellValue.equals("${result.testDate}")) {
                        // 试验检测日期
                        cell.setCellValue(originalRecordDataVo.getTestDate());
                    }
                    if (!cellValue.equals("") && cellValue.equals("${result.testCondition}")) {
                        // 试验条件
                        cell.setCellValue(originalRecordDataVo.getTestDate());
                    }
                    if (!cellValue.equals("") && cellValue.equals("${result.testBasis}")) {
                        // 检测依据
                        cell.setCellValue(originalRecordDataVo.getTestBasis());
                    }
                    if (!cellValue.equals("") && cellValue.equals("${result.judgeBasis}")) {
                        // 判定依据
                        cell.setCellValue(originalRecordDataVo.getTestBasis());
                    }
                    if (!cellValue.equals("") && cellValue.equals("${result.equipment}")) {
                        // 主要仪器设备名称及编号
                        cell.setCellValue(originalRecordDataVo.getEquipment());
                    }
                    if (!cellValue.equals("") && cellValue.contains("样品名称：")) {
                        TemplateSampleVo sample = originalRecordDataVo.getSample();

                        // 样品名称：${result.sample.sampleName}
                        // 样品编号：${result.sample.sampleNumber}样品数量：${result.sample.sampleQuantity}
                        //样品描述：${result.sample.sampleDesc}来样时间：${result.sample.sampleTime}
                        String sampleName = cellValue.replace("${result.sample.sampleName}", sample.getSampleName());
                        String sampleNumber = sampleName.replace("${result.sample.sampleNumber}", sample.getSampleNumber());
                        String sampleQuantity = sampleNumber.replace("${result.sample.sampleQuantity}", sample.getSampleQuantity());
                        String sampleDesc = sampleQuantity.replace("${result.sample.sampleDesc}", sample.getSampleDesc());
                        String sampleTime = sampleDesc.replace("${result.sample.sampleTime}", sample.getSampleTime());
                        // 赋值
                        cell.setCellValue(sampleTime);
                    }
                }
            }
        }
    }

    /**
     * // 去除 e-iceblue中 sheet（Evaluation Warning）
     *
     * @param fileName  excel文件路径
     * @param sheetName 待删除的 sheetName
     * @throws IOException
     */
    public static void removeExcelSheetName(String fileName, String sheetName) throws IOException {

        InputStream fileStream = new FileInputStream(fileName);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        removeOtherSheets(sheetName, wb);
        fileStream.close();
        OutputStream f = new FileOutputStream("D:\\doc\\e-iceblue\\演示插入结果RemoveSheetName.xlsx");
        wb.write(f);
        f.close();
    }

    /**
     * 颠倒循环次序
     *
     * @param sheetName
     * @param book
     */
    public static void removeOtherSheets(String sheetName, XSSFWorkbook book) {
        for (int i = book.getNumberOfSheets() - 1; i >= 0; i--) {
            XSSFSheet tmpSheet = book.getSheetAt(i);
            if (tmpSheet.getSheetName().equals(sheetName)) {
                book.removeSheetAt(i);
            }
        }
    }

    /**
     * 获取 excel 文本所在 行数及列数。
     *
     * @param excelInsertVo
     * @param book
     */
    public static void getSheetRowAndColumn(ExcelInsertVo excelInsertVo, XSSFWorkbook book) {
        XSSFSheet sheet = book.getSheet(excelInsertVo.getSheetName());
        int lastRowNum = sheet.getLastRowNum(); //获取表格内容的最后一行的行数
        //rowBegin代表要开始读取的行号，下面这个循环的作用是读取每一行内容
        for (int x = 1; x <= lastRowNum; ++x) {
            XSSFRow row = sheet.getRow(x);//获取每一行
            int columnNum = row.getLastCellNum();//获取每一行的最后一列的列号，即总列数
            for (int y = 0; y < columnNum; ++y) {
                XSSFCell cell = row.getCell(y);//获取每个单元格
                if (cell != null) {
                    //设置单元格类型
                    cell.setCellType(cell.CELL_TYPE_STRING);
                    //获取单元格数据
                    String cellValue = cell.getStringCellValue();
                    if (!cellValue.equals("") && cellValue.equals(excelInsertVo.getRecordType())) {
//                        System.out.println("x == " + (x ));
//                        System.out.println("y == " + (y + 2));
                        excelInsertVo.setLeftColumn(y + 2);
                        excelInsertVo.setTopRow(x);
                    }
                }
            }
        }
    }

    /**
     * 清除指定单元格内容
     *
     * @param sheet
     * @param row
     * @param columln
     */
    private static void removeExcelCellValue(XSSFSheet sheet, int row, int columln) {
        //锁定要修改的单元格：先找到行，再找到列
        XSSFRow row1 = sheet.getRow(row);
        row1.createCell(columln).setCellValue("");
    }

    /**
     * sheetName 不包含 则清除
     *
     * @param map
     * @param wb
     */
    public static void removeSheetName(Map<String, Object> map, XSSFWorkbook wb) {
        for (int i = wb.getNumberOfSheets() - 1; i >= 0; i--) {
            XSSFSheet tmpSheet = wb.getSheetAt(i);
            if (map.get(tmpSheet.getSheetName()) == null) {
                wb.removeSheetAt(i);
            }
        }
    }
}

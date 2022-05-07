package com.lims.manage.erp.util;

import cn.hutool.core.date.DateTime;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2022/5/7 20:45
 * @Copyright © 河南交科院
 */
public class ExcelConvertPdf {
    public static void excelConvertPdf(Workbook workbook, OutputStream osOut, HttpServletResponse response) throws DocumentException, IOException {
        //设置页面大小
        Rectangle rectPageSize = new Rectangle(PageSize.A3);// 定义A3页面大小
        rectPageSize = rectPageSize.rotate(); //横版
        Document document = new Document(rectPageSize, -80, -80, 50, 0); //边距

        //字体设置
        /*
         * 由于itext不支持中文，所以需要进行字体的设置，可以调用项目资源文件或者调用windows系统的中文字体，
         * 找到文件后，打开属性，将文件名及所在路径作为字体名即可。
         */
        //创建BaseFont对象，指明字体，编码方式,是否嵌入
        BaseFont bf = BaseFont.createFont("c:\\windows\\fonts\\SIMHEI.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        //BaseFont bf=BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        //创建Font对象，将基础字体对象，字体大小，字体风格
        Font font = new Font(bf, 13, Font.NORMAL);
        int rowNum = 0;
        int colNum = 0;
        Sheet sheet = workbook.getSheetAt(0);
        int column = sheet.getRow(1).getLastCellNum();
        int row = sheet.getPhysicalNumberOfRows();
        System.out.println(row);
        //下面是找出表格中的空行和空列
        List nullCol = new ArrayList<>();
        List nullRow = new ArrayList<>();
        for (int j = 0; j < column; j++) {
            int nullColNum = 0;
            for (int i = 0; i < row; i++) {
                Cell cell = sheet.getRow(i).getCell(j);
                if (cell == null || (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) && "".equals(cell.getStringCellValue())) {
                    nullColNum++;
                }
            }
            if (nullColNum == row) {
                nullCol.add(j);
            }
        }
        for (int i = 0; i < row; i++) {
            int nullRowNum = 0;
            for (int j = 0; j < column; j++) {
                Cell cell = sheet.getRow(i).getCell(j);
                if (cell == null || (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) && "".equals(cell.getStringCellValue())) {
                    nullRowNum++;
                }
            }
            if (nullRowNum == column) {
                nullRow.add(i);
            }
        }
        PdfWriter writer = PdfWriter.getInstance(document,osOut);
        document.open();

        PdfPTable table = new PdfPTable(column-sheet.getRow(1).getFirstCellNum());
        List<CellRangeAddress> ranges = new ArrayList();
        int sheetMergeCount = sheet.getNumMergedRegions();
        System.out.println("sheetMergeCount"+sheetMergeCount);
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            ranges.add(range);
        }

        PdfPCell cell1 = null;
        String str = null;
        boolean mergeFlag = true;

        for (int i = sheet.getFirstRowNum(); i < row; i++) {
            /*if (nullRow.contains(i)) { //如果这一行是空行，这跳过这一行
                continue;
            }*/

            for (int j = sheet.getRow(1).getFirstCellNum(); j < column; j++) {
                /*if (nullCol.contains(j)) { //如果这一列是空列，则跳过这一列
                    continue;
                }*/

                boolean flag = true;
                Cell cell = sheet.getRow(i).getCell(j);
                if(null != cell) {
                    if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
                        str = (int) cell.getNumericCellValue() + "";
                    } else {
                        str = cell.getStringCellValue();
                    }

                    System.out.println("i ::"+i + "j::"+j);
                    System.out.println(str);

                    for (CellRangeAddress range : ranges) { //合并的单元格判断和处理

                        if (j >= range.getFirstColumn() && j <= range.getLastColumn() && i >= range.getFirstRow()
                                && i <= range.getLastRow()) {
                            /*if (str == null || "".equals(str)) {
                                flag = false;
                                if (ranges.size()>1){
                                    mergeFlag = true;
                                }else {
                                    mergeFlag = false;
                                }
                                break;
                            }*/
                            if(mergeFlag){
                                rowNum = range.getLastRow() - range.getFirstRow() + 1;
                                colNum = range.getLastColumn() - range.getFirstColumn() + 1;
                                cell1 = mergeCell(str, font, rowNum, colNum);
                                System.out.println("合并"+rowNum+"、"+colNum);
                                table.addCell(cell1);
                                flag = false;
                                if (ranges.size()>1){
                                    mergeFlag = true;
                                }else {
                                    mergeFlag = false;
                                }
                                break;
                            }
                            flag = false;
                            if (ranges.size()>1){
                                mergeFlag = true;
                            }else {
                                mergeFlag = false;
                            }
                        }
                    }

                    if (flag) {
                        table.addCell(getPDFCell(str, font));
                    }
                }

            }
        }

        document.add(table);
        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment;filename="+ DateTime.now()  +".pdf");
        document.close();
        writer.close();
    }

    //合并
    public static PdfPCell mergeCell(String str, Font font, int i, int j) {

        PdfPCell cell = new PdfPCell(new Paragraph(str, font));
        cell.setMinimumHeight(25);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setRowspan(i);
        cell.setColspan(j);

        return cell;
    }
    //获取指定内容与字体的单元格
    public static PdfPCell getPDFCell(String string, Font font) {
        //创建单元格对象，将内容与字体放入段落中作为单元格内容
        PdfPCell cell = new PdfPCell(new Paragraph(string, font));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        //设置最小单元格高度
        cell.setMinimumHeight(25);
        return cell;
    }

}


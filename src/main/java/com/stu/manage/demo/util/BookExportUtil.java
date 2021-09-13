package com.stu.manage.demo.util;

import com.stu.manage.demo.entity.Book;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 图书导出Excel工具包装类
 * @author Li
 */
public class BookExportUtil {
    /**
     * 主方法 设置表格
     * @param list 数据集合
     * @return 输入流
     * @throws IOException
     */
    public static InputStream bookExport(List<Book> list) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet();
            workbook.setSheetName(0, "图书信息");
            //填充数据
            setExcelData(list, sheet);

            workbook.write(os);
            os.close();
            return new ByteArrayInputStream(os.toByteArray());
        } catch (Exception e) {
            System.out.println("应用统计异常");
            throw e;
        }
    }

    /**
     * 填充数据
     * @param list 数据
     * @param sheet sheet
     */
    private static void setExcelData(List<Book> list, HSSFSheet sheet) {
        sheet.autoSizeColumn(1, true);

        HSSFRow row = sheet.createRow(0);

        HSSFCell cell = row.createCell(0);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("实例标题");

        cell = row.createCell(1);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("提交人组织");

        cell = row.createCell(2);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("提交人");

        cell = row.createCell(3);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("图书室");

        cell = row.createCell(4);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("启用日期");

        cell = row.createCell(5);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("页数");

        cell = row.createCell(6);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("作者");

        cell = row.createCell(7);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("出版社");

        cell = row.createCell(8);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("图书条形码");

        cell = row.createCell(9);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("责任人");

        cell = row.createCell(10);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("备注");
        cell = row.createCell(11);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("书名/资料名");
        cell = row.createCell(12);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("图书资料状态");
        cell = row.createCell(13);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("图书资料贡献人");
        cell = row.createCell(14);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("借阅次数");
        cell = row.createCell(15);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("图书资料封面");
        cell = row.createCell(16);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("评论");
        cell = row.createCell(17);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("标签");
        cell = row.createCell(18);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("打分");
        cell = row.createCell(19);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("当前借阅人");
        cell = row.createCell(20);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("定价");
        cell = row.createCell(21);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("简介");

        int count = 1;
        int index = 1;
        for (Book book : list) {
            row = sheet.createRow(count++);

            cell = row.createCell(0);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getStrengthTitle());

            cell = row.createCell(1);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getOrganization());

            cell = row.createCell(2);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getSubmitter());

            cell = row.createCell(3);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getLibrary());

            cell = row.createCell(4);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getDate());

            cell = row.createCell(5);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getPage());

            cell = row.createCell(6);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getAuthor());

            cell = row.createCell(7);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getPublisher());

            cell = row.createCell(8);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getIsbn());

            cell = row.createCell(9);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getLiableer());

            cell = row.createCell(10);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getNote());

            cell = row.createCell(11);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getTitle());

            cell = row.createCell(12);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getStat());

            cell = row.createCell(13);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getDevoteer());

            cell = row.createCell(14);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getNum());

            cell = row.createCell(15);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getImg());

            cell = row.createCell(16);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getComment());

            cell = row.createCell(17);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getLabel());

            cell = row.createCell(18);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getScore());

            cell = row.createCell(19);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getBorrower());

            cell = row.createCell(20);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getPrice());

            cell = row.createCell(21);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(book.getGist());
            index++;
        }
    }

}

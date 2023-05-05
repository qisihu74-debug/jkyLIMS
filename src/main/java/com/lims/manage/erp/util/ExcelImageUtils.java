package com.lims.manage.erp.util;

import com.lims.manage.erp.vo.ExcelInsertVo;
import com.spire.ms.System.Collections.IEnumerator;
import com.spire.xls.*;
import com.spire.xls.collections.PicturesCollection;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author: DLC
 * @Date: 2023/4/25 9:17
 * <p>
 * e-iceblue 往excel插入图片
 */
public class ExcelImageUtils {

    /**
     * excel 文件插入图片
     *
     * @param filePath D:\doc\43bc6f96-754a-4834-bc61-a88eac9846a0.xlsx
     * @param list
     * @param flag true = 清除图片、 flase = 不清楚图片
     */
//    public static void ExcelInsertImage(String filePath, String[] imags, String[] sheetNames) {
    public static void ExcelInsertImage(String filePath, List<ExcelInsertVo> list, String newFilePath,Boolean flag) throws IOException {
        // 获取 wb 读取行号及列号
        InputStream fileStream = new FileInputStream(filePath);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        //创建Workbook实例
        Workbook workbook = new Workbook();
        //加载Excel文档
        workbook.loadFromFile(filePath);
        Set<String> setSheetName = new HashSet<>();
        for (ExcelInsertVo excelInsertVo : list) {
            setSheetName.add(excelInsertVo.getSheetName());
        }
        for (String sheetName : setSheetName) {
            //获取Excel工作表
            Worksheet sheet = workbook.getWorksheets().get(sheetName);
            if (sheet != null && flag == true) {
                // 清除之前的 旧图片
                PicturesCollection excelImag = sheet.getPictures();
                IEnumerator it = excelImag.iterator();
                int itNext = 0;
                while (it.hasNext()) {
                    sheet.getPictures().get(itNext).remove();
                    itNext += 1;
                }
            }
        }
        for (int i = 0; i < list.size(); i++) {
            ExcelInsertVo data = list.get(i);
            //获取Excel工作表
            Worksheet sheet = workbook.getWorksheets().get(data.getSheetName());
            if (sheet != null) {
                // 序号
                int serialNumber = 0;
                //设置图表插入的位置
                ExcelReplaceUtil.getSheetRowAndColumn(data, wb);
                // 新增图片
                for (int j = 0; j < data.getImags().length; j++) {
                    if (data.getImags()[j] != null) {
                        // 塞入指定位置 图片
                        ExcelPicture pic = sheet.getPictures().add(data.getTopRow() + serialNumber, data.getLeftColumn(), data.getImags()[j]);
                        //设置图片的宽度和高度
                        pic.setWidth(80);
                        pic.setHeight(30);
                        serialNumber += 1;
                    }
                }
            }
        }
        // 删除附件
        FileAndFolderUtil.delete(filePath);
        //保存文档
        workbook.saveToFile(newFilePath, ExcelVersion.Version2013);
    }

//    public static void main(String[] args) throws IOException {
//        List<ExcelInsertVo> list = new ArrayList<>();
//        ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
//        // 编辑类型
//        excelInsertVo1.setRecordType("检测：");
//        // 签名信息
//        String[] imags = new String[2];
//        imags[0] = "D:\\doc\\image\\1647502446459100.png";
////        imags[1] = "D:\\doc\\image\\1647502446459100.png";
//        excelInsertVo1.setImags(imags);
//        // sheet名称
//        excelInsertVo1.setSheetName("水泥密度、比表面积试验检测记录表");
//        list.add(excelInsertVo1);
//
//        ExcelInsertVo excelInsertVo2 = new ExcelInsertVo();
//        // 编辑类型
//        excelInsertVo2.setRecordType("记录：");
//        // 签名信息
//        String[] imags2 = new String[1];
//        imags2[0] = "D:\\doc\\image\\1647502446459100.png";
//        excelInsertVo2.setImags(imags2);
//        // sheet名称
//        excelInsertVo2.setSheetName("水泥密度、比表面积试验检测记录表");
//        list.add(excelInsertVo2);
//        String filePath = "D:\\doc\\e-iceblue\\shuini.xlsx";
//        String newFilePath = "D:\\doc\\e-iceblue\\new演示插入结果.xlsx";
//        // 图片插入至excel中
//        ExcelInsertImage(filePath, list, newFilePath);
//        System.out.println("newFilePath  == " + newFilePath);
//    }

//    /**
//     * 转pdf
//     */
//    public static void transFileToPdf(String fillPath) {
//        Workbook wb = new Workbook();
//        wb.loadFromFile(fillPath);
//        wb.getWorksheets().get(0);
//        ConverterSetting converterSetting = new ConverterSetting();
//        converterSetting.setSheetFitToPage(true);
//        wb.setConverterSetting(converterSetting);
//        //调用方法保存为PDF格式
//        wb.saveToFile("D:\\doc\\e-iceblue\\庞源在线-安全-安全周报-20230303130801.pdf", FileFormat.PDF);
//    }

}





package com.lims.manage.erp.util;

import com.lims.manage.erp.vo.ExcelInsertVo;
import com.spire.ms.System.Collections.IEnumerator;
import com.spire.xls.*;
import com.spire.xls.collections.PicturesCollection;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
     * @param flag true = 清除图片、 flase = 不清除图片
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
        for (int i = 0; i < list.size(); i++) {
            ExcelInsertVo data = list.get(i);
            //获取Excel工作表
            Worksheet sheet = workbook.getWorksheets().get(data.getSheetName());
            if (sheet != null) {
                // 序号
                int serialNumber = 0;
                //设置图表插入的位置
                ExcelReplaceUtil.getSheetRowAndColumn(data, wb);
                // 清除指定行与列的图片信息
                if (flag == false) {
                    // 根据 行号和列号 比对 存在的话，返回当前下标。
                    List<Integer> indexs1 = getReviewCoord(data.getLeftColumn() - 1, data.getTopRow() - 1, wb.getSheet(data.getSheetName()));
                    List<Integer> indexs2 = getReviewCoord(data.getLeftColumn() - 1, data.getTopRow(), wb.getSheet(data.getSheetName()));
                    // indexs1 和 indexs2 存在两个 是因为 当前签名信息 最多两行
                    List<Integer> indexs = new ArrayList<>();
                    if(!CollectionUtils.isEmpty(indexs1)){
                        indexs.addAll(indexs1);
                    }
                    if(!CollectionUtils.isEmpty(indexs2)){
                        indexs.addAll(indexs2);
                    }
                    // 清除之前的 旧图片
                    PicturesCollection excelImag = sheet.getPictures();
                    IEnumerator it = excelImag.iterator();
                    int itNext = 0;
                    while (it.hasNext()) {
                        for (Integer index : indexs) {
                            if (index == itNext) {
                                sheet.getPictures().get(itNext).remove();
                            }
                        }
                        itNext += 1;
                    }
                }
                // 新增图片
                for (int j = 0; j < data.getImags().length; j++) {
                    if (data.getImags()[j] != null) {
                        // 塞入指定位置 图片
                        ExcelPicture pic = sheet.getPictures().add(data.getTopRow() + serialNumber, data.getLeftColumn(), data.getImags()[j]);
                        //设置图片的宽度和高度
                        pic.setWidth(80);
                        pic.setHeight(30);
                        serialNumber += 1;
//                        if(flag == false){
//                            // 塞入日期
//                            sheet.get(data.getTopRow()+serialNumber,data.getLeftColumn()+5).setValue("2023-5-6");
//                        }
                    }
                }
            }
        }
        fileStream.close();
        // 删除附件
        FileAndFolderUtil.delete(filePath);
        //保存文档
        workbook.saveToFile(newFilePath, ExcelVersion.Version2013);
    }

    /**
     * 根据 行号和列号 比对 存在的话，返回当前下标。
     *
     * @param colum 获取形状对象左上角所占用的列数。
     * @param row   获取形状对象左上角所占用的行数。
     * @param sheet sheet 页
     * @return 返回 待删除的 下标
     */
    private static List<Integer> getReviewCoord(int colum, int row, XSSFSheet sheet) {
        // 在单元格上查找 chart 和 picture：
        XSSFDrawing drawing = null;
        try {
            drawing = sheet.createDrawingPatriarch();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(drawing == null){
            return null;
        }
        List<XSSFShape> shapesList = drawing.getShapes();
        // 符合条件的标号
        List<Integer> integers = new ArrayList<>();
        for (int i = 0; i < shapesList.size(); ++i) {
            XSSFShape shape = shapesList.get(i);
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;
                XSSFClientAnchor anchor11 = picture.getPreferredSize();
                //getCol1()：获取形状对象左上角所占用的列数。
                //getRow1()：获取形状对象左上角所占用的行数。
                //getCol2()：获取形状对象右下角所占用的列数。
                //getRow2()：获取形状对象右下角所占用的行数。
//                System.out.println("获取形状对象左上角所占用的列数" + anchor11.getCol1());
//                System.out.println("获取形状对象左上角所占用的行数。" + anchor11.getRow1());
                // 判断该图片是否是要删除的目标图片，可根据 anchor 参数来判定
                if (anchor11.getRow1() == row && anchor11.getCol1() == colum) {
//                    System.out.println("标号" + i);
                    integers.add(i);
                }
            }
        }
        return integers;
    }

//    public static void main(String[] args) throws IOException {
//        List<ExcelInsertVo> list = new ArrayList<>();
//        ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
//////        // 编辑类型
//        excelInsertVo1.setRecordType("检测：");
////        excelInsertVo1.setSheetName("膨胀率");
//////        // 签名信息
//        String[] imags = new String[2];
//        imags[0] = "D:\\doc\\image\\1647502446459100.png";
////        imags[1] = "D:\\doc\\image\\1647502682230103.png";
//        excelInsertVo1.setImags(imags);
//////        // sheet名称
//        excelInsertVo1.setSheetName("水泥密度、比表面积试验检测记录表");
//        list.add(excelInsertVo1);
//////
////        ExcelInsertVo excelInsertVo2 = new ExcelInsertVo();
//////        // 编辑类型
//////        excelInsertVo2.setRecordType("记录：");
//////        // 签名信息
//////        String[] imags2 = new String[1];
//////        imags2[0] = "D:\\doc\\image\\1647502446459100.png";
//////        excelInsertVo2.setImags(imags2);
////        // sheet名称
//////        excelInsertVo2.setSheetName("水泥密度、比表面积试验检测记录表");
//////        list.add(excelInsertVo2);
////        ExcelInsertVo excelInsertVo3 = new ExcelInsertVo();
////        excelInsertVo3.setSheetName("膨胀率");
////        excelInsertVo3.setRecordType("复核：");
////        excelInsertVo3.setImags(imags);
////        list.add(excelInsertVo3);
//        String filePath = "D:\\doc\\e-iceblue\\4602092399671262.xlsx";
//        String newFilePath = "D:\\doc\\e-iceblue\\new演示插入结果.xlsx";
//////        // 图片插入至excel中
//        ExcelInsertImage(filePath, list, newFilePath, false);
//        System.out.println("newFilePath  == " + newFilePath);
    // 获取 wb 读取行号及列号
//        InputStream fileStream = new FileInputStream(filePath);
//        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
//        XSSFSheet sheet = wb.getSheet("膨胀率");
//        Integer[] indexs = getReviewCoord(14, 37, sheet);

//        System.out.println("indexs == " + indexs);
    // 在单元格上查找 chart 和 picture：
//        XSSFDrawing drawing = sheet.createDrawingPatriarch();
//        for (XSSFChart chart : drawing.getCharts()) {
//            System.out.println(chart);
//            System.out.println("00");
//        }
//        for (XSSFShape picture : drawing.getShapes()) {
//            drawing  = picture.getDrawing();
//
//            System.out.println(picture);
//        }
//        List<XSSFShape> shapesList = drawing.getShapes();
//        for (int i = 0; i < shapesList.size(); ++i) {
//            XSSFShape shape = shapesList.get(i);
//            if (shape instanceof XSSFPicture) {
//                XSSFPicture picture = (XSSFPicture) shape;
//                XSSFClientAnchor anchor11 = picture.getPreferredSize();
//                //getCol1()：获取形状对象左上角所占用的列数。
//                //getRow1()：获取形状对象左上角所占用的行数。
//                //getCol2()：获取形状对象右下角所占用的列数。
//                //getRow2()：获取形状对象右下角所占用的行数。
//                System.out.println("获取形状对象左上角所占用的列数" + anchor11.getCol1());
//                System.out.println("获取形状对象左上角所占用的行数。" + anchor11.getRow1());
//                // 判断该图片是否是要删除的目标图片，可根据 anchor 参数来判定
//                if (anchor11.getRow1() == 37 && anchor11.getCol1() == 14) {
//                    System.out.println("标号" + i);
////                    XSSFShape shapeToDelete = drawing.getShapes().get(i);
////                    XSSFShapeGroup parent = shapeToDelete.getParent();
////                    workbook.removePictureData(shapeToDelete.getPictureData().getPictureIndex());
////                    XSSFShapeGroup parent =  shapeToDelete.getParent();
////                    drawing.removeShape(shapeToDelete);
////                    parent.getCTGraphicalObjectFrame().getGraphic().getGraphicData().getAny().remove(shapeToDelete.getCTShape());
////                    parent.
////                    drawing.removeShape(shapeToDelete);
////                    drawing.getRelations().remove(i);
////                    drawing.getShapes().get(i);
////                    .removePictureData(shapeToDelete.getPictureData().getPictureIndex());
////                    XSSFShape shapeToDelete = drawing.getShapes().get(i);
////                    if (shapeToDelete instanceof XSSFPicture) {
////                        XSSFShapeGroup group = ((XSSFPicture) shapeToDelete).getParent();
//////                        group.removeShape(shapeToDelete);
//////                        group.
////                    }
////                    XSSFWorkbook workbook = drawing.getWorkbook();
////                    workbook.removePictureData(shapeToDelete.getPictureData().getPictureIndex());
//
//                }
//            }
//        }


//        XSSFDrawing drawing = sheet.getDrawingPatriarch();
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





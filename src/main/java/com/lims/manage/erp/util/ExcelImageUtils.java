package com.lims.manage.erp.util;

import com.aspose.cells.Cells;
import com.aspose.cells.Picture;
import com.aspose.cells.PictureCollection;
import com.aspose.cells.PlacementType;
import com.aspose.pdf.TabOrder;
import com.lims.manage.erp.vo.ExcelInsertVo;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.util.CollectionUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @Author: DLC
 * @Date: 2023/4/25 9:17
 * <p>
 * e-iceblue 往excel插入图片
 */
public class ExcelImageUtils {

    /**
     * Excel 插入图片
     *
     * @param filePath    附件路径
     * @param list        数据
     * @param newFilePath 生成新文件路径
     * @throws Exception
     */
    public static void inserImage(String filePath, List<ExcelInsertVo> list, String newFilePath) throws Exception {
        // 获取 wb 读取行号及列号
        InputStream fileStream = new FileInputStream(filePath);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        // 打开要编辑的工作簿
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(filePath);
        for (int i = 0; i < list.size(); i++) {
            ExcelInsertVo excelInsertVo = list.get(i);
            // 获取需要操作的工作表
            com.aspose.cells.Worksheet worksheet = workbook.getWorksheets().get(excelInsertVo.getSheetName());
            if (worksheet != null) {
                // 处理 data 中 map遍历值。
                Map<String, Object> map = excelInsertVo.getMap();
                for (String key : map.keySet()) {
                    // 根据key 遍历数据
                    ExcelInsertVo data = (ExcelInsertVo) map.get(key);
                    // 序号
                    int serialNumber = 0;
                    //设置图表插入的位置
                    ExcelReplaceUtil.getSheetRowAndColumn(data, wb);
                    for (int j = 0; j < data.getImags().length; j++) {
                        if (data.getImags()[j] != null) {
                            // 塞入指定位置 图片
                            // 加载并插入位于本地路径下的图像文件
                            int row = data.getTopRow() + serialNumber; // 图片所在行索引（0-based）
                            int column = data.getLeftColumn(); // 图片所在列索引（0-based）
                            int widthInPixels = 80; // 图片宽度（以像素为单位）
                            int heightInPixels = 30; // 图片高度（以像素为单位）
                            String imagePath = data.getImags()[j];// 要插入的图像文件路径
                            // 创建一个 PictureCollection 对象
                            PictureCollection pics = worksheet.getPictures();
                            // 加载并插入图像文件到指定单元格范围内
                            int index = pics.add(row, column, imagePath);
//                            System.out.println("index == " + index);
                            Picture pic = pics.get(index);
                            // 设置图像的大小和缩放模式
                            pic.setWidth(widthInPixels);
                            pic.setHeight(heightInPixels);
                            //        pic.setSizeMode(PictureSizeMode.FIT);
                            serialNumber += 1;
                        }
                    }
                }
            }
        }
        // 保存修改后的工作簿
        workbook.save(newFilePath);

    }

//    public static void main(String[] args) throws Exception {
//        List<ExcelInsertVo> list = new ArrayList<>();
//        ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
//////        // 编辑类型
//        excelInsertVo1.setRecordType("检测：");
//        excelInsertVo1.setSheetName("膨胀率");
//////        // 签名信息
//        String[] imags = new String[2];
//        imags[0] = "D:\\doc\\image\\1647502446459100.png";
//        imags[1] = "D:\\doc\\image\\1647502682230103.png";
//        excelInsertVo1.setImags(imags);
//        list.add(excelInsertVo1);
//        String filePath = "D:\\doc\\e-iceblue\\4602092399671262.xlsx";
//        String newFilePath = "D:\\doc\\e-iceblue\\new演示插入结果.xlsx";
//        test1Image(filePath, list, newFilePath);
//    }

    public static void seachXY(String filePath, List<ExcelInsertVo> list, String newFilePath) throws Exception {
        // 获取 wb 读取行号及列号
        InputStream fileStream = new FileInputStream(filePath);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        // 打开要编辑的工作簿
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(filePath);
        for (int i = 0; i < list.size(); i++) {
            ExcelInsertVo excelInsertVo = list.get(i);
            // 处理 data 中 map遍历值。
            Map<String, Object> map = excelInsertVo.getMap();
            // indexs1 和 indexs2 存在两个 是因为 当前签名信息 最多两行
            List<Integer> indexs = new ArrayList<>();
            for (String key : map.keySet()) {
                // 根据key 遍历数据
                ExcelInsertVo data = (ExcelInsertVo) map.get(key);
//                System.out.println("遍历data 数据 == " + data);
                // 获取需要操作的工作表
                com.aspose.cells.Worksheet workSheet = workbook.getWorksheets().get(data.getSheetName());
                if (workSheet != null) {
                    //设置图表插入的位置
                    ExcelReplaceUtil.getSheetRowAndColumn(data, wb);
                    // 根据 行号和列号 比对 存在的话，返回当前下标。
                    List<Integer> indexs1 = getReviewCoord2(data.getLeftColumn(), data.getTopRow(), workSheet);
                    List<Integer> indexs2 = getReviewCoord2(data.getLeftColumn(), data.getTopRow() + 1, workSheet);
                    if (!CollectionUtils.isEmpty(indexs1)) {
                        indexs.addAll(indexs1);
                    }
                    if (!CollectionUtils.isEmpty(indexs2)) {
                        indexs.addAll(indexs2);
                    }
                }
            }
            // 获取需要操作的工作表
            com.aspose.cells.Worksheet workSheet = workbook.getWorksheets().get(excelInsertVo.getSheetName());
            if (workSheet != null) {
                // 针对 单一 sheet页面 进行删除
                removeSheetImage(workSheet, indexs);
            }
        }
        // 保存修改后的工作簿
        workbook.save(newFilePath);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (drawing == null) {
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

    /**
     * 根据标号 统一删除图片
     *
     * @param workSheet
     * @param indexs
     */
    public static void removeSheetImage(com.aspose.cells.Worksheet workSheet, List<Integer> indexs) {
//        System.out.println("indexs  ==  " + indexs);
        // 清除之前的 旧图片
        PictureCollection pics = workSheet.getPictures();
//        System.out.println("pics.getCount() ==== " + pics.getCount());
        for (int xx = pics.getCount() - 1; xx >= 0; xx--) {
            if (!CollectionUtils.isEmpty(indexs)) {
                // 使用 Collections.sort 方法按从小到大对列表进行排序
                Collections.sort(indexs);
                for (int yy = indexs.size() - 1; yy >= 0; yy--) {
                    if (indexs.get(yy).equals(xx)) {
                        pics.removeAt(xx);
                    }
                }
            }
        }
    }

    /**
     * 222222222222222222222222222222
     * 根据 行号和列号 比对 存在的话，返回当前下标。
     *
     * @param colum 获取形状对象左上角所占用的列数。
     * @param row   获取形状对象左上角所占用的行数。
     * @param sheet sheet 页
     * @return 返回 待删除的 下标
     */
    private static List<Integer> getReviewCoord2(int colum, int row, com.aspose.cells.Worksheet sheet) {
        // 符合条件的标号
        List<Integer> integers = new ArrayList<>();
        // 获取图片对象集合
        PictureCollection pictures = sheet.getPictures();
        // 遍历图片列表获取所有图片的位置信息
        for (int xx = pictures.getCount() - 1; xx >= 0; xx--) {
            Picture picture = pictures.get(xx);
            // 获取图片所在单元格
            int leftColumn = picture.getUpperLeftColumn();
            int leftRow = picture.getUpperLeftRow();
            if (leftRow == row && leftColumn == colum) {
                integers.add(xx);
            }
        }
        return integers;
    }

//    public static void main(String[] args) throws Exception {
//        String filePath = "D:\\doc\\e-iceblue\\4603130104823154.xlsx";
////        String newFilePath = "D:\\doc\\e-iceblue\\new演示插入结果.xlsx";
////        String filePath = "D:\\doc\\e-iceblue\\new演示插入结果.xlsx";
////        String newFilePath = "D:\\doc\\e-iceblue\\new11111演示插入结果.xlsx";
////        // 清除模板图片
////        seachXY(filePath, list, newFilePath);
//        // 打开要编辑的工作簿
//        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(filePath);
//        // 获取需要操作的工作表
//        com.aspose.cells.Worksheet workSheet = workbook.getWorksheets().get("二氧化钛标准曲线");
//        getReviewCoord2(9, 27, workSheet);
//    }


}





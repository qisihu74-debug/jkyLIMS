package com.lims.manage.erp.util;

import com.aspose.cells.*;
import com.lims.manage.erp.vo.ExcelInsertVo;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

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
                    if (data.getImags() != null) {
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
                                Picture pic = pics.get(index);
                                // 设置图像的大小和缩放模式
                                pic.setWidth(widthInPixels);
                                pic.setHeight(heightInPixels);
                                serialNumber += 1;
                            }
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

    /**
     * Excel 插入文本并删除旧内容
     *
     * @param filePath    附件路径
     * @param list        数据
     * @param newFilePath 生成新文件路径
     * @throws Exception
     */
    public static void inserContext(String filePath, List<ExcelInsertVo> list, String newFilePath) throws Exception {
        // 获取 wb 读取行号及列号
        InputStream fileStream = new FileInputStream(filePath);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        // 打开要编辑的工作簿
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(filePath);
        for (int i = 0; i < list.size(); i++) {
            ExcelInsertVo excelInsertVo = list.get(i);
            // 处理 data 中 map遍历值。
            Map<String, Object> map = excelInsertVo.getMap();
            for (String key : map.keySet()) {
                // 根据key 遍历数据
                ExcelInsertVo data = (ExcelInsertVo) map.get(key);
                // 获取需要操作的工作表
                com.aspose.cells.Worksheet workSheet = workbook.getWorksheets().get(data.getSheetName());
                if (workSheet != null) {
                    //设置图表插入的位置
                    ExcelReplaceUtil.getSheetRowAndColumn(data, wb);
                    Cells cells = workSheet.getCells();
                    cells.get(data.getTopRow(), data.getLeftColumn()).setValue(data.getData());
                }
            }
        }
        // 保存修改后的工作簿
        workbook.save(newFilePath);
    }
//        public static void main(String[] args) throws Exception {
//            String filePath = "D:\\doc\\e-iceblue\\4603130104823154.xlsx";
//            String newPath = "D:\\doc\\e-iceblue\\新插入文本文件.xlsx";
//            List<ExcelInsertVo> excelInsertVoList = new ArrayList<>();
//            Map<String, Object> map = new HashMap<>();
//            // key 使用 sheet名加类型进行拼接
//            ExcelInsertVo excelInsertVo3 = new ExcelInsertVo();
//            excelInsertVo3.setSheetName("二氧化钛标准曲线");
//            excelInsertVo3.setRecordType("日期：");
//            excelInsertVo3.setData("2023年5月4日");
//            // key 使用 sheet名加类型进行拼接
//            map.put(excelInsertVo3.getSheetName() + excelInsertVo3.getRecordType(), excelInsertVo3);
//            ExcelInsertVo excelInsertVo = new ExcelInsertVo();
//            excelInsertVo.setSheetName("二氧化钛标准曲线");
//            excelInsertVo.setMap(map);
//            excelInsertVoList.add(excelInsertVo);
////            inserContext(filePath,excelInsertVoList,newPath);
//            inserContext(newPath,excelInsertVoList,filePath);
//    }

    /**
     * 生成日期图片
     *
     * @param imagePath
     * @return
     * @throws IOException
     */
    public static String inserNewImage(String imagePath) throws IOException {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        String titleStr = formatter.format(currentTime);
        int width = 560; // 图片宽
        int height = 320;// 图片高
        // 得到图片缓冲区
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);// INT精确度达到一定,RGB三原色，高度70,宽度150

        // 得到它的绘制环境(这张图片的笔)
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        java.awt.Color transparentRed = new java.awt.Color(255, 255, 255, 255);
        g2.setColor(transparentRed); // 设置背景颜色
        g2.fillRect(0, 0, width, height);// 填充整张图片(其实就是设置背景颜色)
        g2.setColor(Color.black);// 设置字体颜色

        // 设置标题的字体,字号,大小
        java.awt.Font titleFont = new java.awt.Font("宋体", Font.BOLD, 70);
        g2.setFont(titleFont);
        String markNameStr = titleStr;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        // 计算文字长度,计算居中的X点坐标
        FontMetrics fm = g2.getFontMetrics(titleFont);
        int titleWidth = fm.stringWidth(markNameStr);
        int titleWidthX = (width - titleWidth) / 2;// 感觉不居中,向左移动5个单位
        g2.drawString(markNameStr, titleWidthX, 160);
        g2.dispose(); // 释放对象
//        String imagePath = dir + GenID.getID()+"."+"jpg";
        ImageIO.write(bi, "PNG", new FileOutputStream(imagePath));// 保存图片 JPEG表示保存格式
        return imagePath;
    }

    /**
     * 将背景替换为透明
     *
     * @param path the img bytes
     * @return
     * @throws IOException the io exception
     * @author Jack Que
     * @created 2021 -07-08 10:25:10 Change img color.
     */
    public static void changeImgColor(String path) throws IOException {

        File file = new File(path);
        String fileName = file.getName();
        BufferedImage bi = ImageIO.read(file);
        Image image = (Image) bi;
        //将原图片的二进制转化为ImageIcon
        ImageIcon imageIcon = new ImageIcon(image);
        int width = imageIcon.getIconWidth();
        int height = imageIcon.getIconHeight();
//
        //图片缓冲流
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics();
        graphics2D.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());

        int alpha = 255;

        //这个背景底色的选择，我这里选择的是比较偏的位置，可以修改位置。背景色选择不知道有没有别的更优的方式（比如先过滤一遍获取颜色次数最多的，但是因为感觉做起来会比较复杂没去实现），如果有可以评论。
        int RGB = bufferedImage.getRGB(width - 1, height - 1);

        for (int i = bufferedImage.getMinX(); i < width; i++) {
            for (int j = bufferedImage.getMinY(); j < height; j++) {

                int rgb = bufferedImage.getRGB(i, j);

                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                int R = (RGB & 0xff0000) >> 16;
                int G = (RGB & 0xff00) >> 8;
                int B = (RGB & 0xff);
                //a为色差范围值，渐变色边缘处理，数值需要具体测试，50左右的效果比较可以
                int a = 45;
                if (Math.abs(R - r) < a && Math.abs(G - g) < a && Math.abs(B - b) < a) {
                    alpha = 0;
                } else {
                    alpha = 255;
                }
                rgb = (alpha << 24) | (rgb & 0x00ffffff);
                bufferedImage.setRGB(i, j, rgb);
            }
        }

//        graphics2D.drawImage(bufferedImage, 0, 0, imageIcon.getImageObserver());

        //新建字节输出流，用来存放替换完背景的图片
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

//            String[] split = fileName.split("\\.");
//            fileName = split[0]+"(已转换)."+split[1];
        ImageIO.write(bufferedImage, "png", new File(path));
    }

}





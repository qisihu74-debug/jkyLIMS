package com.lims.manage.erp.util;

import com.aspose.cells.License;
import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.Workbook;
import com.aspose.cells.WorksheetCollection;
import com.aspose.slides.Presentation;
import com.aspose.words.SaveFormat;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.PdfPageSize;
import com.spire.pdf.graphics.PdfMargins;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.web.multipart.MultipartFile;

import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2022/6/21 14:31
 * @Copyright © 河南交科院
 */
@Slf4j
public class PDFHelper3 {
    private static InputStream inputStream = null;
    /**
     * 获取license 去除水印
     * @return
     */
    public static boolean getLicense() {
        boolean result = false;
        try {
            ClassLoader contextClassLoader =AsposeUtil.class.getClassLoader();
            inputStream =contextClassLoader.getResourceAsStream("license.xml");
            License aposeLic = new License();
            aposeLic.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取license 去除水印
     * @return
     */
    public static boolean getWordsLicense() {
        boolean result = false;
        try {
            ClassLoader contextClassLoader =AsposeUtil.class.getClassLoader();
            inputStream =contextClassLoader.getResourceAsStream("license-pdf.xml");
            com.aspose.pdf.License aposeLic = new com.aspose.pdf.License();
            aposeLic.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * slider
     * @return
     */
    public static boolean getPdfLicense() {
        boolean result = false;
        try {
            ClassLoader contextClassLoader =AsposeUtil.class.getClassLoader();
            inputStream =contextClassLoader.getResourceAsStream("licence-ppt.xml");
            com.aspose.slides.License aposeLic = new com.aspose.slides.License();
            aposeLic.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //移除文字水印
    public static boolean removeWatermark(File file) {
        try {
            XWPFDocument doc = new XWPFDocument(new FileInputStream(file));
            // 段落
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text=paragraph.getText();
                if("Evaluation Only. Created with Aspose.Pdf. Copyright 2002-2018 Aspose Pty Ltd.".equals(text)){
                    List<XWPFRun> runs = paragraph.getRuns();
                    runs.forEach(e-> e.setText("",0));
                }
                if("Evaluation Only. Created with Aspose.Words. Copyright 2003-2018 Aspose Pty Ltd.".equals(text)){
                    List<XWPFRun> runs = paragraph.getRuns();
                    runs.forEach(e-> e.setText("",0));
                }
                if(" Evaluation Warning : The document was created with Spire.PDF for Java.".equals(text)){
                    List<XWPFRun> runs = paragraph.getRuns();
                    runs.forEach(e-> e.setText("",0));
                }
            }
            FileOutputStream outStream = new FileOutputStream(file);
            doc.write(outStream);
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * pdf转word
     * @param file
     * @param basePath
     */
    public static void pdf2doc(MultipartFile file, String basePath) {
        FileInputStream inputStream = null;
        File toFile = AsposeUtil.MultipartFileToFile(file);
        long old = System.currentTimeMillis();
        try {
            File fileDoc = new File(basePath);
            FileOutputStream os = new FileOutputStream(fileDoc);
            inputStream = new FileInputStream(toFile);
            com.aspose.pdf.Document doc = new com.aspose.pdf.Document(inputStream);//加载源文件数据
            System.out.println("开始转换");
            doc.save(os, com.aspose.pdf.SaveFormat.DocX);//设置转换文件类型并转换
            os.close();
            //去除水印
            removeWatermark(new File(basePath));
            //转化用时
            long now = System.currentTimeMillis();
            System.out.println("Pdf 转 Word 共耗时：" + ((now - old) / 1000.0) + "秒");
        } catch (Exception e) {
            System.out.println("Pdf 转 Word 失败...");
            e.printStackTrace();
        }
    }

    /**
     * word转pdf
     * @param file
     * @param basePath
     */
    public static void doc2pdf(MultipartFile file, String basePath) throws Exception {
        FileInputStream inputStream = null;
        File toFile = AsposeUtil.MultipartFileToFile(file);
        long old = System.currentTimeMillis();
        try {
            File fileDoc = new File(basePath);
            FileOutputStream os = new FileOutputStream(fileDoc);
            inputStream = new FileInputStream(toFile);
            com.aspose.words.Document doc = new com.aspose.words.Document(inputStream);//加载源文件数据
            System.out.println("开始转换");
            doc.save(os, SaveFormat.PDF);//设置转换文件类型并转换
            os.close();
            //转化用时
            long now = System.currentTimeMillis();
            System.out.println("word 转 pdf 共耗时：" + ((now - old) / 1000.0) + "秒");
        } catch (Exception e) {
            System.out.println("word 转 pdf 失败...");
            e.printStackTrace();
        }
    }

    /**
     * excel转pdf
     * @param file
     * @param basePath
     * @throws Exception
     */
    public static void excel2pdf(MultipartFile file, String basePath) throws Exception {
        getLicense();
        FileInputStream inputStream = null;
        File toFile = AsposeUtil.MultipartFileToFile(file);
        long old = System.currentTimeMillis();
        try {
            File fileDoc = new File(basePath);
            FileOutputStream os = new FileOutputStream(fileDoc);
            inputStream = new FileInputStream(toFile);
            Workbook wb = new Workbook(inputStream);
            WorksheetCollection worksheets = wb.getWorksheets();
            int count = worksheets.getCount();
            int[] autoDrawSheets=new int[count];
            int[] showSheets=new int[count];
            int index=0;
            while (count != 0){
                autoDrawSheets[index]=count-1;
                showSheets[index]=count-1;
                count--;
                index++;
            }
            int[] reverse = reverse(autoDrawSheets);
            int[] reverse1 = reverse(showSheets);

            FileOutputStream fileOS = new FileOutputStream(basePath);
            PdfSaveOptions pdfSaveOptions = new PdfSaveOptions();
            pdfSaveOptions.setOnePagePerSheet(true);
            //当excel中对应的sheet页宽度太大时，在PDF中会拆断并分页。此处等比缩放。
            autoDraw(wb,reverse);
            //隐藏workbook中不需要的sheet页。
            //printSheetPage(wb,reverse1);
            //wb.save(fileOS, SaveFormat.PDF);
            wb.save(fileOS, pdfSaveOptions);
            os.close();
            //转化用时
            long now = System.currentTimeMillis();
            System.out.println("excel 转 pdf 共耗时：" + ((now - old) / 1000.0) + "秒");
        } catch (Exception e) {
            System.out.println("excel 转 pdf 失败...");
            e.printStackTrace();
        }
    }

    /**
     * excel转pdf
     * @param inputStream
     * @param basePath
     * @throws Exception
     */
    public static ByteArrayOutputStream excel2pdf2(InputStream inputStream, String basePath) throws Exception {
        ByteArrayOutputStream bio = null;
        getLicense();
        long old = System.currentTimeMillis();
        try {
            File fileDoc = new File(basePath);
            FileOutputStream os = new FileOutputStream(fileDoc);
            Workbook wb = new Workbook(inputStream);
            WorksheetCollection worksheets = wb.getWorksheets();
            int count = worksheets.getCount();
            int[] autoDrawSheets=new int[count];
            int[] showSheets=new int[count];
            int index=0;
            while (count != 0){
                autoDrawSheets[index]=count-1;
                showSheets[index]=count-1;
                count--;
                index++;
            }
            int[] reverse = reverse(autoDrawSheets);
            int[] reverse1 = reverse(showSheets);

            FileOutputStream fileOS = new FileOutputStream(basePath);
            PdfSaveOptions pdfSaveOptions = new PdfSaveOptions();
            pdfSaveOptions.setOnePagePerSheet(true);
            //设置合规性类型
            //当excel中对应的sheet页宽度太大时，在PDF中会拆断并分页。此处等比缩放。
            autoDraw(wb,reverse);
            //隐藏workbook中不需要的sheet页。
            //printSheetPage(wb,reverse1);
            //wb.save(fileOS, SaveFormat.PDF);
            wb.save(fileOS, pdfSaveOptions);
            os.close();
            //设置pdf样式
            String s = setPdfStyle(basePath);
            //转化用时
            long now = System.currentTimeMillis();
            //处理流
            FileInputStream inputStream1 = new FileInputStream(s);
            bio = FileAndFolderUtil.parseIn(inputStream1);
            System.out.println("excel 转 pdf 共耗时：" + ((now - old) / 1000.0) + "秒");
            FileAndFolderUtil.delete(basePath);
            FileAndFolderUtil.delete(s);
        } catch (Exception e) {
            System.out.println("excel 转 pdf 失败...");
            e.printStackTrace();
        }
        return bio;
    }

    /**
     * excel转word
     * @param file
     * @param basePath
     * @throws Exception
     */
    public static void excel2word(MultipartFile file, String basePath) throws Exception {
        String pdfPath = basePath.substring(0, basePath.indexOf("."))+".pdf";
        excel2pdf(file,pdfPath);
        File file1 = new File(pdfPath);
        String originalFilename = file1.getName();
        String fileName = originalFilename.split("\\.")[0];
        MultipartFile multipartFile = AsposeUtil.fileToMultipart(file1, fileName);
        pdf2doc(multipartFile,basePath);
        /*FileInputStream inputStream = null;
        File toFile = AsposeUtil.MultipartFileToFile(file);
        long old = System.currentTimeMillis();
        try {
            File fileDoc = new File(basePath);
            OutputStream os = new FileOutputStream(fileDoc);
            inputStream = new FileInputStream(toFile);
            Workbook wb = new Workbook(inputStream);
            DocxSaveOptions options = new DocxSaveOptions();
//            options.setClearData(true);
//            options.setCreateDirectory(true);
//            options.setMergeAreas(true);
            System.out.println("开始转换");
            wb.save(os,options);
            os.close();
            //转化用时
            long now = System.currentTimeMillis();
            System.out.println("excel 转 word 共耗时：" + ((now - old) / 1000.0) + "秒");
        } catch (Exception e) {
            System.out.println("excel 转 word 失败...");
            e.printStackTrace();
        }*/
    }

    /**
     * ppt转pdf
     * @param file
     * @param basePath
     */
    public static void ppt2pdf(MultipartFile file, String basePath) {
        getPdfLicense();
        FileInputStream inputStream = null;
        File toFile = AsposeUtil.MultipartFileToFile(file);
        long old = System.currentTimeMillis();
        try {
            File fileDoc = new File(basePath);
            OutputStream os = new FileOutputStream(fileDoc);
            inputStream = new FileInputStream(toFile);
            Presentation ppt = new Presentation(inputStream);
            System.out.println("开始转换");
            ppt.save(os, com.aspose.slides.SaveFormat.Pdf);
            os.close();
            //转化用时
            long now = System.currentTimeMillis();
            System.out.println("ppt 转 pdf 共耗时：" + ((now - old) / 1000.0) + "秒");
        } catch (Exception e) {
            System.out.println("ppt 转 pdf 失败...");
            e.printStackTrace();
        }
    }

    /**
     * 设置打印的sheet 自动拉伸比例
     * @param wb
     * @param page 自动拉伸的页的sheet数组
     */
    public static void autoDraw(Workbook wb,int[] page){
        if(null!=page&&page.length>0){
            for (int i = 0; i < page.length; i++) {
                wb.getWorksheets().get(i).getHorizontalPageBreaks().clear();
                wb.getWorksheets().get(i).getVerticalPageBreaks().clear();
            }
        }
    }


    /**
     * 隐藏workbook中不需要的sheet页。
     * @param wb
     * @param page 显示页的sheet数组
     */
    public static void printSheetPage(Workbook wb,int[] page){
        for (int i= 1; i < wb.getWorksheets().getCount(); i++)  {
            wb.getWorksheets().get(i).setVisible(false);
        }
        if(null==page||page.length==0){
            wb.getWorksheets().get(0).setVisible(true);
        }else{
            for (int i = 0; i < page.length; i++) {
                wb.getWorksheets().get(i).setVisible(true);
            }
        }
    }

    public static int[] reverse(int[] a) {
        List<Integer> numbers = IntStream.of(a).boxed().collect(Collectors.toList());
        Collections.reverse(numbers);
        return numbers.stream().mapToInt(i -> i).toArray();
    }

    /**
     * 设置pdf每页缩放
     * @param basePath
     */
    public static String setPdfStyle(String basePath) {
        //创建PdfDocument对象
        PdfDocument originalDoc = new PdfDocument(basePath);
        //创建一个新的PdfDocument实例
        PdfDocument newDoc = new PdfDocument();
        //遍历所有PDF 页面
        int num = originalDoc.getPages().getCount();
        for (int i =0; i<num;i++){
            PdfPageBase page = originalDoc.getPages().get(i);
            PdfMargins margins = new PdfMargins(0, 0);
            //设置新文档的页面大小为A4
            PdfPageBase newPage = newDoc.getPages().add(PdfPageSize.A4, margins);
            //调整画布，设置内容也根据页面的大小进行缩放
            double wScale = 1.0; /*PdfPageSize.A4.getHeight() / page.getSize().getWidth();*/
            double hScale = 1.0; /*PdfPageSize.A4.getWidth() / page.getSize().getHeight();*/
            newPage.getCanvas().translateTransform(wScale, hScale);
            //复制原文档的内容到新文档
            newPage.getCanvas().drawTemplate(page.createTemplate(), new Point2D.Float());
        }
        //保存PDF
        String[] split = basePath.split("\\.");
        String s = split[0] + "doc" + ".pdf";
        newDoc.saveToFile(s);
        //去水印
        PdfDoc.removePdfWatermark(s,s,null);
        return s;
    }

    public static void main(String[] args) {
        setPdfStyle("D:\\Users\\Administrator\\Desktop\\office-tools\\merge(1).pdf");
    }
}

package com.lims.manage.erp.util;


import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.ImagePro;
import com.lims.manage.erp.entity.ReportRecordEntity;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Aut gjl
 * word转pdf
 */
public class AsposeUtil {

    private static InputStream inputStream = null;

    private static Logger logger = LoggerFactory.getLogger(AsposeUtil.class);

    /**
     * 转换pdf
     * @param document
     */
    public static void word2pdf(XWPFDocument document, String outAddress) {
        getLicense();
        try {
            File file = new File(outAddress);
            FileOutputStream os = new FileOutputStream(file);
            //XWPFDocument->Document
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            document.write(b);
            InputStream inputStream = new ByteArrayInputStream(b.toByteArray());
            Document doc = new Document(inputStream);
            doc.save(os, SaveFormat.PDF);
            os.close();
        } catch (Exception e) {
            logger.error("word转pdf失败:{}",e);
        }
    }

    public static void word2pdf2(XWPFDocument document, HttpServletResponse response, ReportRecordEntity reportRecordEntity, MinioClient client) {
        getLicense();
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            //XWPFDocument->Document
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            //盖章
            String sealUrl = reportRecordEntity.getSealUrl();
            List<ImagePro> imagePros = Lists.newArrayList();
            if (!StringUtils.isEmpty(sealUrl) || sealUrl != null) {
                String[] split = sealUrl.split(",");
                for (int i = 0; i < split.length; i++) {
                    ImagePro pro = new ImagePro(100 * (i + 1), 100, 15F, split[i]);
                    imagePros.add(pro);
                }
            }
            document.write(b);
            InputStream inputStream = new ByteArrayInputStream(b.toByteArray());
            Document doc = new Document(inputStream);
            doc.save(outputStream, SaveFormat.PDF);// TODO 得到pdf输入流
            //TODO 获取到pdf输入流
            OutputStream pdfStream = ImageToPdfUtils.writeToPdf2(inputStream, outputStream, imagePros);
            response.reset();
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=aaa.pdf");
            //OutputStream放入HttpServletResponse
            ByteArrayInputStream in = null;
            //输出pdf
            in = FileAndFolderUtil.parseOut(pdfStream);
            outputStream = response.getOutputStream();
            //获取要下载的文件输入流
            int len = 0;
            //创建数据缓冲区
            byte[] buffer = new byte[1024];
            //将FileInputStream流写入到buffer缓冲区
            while ((len = in.read(buffer)) > 0) {
                //使用OutputStream将缓冲区的数据输出到浏览器
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            in.close();
            outputStream.close();
            document.close();
        }catch (Exception e){
            logger.error("下载失败:{}",e);
        }
    }

    public static void word2pdf3(XWPFDocument document,HttpServletResponse response, ReportRecordEntity reportRecordEntity) {
        getLicense();
        try {
            //XWPFDocument->Document
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ByteArrayOutputStream b1 = new ByteArrayOutputStream();
            document.write(b);
            InputStream inputStream = new ByteArrayInputStream(b.toByteArray());
            Document doc = new Document(inputStream);
            doc.save(b1, SaveFormat.PDF);
            String sealUrl = reportRecordEntity.getSealUrl();
            List<ImagePro> imagePros = Lists.newArrayList();
            if (!StringUtils.isEmpty(sealUrl) || sealUrl != null) {
                String[] split = sealUrl.split(",");
                for (int i = 0; i < split.length; i++) {
                    ImagePro pro = new ImagePro(100 * (i + 1), 100, 15F, split[i]);
                    imagePros.add(pro);
                }
            }
            InputStream inputStream1 = new ByteArrayInputStream(b1.toByteArray());
            ImageToPdfUtils.writeToPdf3(inputStream1,response.getOutputStream(),imagePros);
            b.close();
            b1.close();
        } catch (Exception e) {
            logger.error("word转pdf失败:{}",e);
        }
    }
    public static ByteArrayOutputStream word2pdf4(XWPFDocument document) {
        getLicense();
        ByteArrayOutputStream b1 = null;
        try {
            //XWPFDocument->Document
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            b1 = new ByteArrayOutputStream();
            document.write(b);
            InputStream inputStream = new ByteArrayInputStream(b.toByteArray());
            Document doc = new Document(inputStream);
            doc.save(b1, SaveFormat.PDF);
//            String sealUrl = reportRecordEntity.getSealUrl();
//            List<ImagePro> imagePros = Lists.newArrayList();
//            if (!StringUtils.isEmpty(sealUrl) || sealUrl != null) {
//                String[] split = sealUrl.split(",");
//                for (int i = 0; i < split.length; i++) {
//                    ImagePro pro = new ImagePro(100 * (i + 1), 100, 15F, split[i]);
//                    imagePros.add(pro);
//                }
//            }
//            InputStream inputStream1 = new ByteArrayInputStream(b1.toByteArray());
//            ByteArrayOutputStream b2 = new ByteArrayOutputStream();
//            ByteArrayOutputStream b3 = ImageToPdfUtils.writeToPdf4(inputStream1, b2, imagePros);
//            InputStream inputStream2 = new ByteArrayInputStream(b3.toByteArray());
//            String upload = MinIoUtil.upload("report-download", reportRecordEntity.getReportCode() + ".pdf", inputStream2, "application/octet-stream");

            b.close();

        } catch (Exception e) {
            logger.error("word转pdf失败:{}",e);
        }
        return b1;
    }

    /**
     * 设置license
     * @return
     */
    public static boolean getLicense() {
        boolean result = false;
        try {
            ClassLoader contextClassLoader =AsposeUtil.class.getClassLoader();
            inputStream =contextClassLoader.getResourceAsStream("license.xml");
            com.aspose.words.License aposeLic = new com.aspose.words.License();
            aposeLic.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 合并报告
     * @param map
     * @return
     * @throws Exception
     */
    public static XWPFDocument mergeDoc(Map<Integer,XWPFDocument> map) throws Exception {
        //将map按照key的顺序将value转为list
        Map<Integer, XWPFDocument> documentMap = sortByKey(map, false);
        List<XWPFDocument> documentList = documentMap.values().stream()
                .collect(Collectors.toList());
        XWPFDocument xmd=documentList.get(0); //默认获取第一个作为模板
        for (int i=0;i<documentList.size()-1;i++) {
            xmd=mergeDoc(xmd,documentList.get(i+1)); //相继合并
        }
        return xmd;
    }

    /**
     * 根据map的key排序
     *
     * @param map 待排序的map
     * @param isDesc 是否降序，true：降序，false：升序
     * @return 排序好的map
     * @author zero 2019/04/08
     */
    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map, boolean isDesc) {
        Map<K, V> result = Maps.newLinkedHashMap();
        if (isDesc) {
            map.entrySet().stream().sorted(Map.Entry.<K, V>comparingByKey().reversed())
                    .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        } else {
            map.entrySet().stream().sorted(Map.Entry.<K, V>comparingByKey())
                    .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        }
        return result;
    }

    /**
     * doc文件输入流
     * @param document
     * @return
     * @throws IOException
     */
    public static InputStream docToIo(XWPFDocument document) throws IOException {
        //二进制OutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //文档写入流
        document.write(baos);
        //OutputStream写入InputStream二进制流
        ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
        return in;
    }

    /**
     * 合并word
     * @param src1Document
     * @param src2Document
     * @return
     */
    @SneakyThrows
    public static XWPFDocument mergeDoc(XWPFDocument src1Document, XWPFDocument src2Document){
        XWPFParagraph p = src1Document.createParagraph();
        //设置分页符
        p.setPageBreak(true);
        CTBody src1Body = src1Document.getDocument().getBody();
        CTBody src2Body = src2Document.getDocument().getBody();
        //XWPFParagraph p2 = src2Document.createParagraph();
        XmlOptions optionsOuter = new XmlOptions();
        optionsOuter.setSaveOuter();
        String appendString = src2Body.xmlText(optionsOuter);
        String srcString = src1Body.xmlText();
        String prefix = srcString.substring(0,srcString.indexOf(">")+1);
        String mainPart = srcString.substring(srcString.indexOf(">")+1,srcString.lastIndexOf("<"));
        String sufix = srcString.substring( srcString.lastIndexOf("<") );
        String addPart = appendString.substring(appendString.indexOf(">") + 1, appendString.lastIndexOf("<"));
        CTBody makeBody = CTBody.Factory.parse(prefix+mainPart+addPart+sufix);
        src1Body.set(makeBody);
        return src1Document;
    }

    /**
     * XWPFDocument 转 MultipartFile（CommonsMultipartFile）
     *
     * @param document 文档对象
     * @param fileName 文件名
     * @return
     */
    public static MultipartFile xwpfDocumentToCommonsMultipartFile(XWPFDocument document, String fileName) {
        //XWPFDocument转FileItem
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem fileItem = factory.createItem("textField", "text/plain", true, fileName);
        try {
            OutputStream os = fileItem.getOutputStream();
            document.write(os);
            os.close();
            //FileItem转MultipartFile
            MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
            return multipartFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * des:表末尾添加行(表，要复制样式的行，添加行数)
     * @param table
     * @param source
     * @param rows
     */
    public static void addRows(XWPFTable table, int source, int rows){
        try{
            //获取表格的总行数
            int index = table.getNumberOfRows();
            //循环添加行和和单元格
            for(int i=1;i<=rows;i++) {
                //获取要复制样式的行
                XWPFTableRow sourceRow = table.getRow(source);
                //添加新行
                XWPFTableRow targetRow = table.insertNewTableRow(index++);
                //复制行的样式给新行
                targetRow.getCtRow().setTrPr(sourceRow.getCtRow().getTrPr());
                //获取要复制样式的行的单元格
                List<XWPFTableCell> sourceCells = sourceRow.getTableCells();
                //循环复制单元格
                for (XWPFTableCell sourceCell : sourceCells) {
                    //添加新列
                    XWPFTableCell newCell = targetRow.addNewTableCell();
                    //复制单元格的样式给新单元格
                    newCell.getCTTc().setTcPr(sourceCell.getCTTc().getTcPr());
                    //得到复制单元格的段落
                    List<XWPFParagraph> sourceParagraphs = sourceCell.getParagraphs();
                    if (org.springframework.util.StringUtils.isEmpty(sourceCell.getText())) {
                        continue;
                    }
                    //拿到第一段
                    XWPFParagraph sourceParagraph = sourceParagraphs.get(0);
                    //得到新单元格的段落
                    List<XWPFParagraph> targetParagraphs = newCell.getParagraphs();
                    //判断新单元格是否为空
                    if (org.springframework.util.StringUtils.isEmpty(newCell.getText())) {
                        //添加新的段落
                        XWPFParagraph ph = newCell.addParagraph();
                        //复制段落样式给新段落
                        ph.getCTP().setPPr(sourceParagraph.getCTP().getPPr());
                        //得到文本对象
                        XWPFRun run = ph.getRuns().isEmpty() ? ph.createRun() : ph.getRuns().get(0);
                        //复制文本样式
                        run.setFontFamily(sourceParagraph.getRuns().get(0).getFontFamily());
                    } else {
                        XWPFParagraph ph = targetParagraphs.get(0);
                        ph.getCTP().setPPr(sourceParagraph.getCTP().getPPr());
                        XWPFRun run = ph.getRuns().isEmpty() ? ph.createRun() : ph.getRuns().get(0);
                        run.setFontFamily(sourceParagraph.getRuns().get(0).getFontFamily());
                    }
                }
            }
        }catch (Exception e){
            logger.error("word表格新增行失败:{}",e);
        }
    }
}

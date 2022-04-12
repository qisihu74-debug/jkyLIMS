package com.lims.manage.erp.util;


import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.deepoove.poi.xwpf.NiceXWPFDocument;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.ImagePro;
import com.lims.manage.erp.entity.ReportRecordEntity;
import io.minio.MinioClient;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
    public NiceXWPFDocument mergeDoc(Map<Integer,XWPFDocument> map) throws Exception {
        //将map按照key的顺序将value转为list
        Map<Integer, XWPFDocument> documentMap = sortByKey(map, false);
        List<XWPFDocument> documentList = map.values().stream()
                .collect(Collectors.toList());
        //新文档的地址
        File dest = new File("D:\\dest.docx");
        FileOutputStream out = new FileOutputStream(dest);
        //之前声明了一个 ArrayList<File> fileList;
        XWPFDocument document = documentList.get(0);
//        NiceXWPFDocument mainDoc = new NiceXWPFDocument(new FileInputStream(document));
//        for (int i = 1; i < documentList.size(); i++) {
//            NiceXWPFDocument subDoc = new NiceXWPFDocument(new FileInputStream(documentList.get(i)));
//            mainDoc = mainDoc.merge(subDoc);
//        }
//        mainDoc.write(out);
//        mainDoc.close();
//        out.close();
          return null;
    }

    /**
     * 根据map的key排序
     *
     * @param map 待排序的map
     * @param isDesc 是否降序，true：降序，false：升序
     * @return 排序好的map
     * @author zero 2019/04/08
     */
    public <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map, boolean isDesc) {
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
    public FileInputStream docToIo(XWPFDocument document) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//二进制OutputStream
        document.write(baos);//文档写入流
        ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());//OutputStream写入InputStream二进制流

        return null;
    }
}

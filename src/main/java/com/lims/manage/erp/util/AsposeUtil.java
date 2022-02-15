package com.lims.manage.erp.util;


import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.ImagePro;
import com.lims.manage.erp.entity.ReportRecordEntity;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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
            doc.save(outputStream, SaveFormat.PDF);
            InputStream inputStream1 = FileAndFolderUtil.convertIo(outputStream);
            ImageToPdfUtils.writeToPdf2(inputStream1, outputStream, imagePros);
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=aaa.pdf");
            outputStream.close();
        } catch (Exception e) {
            logger.error("word转pdf失败:{}",e);
        }
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

}

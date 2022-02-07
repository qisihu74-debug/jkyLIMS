package com.lims.manage.erp.util;


import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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
            document.write(b); // doc should be a XWPFDocument
            InputStream inputStream = new ByteArrayInputStream(b.toByteArray());
            Document doc = new Document(inputStream);
            /*for (Section sect : doc.getSections()) {
                removeWatermarkFromHeader(sect, HeaderFooterType.HEADER_PRIMARY);
                removeWatermarkFromHeader(sect, HeaderFooterType.HEADER_FIRST);
                removeWatermarkFromHeader(sect, HeaderFooterType.HEADER_EVEN);
            }*/
            doc.save(os, SaveFormat.PDF);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
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

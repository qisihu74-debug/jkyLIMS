package com.lims.manage.erp.util;


import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2021/12/13 15:07
 * @Copyright © 河南交科院
 */
public class FileAndFolderUtil {

    private final static Logger logger = LoggerFactory.getLogger(FileAndFolderUtil.class);
    /**
     * 获取项目加载根路径
     * @return
     */
    public static String getClassPath(){
        String strRes = "";
        try {
            strRes = FileAndFolderUtil.class.getResource("//").toURI().getPath();
        } catch (URISyntaxException e) {
            logger.info("getClassPath URISyntaxException:" + e.toString());
        }

        return strRes;
    }

    /**
     * 得到文件夹下的所有指定后缀文件名列表
     * @param strFolderPath 文件夹路径
     * @param strSuffix 需要遍历的的文件后缀
     * @param blIsAbsPath 是否采用绝对路径返回
     * @return
     */
    public static List<String> getFileName(String strFolderPath, String strSuffix, boolean blIsAbsPath) {
        List<String> lsFileName = new ArrayList<String>();

        //用于查找文件
        File getDocument;
        if (strFolderPath == null || strFolderPath.equals("")) {
            return null;
        } else {
            if (strFolderPath.substring(strFolderPath.length()-1).equals("/")) {
                strFolderPath = strFolderPath.substring(0, strFolderPath.length()-1);
            }
            getDocument = new File(strFolderPath);
        }
        //存储文件容器
        String getFileName[];
        getFileName = getDocument.list();
        if (getFileName==null || getFileName.length<1) {
            logger.error("no file in path:" + strFolderPath + "! please check!!!");
            return null;
        }

        //遍历整合
        for (int i = 0; i < getFileName.length; i++) {
            //文件名合法性检查
            String strFileNameTmp = getFileName[i];
            if (strFileNameTmp.length() <= strSuffix.length()) {
                continue;
            }
            if (!strFileNameTmp.substring(strFileNameTmp.length() - strSuffix.length()).equals(strSuffix)) {
                //文件后缀不符合的情况
                continue;
            }
            //文件路径加载
            String strFileName = "";
            if (blIsAbsPath) {
                strFileName = strFolderPath + File.separator + getFileName[i];
            }else{
                strFileName = getFileName[i];
            }
            logger.debug("have loaded file =" + strFileName);
            lsFileName.add(strFileName);
        }
        return lsFileName;
    }

    /**
     * 获取文件夹下文件名称列表
     * @param strFolderPath
     * @param strSuffix
     * @return
     */
    public static List<String> getFileName(String strFolderPath, String strSuffix){
        return getFileName(strFolderPath, strSuffix, true);
    }

    /**
     * docx文档转换为PDF
     * @param pdfPath PDF文档存储路径
     * @param document
     * @throws Exception 可能为Docx4JException, FileNotFoundException, IOException等
     */
    /*public static void convertDocxToPdf(XWPFDocument document, String pdfPath) throws Exception {
        //XWPFDocument转inputstream
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        document.write(b);
        InputStream inputStream = new ByteArrayInputStream(b.toByteArray());
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(pdfPath));
            WordprocessingMLPackage mlPackage = WordprocessingMLPackage.load(inputStream);
            setFontMapper(mlPackage);
            Docx4J.toPDF(mlPackage, new FileOutputStream(new File(pdfPath)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /**
     * 设置字体
     * @param mlPackage
     * @throws Exception
     */
    private static void setFontMapper(WordprocessingMLPackage mlPackage) throws Exception {
        Mapper fontMapper = new IdentityPlusMapper();
        fontMapper.put("隶书", PhysicalFonts.get("LiSu"));
        fontMapper.put("宋体", PhysicalFonts.get("SimSun"));
        fontMapper.put("微软雅黑", PhysicalFonts.get("Microsoft Yahei"));
        fontMapper.put("黑体", PhysicalFonts.get("SimHei"));
        fontMapper.put("楷体", PhysicalFonts.get("KaiTi"));
        fontMapper.put("新宋体", PhysicalFonts.get("NSimSun"));
        fontMapper.put("华文行楷", PhysicalFonts.get("STXingkai"));
        fontMapper.put("华文仿宋", PhysicalFonts.get("STFangsong"));
        fontMapper.put("宋体扩展", PhysicalFonts.get("simsun-extB"));
        fontMapper.put("仿宋", PhysicalFonts.get("FangSong"));
        fontMapper.put("仿宋_GB2312", PhysicalFonts.get("FangSong_GB2312"));
        fontMapper.put("幼圆", PhysicalFonts.get("YouYuan"));
        fontMapper.put("华文宋体", PhysicalFonts.get("STSong"));
        fontMapper.put("华文中宋", PhysicalFonts.get("STZhongsong"));

        mlPackage.setFontMapper(fontMapper);
    }

    /**
     * document对象转pdf
     * @param document
     * @param outUrl
     * @throws Exception
     */
    public static void docxToPdf(XWPFDocument document, String outUrl) throws Exception {
        PdfOptions options = PdfOptions.create();
        OutputStream out = new FileOutputStream(new File(outUrl));
        PdfConverter.getInstance().convert(document, out, options);
    }

    /**
     * 输出文件路径
     * @param outputFilePath
     * @return
     * @throws IOException
     */
    protected static OutputStream getOutFileStream(String outputFilePath) throws IOException{
        File outFile = new File(outputFilePath);
        try{
            outFile.getParentFile().mkdirs();
        } catch (NullPointerException e){
            logger.error("转换文件获取输出流异常:{}",e);
        }
        outFile.createNewFile();
        FileOutputStream oStream = new FileOutputStream(outFile);
        return oStream;
    }

    /**
     * docx 转成 pdf
     * @param outUrl 输出pdf文件名称
     * @param document 操作目录
     * @throws Exception
     */
    public static void convertDocxToPdf(XWPFDocument document, String outUrl) {
        OutputStream target = null;
        try {
            // 输出目标
            target = new FileOutputStream(outUrl);
            // 转换配置
            PdfOptions options = PdfOptions.create();
            // 兼容中文配置
            options.fontProvider(new IFontProvider() {
                @Override
                public com.lowagie.text.Font getFont(String familyName, String encoding, float size, int style, Color color) {
                    try {
                        BaseFont bfChinese = BaseFont.createFont("C:/Windows/Fonts/simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                        //BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                        com.lowagie.text.Font fontChinese = new com.lowagie.text.Font(bfChinese, size, style, color);
                        if (familyName != null) {
                            fontChinese.setFamily(familyName);
                        }
                        return fontChinese;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
            // 转换成pdf
            PdfConverter.getInstance().convert(document, target, options);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            close(target);
        }
    }

    /**
     * 关闭输出流
     * @param os
     */
    private static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 流转化
     * @param outputStream
     * @return
     */
    public static InputStream convertIo(ServletOutputStream outputStream){
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] bytes = b.toByteArray();
        try {
            outputStream.write(bytes);
        }catch (Exception e){
            logger.error("流转换失败:{}",e);
        }
        InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }

    /**
     * InputStream转OutputStream
     * @param in
     * @return
     * @throws Exception
     */
    public static ByteArrayOutputStream parseIn(InputStream in) throws Exception {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        int ch;
        while ((ch = in.read()) != -1) {
            swapStream.write(ch);
        }
        return swapStream;
    }

    /**
     * outputStream转InputStream
     * @param out
     * @return
     * @throws Exception
     */
    public static ByteArrayInputStream parseOut(final OutputStream out) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos = (ByteArrayOutputStream) out;
        final ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
        return swapStream;
    }

    /**
     * 将公网文件url转成文件FIle对象
     * @param url
     * @return
     * @throws Exception
     */
    public static File getFile(String url) throws Exception {
        //对本地文件命名
        String fileName = url.substring(url.lastIndexOf("."),url.length());
        File file = null;

        URL urlfile;
        InputStream inStream = null;
        OutputStream os = null;
        try {
            file = File.createTempFile("net_url", fileName);
            //下载
            urlfile = new URL(url);
            inStream = urlfile.openStream();
            os = new FileOutputStream(file);

            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = inStream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != os) {
                    os.close();
                }
                if (null != inStream) {
                    inStream.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    /**
     * 输入流转字节流
     * @param inputStream
     * @return
     */
    public static byte[] inputStramToByte(InputStream inputStream){
        InputStream bufferedInputStream =new BufferedInputStream(inputStream);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        byte[] buffer=null;
        int len=0;
        byte []buf=new byte[2048];
        try {
            while((len=bufferedInputStream.read(buf))!=-1){
                byteArrayOutputStream.write(buf, 0, len);
            }
            byteArrayOutputStream.flush();
        }catch (Exception e){
            logger.error("inputstream转字节流失败:{}",e);
        }
        buffer=byteArrayOutputStream.toByteArray();
        return buffer;
    }
}

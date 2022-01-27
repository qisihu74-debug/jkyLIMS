package com.lims.manage.erp.util;


import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.docx4j.Docx4J;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
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

  /*  public static String transFile2Str(String strFilePath){

        //创建SAXReader对象
        SAXReader reader = new SAXReader();
        //读取文件 转换成Document
        Document document = null;
        try {
            document = reader.read(new File(getClassPath() + strFilePath));
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //document转换为String字符串
        String documentStr = document.asXML();
        return documentStr;

    }*/

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
    public static void convertDocxToPdf(XWPFDocument document, String pdfPath) throws Exception {
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
    }

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
    public static void docxToPdf(XWPFDocument document, String outUrl ) throws Exception {
        /*OutputStream outStream=getOutFileStream(outUrl);
        PdfOptions options = PdfOptions.create();
        PdfConverter.getInstance().convert(document, outStream, options);*/
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
}

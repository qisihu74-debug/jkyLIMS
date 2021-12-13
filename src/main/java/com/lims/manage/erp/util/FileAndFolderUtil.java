package com.lims.manage.erp.util;


import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static String transFile2Str(String strFilePath){

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

}

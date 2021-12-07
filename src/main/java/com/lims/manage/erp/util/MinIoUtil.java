package com.lims.manage.erp.util;

import com.lims.manage.erp.config.MinioConfig;
import io.minio.MinioClient;
import io.minio.messages.Bucket;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.util
 * @desc
 * @date 2021/10/28 11:25
 * @Copyright © 河南交科院
 */
@Slf4j
@Component
public class MinIoUtil {
    public static MinioClient minioClient;

    @Autowired
    private MinioConfig minioProperties;

    public static MinIoUtil minIoUtil;

    /**
     * 初始化minio配置
     */
    @PostConstruct
    public  void init() {
        minIoUtil=this;
        minIoUtil.minioProperties=this.minioProperties;
        try {
            minioClient = minioProperties.getMinioClient();
            log.info(">>>>>>>>>>>minio 初始化成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("》》》》》》》》》》初始化minio异常: 【{}】", e.fillInStackTrace());
        }
    }

    /**
     * 判断 bucket是否存在
     *
     * @param bucketName 桶名
     * @return: boolean
     */
    @SneakyThrows(Exception.class)
    public static boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(bucketName);
    }

    /**
     * 创建 bucket
     * @param bucketName 桶名
     * @return: void
     */
    @SneakyThrows(Exception.class)
    public static void createBucket(String bucketName) {
        boolean isExist = minioClient.bucketExists(bucketName);
        if (!isExist) {
            minioClient.makeBucket(bucketName);
        }
    }

    /**
     *
     * 获取全部bucket
     * @return: java.util.List<io.minio.messages.Bucket>
     */
    @SneakyThrows(Exception.class)
    public static List<Bucket> getAllBuckets() {
        return minioClient.listBuckets();
    }

    /**
     * 文件上传
     * @param bucketName 桶名
     * @param fileName 文件名
     * @param filePath 文件路径
     */
    @SneakyThrows(Exception.class)
    public static void upload(String bucketName, String fileName, String filePath) {
        createBucket(bucketName);
        minioClient.putObject(bucketName, fileName, filePath, null);
    }

    /**
     * 文件上传（返回URL下载地址）
     * @param bucketName 桶名
     * @param fileName 文件名
     * @param stream 文件流
     * @return: 文件url下载地址
     */
    @SneakyThrows(Exception.class)
    public static String upload(String bucketName, String fileName, InputStream stream,String contentType) {
        createBucket(bucketName);
        minioClient.putObject(bucketName, fileName, stream, stream.available(),contentType);
        return getFileUrl(bucketName, fileName);
    }

    /**
     * 文件上传 （返回URL下载地址）
     * @param bucketName 桶名
     * @param file 文件
     * @return: 文件url下载地址
     */
    @SneakyThrows(Exception.class)
    public static String upload(String bucketName, MultipartFile file,String fileName) {
        createBucket(bucketName);
        final InputStream is = file.getInputStream();
        //final String fileName = file.getOriginalFilename();
        minioClient.putObject(bucketName, fileName, is, is.available(), Const.image);
        is.close();
        return getFileUrl(bucketName, fileName);
    }

    /**
     * 删除文件
     * @param bucketName 桶名
     * @param fileName 文件名
     */
    @SneakyThrows(Exception.class)
    public static void deleteFile(String bucketName, String fileName) {
        minioClient.removeObject(bucketName, fileName);
    }

    /**
     * 下载文件 （流输出）
     * @param bucketName  桶名
     * @param fileName 文件名
     */
    @SneakyThrows(Exception.class)
    public static void download(String bucketName, String fileName, HttpServletResponse response) {
       try {
           InputStream object = minioClient.getObject(bucketName, fileName);
           byte buf[] = new byte[1024];
           int length = 0;
           response.reset();

           response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
           response.setContentType("application/octet-stream");
           response.setCharacterEncoding("utf-8");
           OutputStream outputStream = response.getOutputStream();
           while ((length = object.read(buf)) > 0) {
               outputStream.write(buf, 0, length);
           }
           outputStream.close();
         } catch (Exception ex) {
            log.info("导出失败：", ex.getMessage());
         }
    }

    /**
     *
     * 获取minio文件的下载地址
     * @param bucketName 桶名
     * @param fileName  文件名
     */
    @SneakyThrows(Exception.class)
    public static String getFileUrl(String bucketName, String fileName) {
        return minioClient.presignedGetObject(bucketName, fileName);
    }

    /**
     *
     * 获取minio文件的下载地址
     * @param bucketName 桶名
     * @param fileName  文件名
     */
    @SneakyThrows(Exception.class)
    public static String getUrl(String bucketName, String fileName) {
        return minioClient.getObjectUrl(bucketName,fileName);
    }
    /**
     * 获取minio文件的输入流
     * @param bucketName 桶名
     * @param fileName  文件名
     * @return
     */
    @SneakyThrows(Exception.class)
    public static InputStream getFileStream(String bucketName, String fileName) {
        return minioClient.getObject(bucketName,fileName);
    }

}

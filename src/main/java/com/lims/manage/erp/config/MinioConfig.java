package com.lims.manage.erp.config;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.config
 * @desc
 * @date 2021/10/28 11:42
 * @Copyright © 河南交科院
 */
@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String url;
    @Value("${minio.ip}")
    private String endpoint;
    @Value("${minio.port}")
    private int port;
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String ecretKey;
    public static ConcurrentHashMap<String,Object> hashMap = new ConcurrentHashMap<String, Object>();

    public MinioClient getMinioClient() throws InvalidEndpointException,
            InvalidPortException {
        //MinioClient minioClient = new MinioClient(endpoint, port, accessKey, ecretKey);
        MinioClient minioClient = new MinioClient(url, accessKey, ecretKey);
        hashMap.put("minioClient",minioClient);
        return minioClient;
    }
}

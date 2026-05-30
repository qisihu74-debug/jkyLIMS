package com.lims.manage.erp.service;

import com.lims.manage.erp.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * OSS对象存储表(SysOss)表服务接口
 *
 * @author makejava
 * @since 2022-03-10 16:13:34
 */
public interface SysOssService  {
    Map<String,Object> postAnnounce(MultipartFile file);
    Boolean delAnnounce(String fileName);
}


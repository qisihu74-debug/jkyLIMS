package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TechnicistFiles;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2023-12-19 10:21
 * @Copyright © 河南交科院
 */
public interface TechnicistFilesService extends IService<TechnicistFiles> {

    /**
     * 上传或更新人员履历表
     * @param technicistId
     * @param file
     * @return
     */
    Boolean uploadResume(Integer technicistId, MultipartFile file);

    /**
     * 删除文件根据url
     * @param fileUrl
     */
    void delFileByUrl(String fileUrl);
}

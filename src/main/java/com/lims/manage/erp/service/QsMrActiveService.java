package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.QsMrActiveEntity;
import com.lims.manage.erp.result.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-07-26 11:14
 * @Copyright © 河南交科院
 */
public interface QsMrActiveService extends IService<QsMrActiveEntity> {

    /**
     * 提交管理评审总结
     *
     * @param qsMrActiveEntity
     * @param file
     * @return
     */
    Result submitInternalAuditDocument(QsMrActiveEntity qsMrActiveEntity, MultipartFile[] file);
}

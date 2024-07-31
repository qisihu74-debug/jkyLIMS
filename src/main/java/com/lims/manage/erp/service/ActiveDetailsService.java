package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.ActiveDetailsEntity;
import com.lims.manage.erp.entity.ActiveDetailsFileUrlEntity;
import com.lims.manage.erp.entity.QsMrActiveEntity;
import com.lims.manage.erp.result.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * 部门信息 上传附件详情
 *
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-07-26 11:43
 * @Copyright © 河南交科院
 */
public interface ActiveDetailsService extends IService<ActiveDetailsEntity> {

    /**
     * 部门负责人 上传材料
     *
     * @param activeDetailsEntity
     * @param file
     * @return
     */
    Result uploadMaterial(ActiveDetailsEntity activeDetailsEntity, MultipartFile[] file);

    /**
     * 部门负责人 删除上传材料
     *
     * @param activeDetailsFileUrlEntity
     * @return
     */
    Result removeFile(ActiveDetailsFileUrlEntity activeDetailsFileUrlEntity);

    /**
     * 根据登录人回显评审部门列表及附件信息
     *
     * @param activeId
     * @return
     */
    Result getDepartmentAndFile(String activeId);

    /**
     * 催办
     *
     * @param detailsEntity
     * @return
     */
    Result hastenWork(ActiveDetailsEntity detailsEntity);

}

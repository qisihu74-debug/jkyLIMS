package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.InternalAudit;
import com.lims.manage.erp.entity.SysUserEntity;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-01-02 14:49
 * @Copyright © 河南交科院
 */
public interface InternalAuditService extends IService<InternalAudit> {

    /**
     * 查询内审列表
     * @param pageSize
     * @param pageNum
     * @param search
     * @param userEntity
     * @return
     */
    PageInfo<InternalAudit> planList(Integer pageSize, Integer pageNum, String search, SysUserEntity userEntity);

    void delFileByUrl(String fileUrl);
}

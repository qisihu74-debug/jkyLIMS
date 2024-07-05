package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.InternalAuditorActive;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-07-05 11:25
 * @Copyright © 河南交科院
 */
public interface QsAuditService {

    /**
     * 查询内审员内审活动列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @param userId
     * @return
     */
    PageInfo<InternalAuditorActive> internalAuditorActiveList(Integer pageNum, Integer pageSize, String name, Long userId);
}

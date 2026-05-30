package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.AduditBaseData;
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

    /**
     * 查询内审基础信息
     * @return
     * @param pageNum
     * @param pageSize
     * @param type
     * @param dir
     * @param content
     * @param method
     * @param subject
     */
    PageInfo<AduditBaseData> getCheckBaseDataList(Integer pageNum, Integer pageSize, String type, String dir, String content, String method, String subject,Integer divideId);

    /**
     * 查看活动状态完成情况
     * @param activeId
     * @return
     */
    String getStateByActiveId(int activeId);

    /**
     * 根据内审活动id获取发布管理员的userId
     * @param activeId
     * @return
     */
    String getUserIdByActiveId(int activeId);

    /**
     * 部门负责人内审活动列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @param userId
     * @return
     */
    PageInfo<InternalAuditorActive> deptLeaderActiveList(Integer pageNum, Integer pageSize, String name, Long userId);
}

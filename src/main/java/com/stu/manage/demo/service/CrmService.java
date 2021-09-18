package com.stu.manage.demo.service;

import com.aliyun.dingtalkcrm_1_0.models.GetOfficialAccountContactsResponse;
import com.stu.manage.demo.entity.CrmEntity;

import java.util.HashSet;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.service
 * @desc
 * @date 2021/9/16 14:35
 * @Copyright © 河南交科院
 */
public interface CrmService {

    /**
     * 判断人员是否已获取
     * @param instanceId
     * @return
     */
    CrmEntity checkExist(String instanceId);

    /**
     * 获取crm数据
     * @param tokenUrl
     * @param appKey
     * @param appsecret
     * @param nextToken
     * @return
     * @throws Exception
     */
    GetOfficialAccountContactsResponse getCustomer(String tokenUrl, String appKey, String appsecret, String nextToken) throws Exception;

    /**
     * 获取当前拉取到crm数据最大的序号
     * @return
     */
    String getMaxIndex();

    /**
     * 批量保存拉取的crm数据
     * @param list
     */
    void BatchSave(List<CrmEntity> list);
}

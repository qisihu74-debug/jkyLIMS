package com.stu.manage.demo.service.impl;

import com.aliyun.dingtalkcrm_1_0.models.GetOfficialAccountContactsResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stu.manage.demo.entity.CrmEntity;
import com.stu.manage.demo.mapper.CrmMapper;
import com.stu.manage.demo.service.CrmService;
import com.stu.manage.demo.util.AccessTokenSingleton;
import com.stu.manage.demo.util.DingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.service.impl
 * @desc
 * @date 2021/9/16 14:35
 * @Copyright © 河南交科院
 */
@Service
public class CrmServiceImpl implements CrmService {
    Logger logger = LoggerFactory.getLogger(CrmServiceImpl.class);
    @Autowired
    private CrmMapper crmMapper;

    @Override
    public CrmEntity checkExist(String userId) {
        QueryWrapper<CrmEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return crmMapper.selectOne(queryWrapper);
    }

    @Override
    public GetOfficialAccountContactsResponse getCustomer(String tokenUrl, String appKey, String appsecret, String nextToken) throws Exception {
        String token = AccessTokenSingleton.getInstance().getToken(tokenUrl, appKey, appsecret);
        GetOfficialAccountContactsResponse response = DingUtils.getServiceWindow(token, nextToken);
        return response;
    }

    @Override
    public String getMaxIndex() {
        return crmMapper.getMaxIndex();
    }

    @Override
    public void BatchSave(List<CrmEntity> list) {
        try {
            crmMapper.BatchSave(list);
        }catch (Exception e){
            logger.error("crm数据入库失败:{}",e);
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void authorize(String userId) {
        //TODO
    }
}

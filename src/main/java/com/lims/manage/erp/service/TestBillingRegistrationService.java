package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestBillingRegistrationEntity;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2024/1/25 11:26
 */
public interface TestBillingRegistrationService extends IService<TestBillingRegistrationEntity> {

    Result list(TestBillingRegistrationEntity registrationEntity);

    /**
     * 更新
     *
     * @param registrationEntity
     * @return
     */
    Result update(TestBillingRegistrationEntity registrationEntity);


    /**
     * 批量新增
     *
     * @param list
     * @return
     */
    Result batchAdd(List<TestBillingRegistrationEntity> list);
}

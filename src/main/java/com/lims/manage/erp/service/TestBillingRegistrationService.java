package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.TestBillingRegistrationEntity;
import com.lims.manage.erp.result.Result;

/**
 * @Author: DLC
 * @Date: 2024/1/25 11:26
 */
public interface TestBillingRegistrationService {

    Result list(TestBillingRegistrationEntity registrationEntity);

    /**
     * 更新
     *
     * @param registrationEntity
     * @return
     */
    Result update(TestBillingRegistrationEntity registrationEntity);
}

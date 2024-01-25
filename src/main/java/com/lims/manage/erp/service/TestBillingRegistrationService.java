package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestBillingRegistrationEntity;
import com.lims.manage.erp.result.Result;

/**
 * @Author: DLC
 * @Date: 2024/1/25 11:26
 */
public interface TestBillingRegistrationService {

    Result list(TestBillingRegistrationEntity registrationEntity);
}

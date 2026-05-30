package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.EntrustRemittanceRegistrationEntity;
import com.lims.manage.erp.entity.TestCompanyEntity;
import com.lims.manage.erp.entity.TestCustomerEntity;
import com.lims.manage.erp.result.Result;

/**
 * @Author: DLC
 * @Date: 2024/3/11 14:52
 */
public interface TestCompanyService {

    /**
     * 委托单位搜索查询
     *
     * @param entity
     * @return
     */
    Result searchCompanyInformation(TestCompanyEntity entity);

    /**
     * 通过 companyId 获取委托单的信息。
     *
     * @param entity
     * @return
     */
    Result searchEntrustList(TestCustomerEntity entity);

    /**
     * 通过 companyId 获取委托单 实际应收总金额
     *
     * @param entity
     * @return
     */
    Result searchEntrusTotalMoney(TestCustomerEntity entity);

    /**
     * 委托单位新增
     *
     * @param entity
     * @return
     */
    Result addCompany(TestCompanyEntity entity);

    /**
     * 添加联系人
     *
     * @param entity
     * @return
     */
    Result addContacts(TestCustomerEntity entity);

    /**
     * 新增回款登记
     *
     * @param entity
     * @return
     */
    Result addRegistrationEntity(EntrustRemittanceRegistrationEntity entity);
}

package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestProductCommitteeEntity;
import com.lims.manage.erp.result.Result;


/**
 * 产品委员会业务层
 */
public interface TestProductCommitteeService extends IService<TestProductCommitteeEntity> {


    Result addProductCommittee(TestProductCommitteeEntity productCommittee);

    Result delProductCommittee(String councilId);
}

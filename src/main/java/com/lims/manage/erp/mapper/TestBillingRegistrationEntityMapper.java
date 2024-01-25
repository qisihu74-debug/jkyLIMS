package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestBillingRegistrationEntity;

public interface TestBillingRegistrationEntityMapper extends BaseMapper<TestBillingRegistrationEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(TestBillingRegistrationEntity record);

    int insertSelective(TestBillingRegistrationEntity record);

    TestBillingRegistrationEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestBillingRegistrationEntity record);

    int updateByPrimaryKey(TestBillingRegistrationEntity record);
}
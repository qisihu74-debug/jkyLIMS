package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.TestSampleMixInfoEntity;

public interface TestSampleMixInfoEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TestSampleMixInfoEntity record);

    int insertSelective(TestSampleMixInfoEntity record);

    TestSampleMixInfoEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestSampleMixInfoEntity record);

    int updateByPrimaryKey(TestSampleMixInfoEntity record);

}
package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestControlledDocumentsEntity;

public interface TestControlledDocumentsMapper extends BaseMapper<TestControlledDocumentsEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(TestControlledDocumentsEntity record);

    int insertSelective(TestControlledDocumentsEntity record);

    TestControlledDocumentsEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestControlledDocumentsEntity record);

    int updateByPrimaryKeyWithBLOBs(TestControlledDocumentsEntity record);

    int updateByPrimaryKey(TestControlledDocumentsEntity record);
}
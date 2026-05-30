package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestControlledDocumentsRecordEntity;

public interface TestControlledDocumentsRecordMapper extends BaseMapper<TestControlledDocumentsRecordEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(TestControlledDocumentsRecordEntity record);

    int insertSelective(TestControlledDocumentsRecordEntity record);

    TestControlledDocumentsRecordEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestControlledDocumentsRecordEntity record);

    int updateByPrimaryKeyWithBLOBs(TestControlledDocumentsRecordEntity record);

    int updateByPrimaryKey(TestControlledDocumentsRecordEntity record);
}
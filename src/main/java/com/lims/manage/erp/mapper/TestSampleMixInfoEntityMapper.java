package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.TestSampleMixInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface TestSampleMixInfoEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TestSampleMixInfoEntity record);

    int insertSelective(TestSampleMixInfoEntity record);

    TestSampleMixInfoEntity selectByPrimaryKey(Integer id);

    TestSampleMixInfoEntity selectBySampleId(Integer sampleId);

    int updateByPrimaryKeySelective(TestSampleMixInfoEntity record);

    int updateByPrimaryKey(TestSampleMixInfoEntity record);

    int updateBySampleId(TestSampleMixInfoEntity record);

    TestSampleMixInfoEntity selectByEntrustId(Long entrustId);

}
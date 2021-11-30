package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SampleEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@Mapper
public interface SampleEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SampleEntity record);

    int insertSelective(SampleEntity record);

    SampleEntity selectByPrimaryKey(Integer id);

    List<SampleEntity> selectSampleList(SampleEntity record);

    int updateByPrimaryKeySelective(SampleEntity record);

    int updateByPrimaryKey(SampleEntity record);
}
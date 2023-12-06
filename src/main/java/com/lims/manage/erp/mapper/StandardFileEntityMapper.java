package com.lims.manage.erp.mapper;


import com.lims.manage.erp.entity.StandardFileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface StandardFileEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(StandardFileEntity record);

    int insertRecord(StandardFileEntity record);

    int insertSelective(StandardFileEntity record);

    StandardFileEntity selectByPrimaryKey(Integer id);

    StandardFileEntity getDetail(Integer id);

    int updateByPrimaryKeySelective(StandardFileEntity record);

    int updateByPrimaryKeyWithBLOBs(StandardFileEntity record);

    int updateByPrimaryKey(StandardFileEntity record);

    int getMaxId();

    List<StandardFileEntity> getRecords(Integer pid);
}
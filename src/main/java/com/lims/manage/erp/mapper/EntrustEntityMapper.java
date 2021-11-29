package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.EntrustEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface EntrustEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(EntrustEntity record);

    int insertSelective(EntrustEntity record);

    EntrustEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(EntrustEntity record);

    int updateByPrimaryKey(EntrustEntity record);
}
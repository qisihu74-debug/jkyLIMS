package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.DeclarationParamEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface DeclarationParamEntityMapper {
    int insert(DeclarationParamEntity record);

    int insertSelective(DeclarationParamEntity record);
}

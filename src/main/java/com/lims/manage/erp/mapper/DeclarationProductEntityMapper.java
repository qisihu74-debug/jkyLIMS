package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.DeclarationProductEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface DeclarationProductEntityMapper {
    int insert(DeclarationProductEntity record);

    int insertSelective(DeclarationProductEntity record);
}

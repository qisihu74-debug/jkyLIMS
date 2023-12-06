package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ItemOriginalRecordTemplateRel;

public interface ItemOriginalRecordTemplateMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ItemOriginalRecordTemplateRel record);

    int insertSelective(ItemOriginalRecordTemplateRel record);

    ItemOriginalRecordTemplateRel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ItemOriginalRecordTemplateRel record);

    int updateByPrimaryKey(ItemOriginalRecordTemplateRel record);
}
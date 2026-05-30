package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.ManageReviewInformationEntity;

public interface ManageReviewInformationEntityMapper extends BaseMapper<ManageReviewInformationEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(ManageReviewInformationEntity record);

    int insertSelective(ManageReviewInformationEntity record);

    ManageReviewInformationEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ManageReviewInformationEntity record);

    int updateByPrimaryKey(ManageReviewInformationEntity record);
}
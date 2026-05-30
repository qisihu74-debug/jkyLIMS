package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.ManageReviewPlanEntity;

public interface ManageReviewPlanEntityMapper extends BaseMapper<ManageReviewPlanEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(ManageReviewPlanEntity record);

    int insertSelective(ManageReviewPlanEntity record);

    ManageReviewPlanEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ManageReviewPlanEntity record);

    int updateByPrimaryKey(ManageReviewPlanEntity record);
}
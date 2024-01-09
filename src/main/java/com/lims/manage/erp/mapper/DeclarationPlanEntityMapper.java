package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.DeclarationPlanEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface DeclarationPlanEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(DeclarationPlanEntity record);

    int insertSelective(DeclarationPlanEntity record);

    DeclarationPlanEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeclarationPlanEntity record);

    int updateByPrimaryKey(DeclarationPlanEntity record);

    /**
     * 修改申报计划删除状态
     * @param planId
     * @return
     */
    int updateDelete(Long planId);

    /**
     * 修改参数申报计划
     * @param record
     * @return
     */
    int updatePlan(DeclarationPlanEntity record);

    List<DeclarationPlanEntity> getPlanList();
}

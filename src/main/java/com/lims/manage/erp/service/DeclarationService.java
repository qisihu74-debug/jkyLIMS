package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.DeclarationPlanEntity;
import com.lims.manage.erp.result.Result;

public interface DeclarationService {
    /**
     * 新增申报计划
     * @param planEntity
     * @return
     */
    Result addPlan(DeclarationPlanEntity planEntity);

    /**
     * 删除参数申报计划
     * @param planId
     * @return
     */
    Result deletePlan(Long planId);

    /**
     * 编辑参数申报计划
     * @param planEntity
     * @return
     */
    Result updatePlan(DeclarationPlanEntity planEntity);

    Result getPlanList(DeclarationPlanEntity planEntity);
}

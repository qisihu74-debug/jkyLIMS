package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.DeclarationPlanEntity;
import com.lims.manage.erp.entity.ReserveCodeEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.service.DeclarationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/declaration/")
public class DeclarationController {
    @Autowired
    private DeclarationService declarationService;

    /**
     * 新增申报计划
     *
     * @param planEntity
     * @return
     */
    @PostMapping("/addPlan")
    public Result addPlan(@RequestBody DeclarationPlanEntity planEntity) {
        return this.declarationService.addPlan(planEntity);
    }

    /**
     * 删除参数申报计划
     *
     * @param planId
     * @return
     */
    @GetMapping("/deletePlan")
    public Result deletePlan(Long planId) {
        return this.declarationService.deletePlan(planId);
    }

    /**
     * 编辑参数申报计划
     * @param planEntity
     * @return
     */
    @PostMapping("/updatePlan")
    public Result updatePlan(@RequestBody DeclarationPlanEntity planEntity) {
        return this.declarationService.updatePlan(planEntity);
    }

    @PostMapping("/getPlanList")
    public Result getPlanList(@RequestBody DeclarationPlanEntity planEntity) {
        return this.declarationService.getPlanList(planEntity);
    }
}

package com.lims.manage.erp.controller;

import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.DeclarationItemEntity;
import com.lims.manage.erp.entity.DeclarationParamEntity;
import com.lims.manage.erp.entity.DeclarationPlanEntity;
import com.lims.manage.erp.entity.DeclarationProductEntity;
import com.lims.manage.erp.enums.BusinessType;
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
    @Log(title = "新增申报计划", businessType = BusinessType.INSERT)
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
    @Log(title = "删除参数申报计划", businessType = BusinessType.DELETE)
    @GetMapping("/deletePlan")
    public Result deletePlan(Long planId) {
        return this.declarationService.deletePlan(planId);
    }

    /**
     * 编辑参数申报计划
     * @param planEntity
     * @return
     */
    @Log(title = "编辑参数申报计划", businessType = BusinessType.UPDATE)
    @PostMapping("/updatePlan")
    public Result updatePlan(@RequestBody DeclarationPlanEntity planEntity) {
        return this.declarationService.updatePlan(planEntity);
    }

    /**
     * 查询参数申报列表
     * @param planEntity
     * @return
     */
    @PostMapping("/getPlanList")
    public Result getPlanList(@RequestBody DeclarationPlanEntity planEntity) {
        return this.declarationService.getPlanList(planEntity);
    }

    /**
     * 查询产品类别下拉列表
     * @return
     */
    @GetMapping("/getProductType")
    public Result getProductType() {
        return this.declarationService.getProductType();
    }

    /**
     * 新增参数申报产品
     * @param productEntity
     * @return
     */
    @Log(title = "新增参数申报产品", businessType = BusinessType.INSERT)
    @PostMapping("/addProduct")
    public Result addProduct(@RequestBody DeclarationProductEntity productEntity) {
        return this.declarationService.addProduct(productEntity);
    }

    /**
     * 删除参数申报计划下的产品
     * @param productEntity
     * @return
     */
    @Log(title = "删除参数申报计划下的产品", businessType = BusinessType.DELETE)
    @PostMapping("/deleteProduct")
    public Result deleteProduct(@RequestBody DeclarationProductEntity productEntity) {
        return this.declarationService.deleteProduct(productEntity);
    }

    /**
     * 修改参数申报计划下的产品
     * @param productEntity
     * @return
     */
    @Log(title = "修改参数申报计划下的产品", businessType = BusinessType.UPDATE)
    @PostMapping("/updateProduct")
    public Result updateProduct(@RequestBody DeclarationProductEntity productEntity) {
        return this.declarationService.updateProduct(productEntity);
    }

    /**
     * 查询申报计划下的产品列表
     * @return
     */
    @PostMapping("/getProductList")
    public Result getProductList(@RequestBody DeclarationProductEntity productEntity) {
        return this.declarationService.getProductList(productEntity);
    }

    /**
     * 查询依据标准下拉列表
     * @return
     */
    @GetMapping("/getStandard")
    public Result getStandard(String standard) {
        return this.declarationService.getStandard(standard);
    }

    /**
     * 查询依据标准方法下拉列表
     * @return
     */
    @GetMapping("/getMethod")
    public Result getMethod(Long standardId,String method) {
        return this.declarationService.getMethod(standardId,method);
    }

    /**
     * 查询仪器设备下拉列表
     * @param equipment
     * @return
     */
    @GetMapping("/getEquipment")
    public Result getEquipment(String equipment) {
        return this.declarationService.getEquipment(equipment);
    }

    /**
     * 查询产品检测项下拉列表
     * @param productId
     * @return
     */
    @GetMapping("/getCheckItemList")
    public Result getCheckItemList(Long productId) {
        return this.declarationService.getCheckItemList(productId);
    }

    /**
     * 新增申报参数
     * @param itemEntity
     * @return
     */
    @Log(title = "新增申报参数", businessType = BusinessType.INSERT)
    @PostMapping("/addParam")
    public Result addParam(@RequestBody DeclarationItemEntity itemEntity) {
        return this.declarationService.addParam(itemEntity);
    }
    @PostMapping("/addParamOld")
    public Result addParamOld(@RequestBody DeclarationParamEntity paramEntity) {
        return this.declarationService.addParamOld(paramEntity);
    }

    /**
     * 删除参数申报计划下的产品参数
     * @param paramEntity
     * @return
     */
    @Log(title = "删除参数申报计划下的产品参数", businessType = BusinessType.DELETE)
    @PostMapping("/deleteParam")
    public Result deleteParam(@RequestBody DeclarationItemEntity paramEntity) {
        return this.declarationService.deleteParam(paramEntity);
    }

    /**
     * 修改参数申报计划下的产品参数
     * @param paramEntity
     * @return
     */
    @Log(title = "修改参数申报计划下的产品参数", businessType = BusinessType.UPDATE)
    @PostMapping("/updateParam")
    public Result updateParam(@RequestBody DeclarationItemEntity paramEntity) {
        return this.declarationService.updateParam(paramEntity);
    }

    /**
     * 查询申报参数列表
     * @param paramEntity
     * @return
     */
    @PostMapping("/getParamList")
    public Result getParamList(@RequestBody DeclarationItemEntity paramEntity) {
        return this.declarationService.getParamList(paramEntity);
    }

    /**
     * 新增检测项检测依据
     * @param paramEntity
     * @return
     */
    @Log(title = "新增检测项检测依据", businessType = BusinessType.INSERT)
    @PostMapping("/addParamStandard")
    public Result addParamStandard(@RequestBody DeclarationParamEntity paramEntity) {
        return this.declarationService.addParamStandard(paramEntity);
    }

    /**
     * 删除参数申报参数检测依据
     * @param paramEntity
     * @return
     */
    @Log(title = "删除参数申报参数检测依据", businessType = BusinessType.DELETE)
    @PostMapping("/deleteParamStandard")
    public Result deleteParamStandard(@RequestBody DeclarationParamEntity paramEntity) {
        return this.declarationService.deleteParamStandard(paramEntity);
    }

    /**
     * 查询申报参数详情
     * @param paramEntity
     * @return
     */
    @PostMapping("/getParamDetail")
    public Result getParamDetail(@RequestBody DeclarationItemEntity paramEntity) {
        return this.declarationService.getParamDetail(paramEntity);
    }

    /**
     * 查询产品下拉列表
     * @param productTypeId
     * @return
     */
    @GetMapping("/getProductListSelect")
    public Result getProductListSelect(Integer productTypeId) {
        return this.declarationService.getProductListSelect(productTypeId);
    }

    /**
     * 查询申报参数详情成功
     * @param itemEntity
     * @return
     */
    @PostMapping("/getParamDetailInfo")
    public Result getParamDetailInfo(@RequestBody DeclarationItemEntity itemEntity) {
        return this.declarationService.getParamDetailInfo(itemEntity);
    }
}

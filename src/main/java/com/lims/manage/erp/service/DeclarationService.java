package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.DeclarationItemEntity;
import com.lims.manage.erp.entity.DeclarationParamEntity;
import com.lims.manage.erp.entity.DeclarationPlanEntity;
import com.lims.manage.erp.entity.DeclarationProductEntity;
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

    /**
     * 查询参数申报列表
     * @param planEntity
     * @return
     */
    Result getPlanList(DeclarationPlanEntity planEntity);

    /**
     * 查询产品类别下拉列表
     * @return
     */
    Result getProductType();

    /**
     * 新增参数申报产品
     * @param productEntity
     * @return
     */
    Result addProduct(DeclarationProductEntity productEntity);

    /**
     * 删除参数申报计划下的产品
     * @param productEntity
     * @return
     */
    Result deleteProduct(DeclarationProductEntity productEntity);

    /**
     * 修改参数申报计划下的产品
     * @param productEntity
     * @return
     */
    Result updateProduct(DeclarationProductEntity productEntity);

    /**
     * 查询申报计划下的产品列表
     * @param productEntity
     * @return
     */
    Result getProductList(DeclarationProductEntity productEntity);

    /**
     * 查询依据标准下拉列表
     * @param standard
     * @return
     */
    Result getStandard(String standard);

    /**
     * 查询检测方法下拉列表
     * @param standardId
     * @param method
     * @return
     */
    Result getMethod(Long standardId,String method);

    /**
     * 查询仪器设备下拉列表
     * @param equipment
     * @return
     */
    Result getEquipment(String equipment);

    /**
     * 新增申报参数
     * @param itemEntity
     * @return
     */
    Result addParam(DeclarationItemEntity itemEntity);
    Result addParamOld(DeclarationParamEntity paramEntity);

    /**
     * 删除参数申报计划下的产品参数
     * @param paramEntity
     * @return
     */
    Result deleteParam(DeclarationItemEntity paramEntity);

    /**
     * 修改参数申报计划下的产品参数
     * @param paramEntity
     * @return
     */
    Result updateParam(DeclarationItemEntity paramEntity);

    /**
     * 查询申报参数列表
     * @param paramEntity
     * @return
     */
    Result getParamList(DeclarationItemEntity paramEntity);

    /**
     * 添加检测项检测依据
     * @param paramEntity
     * @return
     */
    Result addParamStandard(DeclarationParamEntity paramEntity);

    /**
     * 删除参数申报参数检测依据
     * @param paramEntity
     * @return
     */
    Result deleteParamStandard(DeclarationParamEntity paramEntity);

    /**
     * 查询申报参数详情
     * @param paramEntity
     * @return
     */
    Result getParamDetail(DeclarationItemEntity paramEntity);

    Result getParamDetailInfo(DeclarationItemEntity itemEntity);

    /**
     * 查询产品下拉列表
     * @param productTypeId
     * @return
     */
    Result getProductListSelect(Integer productTypeId);

    /**
     * 查询产品检测项下拉列表
     * @param productId
     * @return
     */
    Result getCheckItemList(Long productId);
}

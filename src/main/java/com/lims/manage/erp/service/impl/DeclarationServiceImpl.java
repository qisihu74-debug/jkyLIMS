package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.DeclarationParamEntity;
import com.lims.manage.erp.entity.DeclarationPlanEntity;
import com.lims.manage.erp.entity.DeclarationProductEntity;
import com.lims.manage.erp.mapper.DeclarationParamEntityMapper;
import com.lims.manage.erp.mapper.DeclarationPlanEntityMapper;
import com.lims.manage.erp.mapper.DeclarationProductEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DeclarationService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.LabelValueVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class DeclarationServiceImpl implements DeclarationService {
    @Autowired
    private DeclarationPlanEntityMapper planEntityMapper;
    @Autowired
    private DeclarationProductEntityMapper productEntityMapper;
    @Autowired
    private DeclarationParamEntityMapper paramEntityMapper;

    @Override
    public Result addPlan(DeclarationPlanEntity planEntity) {
        planEntity.setId(GenID.getID());
        planEntity.setOperateUser(ShiroUtils.getUserInfo().getUsername());
        planEntity.setOperateDate(new Date());
        planEntity.setState("待开始");
        planEntity.setIsDel(0);
        planEntityMapper.insert(planEntity);
        return ResultUtil.success("新增参数申报计划成功！",null);
    }

    @Override
    public Result deletePlan(Long planId) {
        planEntityMapper.updateDelete(planId);
        return ResultUtil.success("删除参数申报计划成功！",null);
    }

    @Override
    public Result updatePlan(DeclarationPlanEntity planEntity) {
        if(planEntity.getId() == null){
            return ResultUtil.error("请选择要修改的申报计划！");
        }
        planEntityMapper.updatePlan(planEntity);
        return ResultUtil.success("修改参数申报计划成功！",null);
    }

    @Override
    public Result getPlanList(DeclarationPlanEntity planEntity) {
        if(planEntity.getPageNum() == null || planEntity.getPageSize() == null){
            return ResultUtil.error("缺少分页参数！");
        }
        PageHelper.startPage(planEntity.getPageNum(),planEntity.getPageSize());
        List<DeclarationPlanEntity> planList = planEntityMapper.getPlanList(planEntity);
        PageInfo<DeclarationPlanEntity> pageInfo = new PageInfo<>(planList);
        return ResultUtil.success("查询参数申报列表成功！",pageInfo);
    }


    @Override
    public Result getProductType() {
        List<LabelValueVo> productType = productEntityMapper.getProductType();
        return ResultUtil.success("查询产品类别下拉列表成功！",productType);
    }

    @Override
    public Result addProduct(DeclarationProductEntity productEntity) {
        Long productId = productEntity.getProductId();
        if(productId == null){//产品ID为空，说明为新增产品
            productEntity.setAttribute("新增");
            long newProductId = GenID.getID();
            productEntity.setProductId(newProductId);
        }else{//校验计划下产品是否存在
            DeclarationProductEntity declarationProductEntity = productEntityMapper.checkProduct(productEntity);
            if(declarationProductEntity != null){
                return ResultUtil.error("当前计划下已经存在此产品！");
            }
            productEntity.setAttribute("扩项");
        }
        String username = ShiroUtils.getUserInfo().getUsername();
        productEntity.setCreateUser(username);
        productEntity.setCreateTime(new Date());
        productEntityMapper.insert(productEntity);
        return ResultUtil.success("新增参数申报产品成功！",null);
    }

    @Override
    public Result deleteProduct(DeclarationProductEntity productEntity) {
        if(productEntity.getProductId() == null || productEntity.getPlanId() == null){
            return ResultUtil.error("请选择要删除的产品！");
        }
        productEntityMapper.deleteProduct(productEntity);
        return ResultUtil.success("删除参数申报计划下的产品成功！",null);
    }

    @Override
    public Result updateProduct(DeclarationProductEntity productEntity) {
        if(productEntity.getProductId() == null || productEntity.getPlanId() == null){
            return ResultUtil.error("请选择要编辑的产品！");
        }
        productEntityMapper.updateProduct(productEntity);
        return ResultUtil.success("修改参数申报计划下的产品成功！",null);
    }

    @Override
    public Result getProductList(DeclarationProductEntity productEntity) {
        if(productEntity.getPageNum() == null || productEntity.getPageSize() == null){
            return ResultUtil.error("缺少分页参数！");
        }
        PageHelper.startPage(productEntity.getPageNum(),productEntity.getPageSize());
        List<DeclarationProductEntity> productList = productEntityMapper.getProductList(productEntity);
        PageInfo<DeclarationProductEntity> pageInfo = new PageInfo<>(productList);
        return ResultUtil.success("查询申报计划下的产品列表成功！",pageInfo);
    }

    @Override
    public Result getStandard(String standard) {
        List<LabelValueVo> standardList = paramEntityMapper.getStandard(standard);
        return ResultUtil.success("查询依据标准下拉列表成功！",standardList);
    }

    @Override
    public Result getMethod(Long standardId,String method) {
        if(standardId == null){
            return ResultUtil.error("请先选择依据标准！");
        }
        List<LabelValueVo> methodList = paramEntityMapper.getMethod(standardId,method);
        return ResultUtil.success("查询检测方法下拉列表成功！",methodList);
    }

    @Override
    public Result getEquipment(String equipment) {
        List<LabelValueVo> equipmentList = paramEntityMapper.getEquipment(equipment);
        return ResultUtil.success("查询仪器设备下拉列表成功！",equipmentList);
    }

    @Override
    public Result addParam(DeclarationParamEntity paramEntity) {
        if(paramEntity.getPlanId() == null || paramEntity.getProductId() == null){
            return ResultUtil.error("请先选择计划和产品信息！");
        }
        Long checkItemId = paramEntity.getCheckItemId();
        if(checkItemId == null){//为新增的检测项
            paramEntity.setAttribute("新增");
            long newParamId = GenID.getID();
            paramEntity.setCheckItemId(newParamId);
        }else{
            DeclarationParamEntity declarationParamEntity = paramEntityMapper.checkParam(paramEntity);
            if(declarationParamEntity != null){
                return ResultUtil.error("当前计划下的产品已经存在此检测参数！");
            }
            paramEntity.setAttribute("扩项");
        }
        String username = ShiroUtils.getUserInfo().getUsername();
        paramEntity.setCreateUser(username);
        paramEntity.setCreateTime(new Date());
        paramEntityMapper.insert(paramEntity);
        return ResultUtil.success("新增参数申报检测参数成功！",null);
    }

    @Override
    public Result deleteProduct(DeclarationParamEntity paramEntity) {
        if(paramEntity.getProductId() == null || paramEntity.getPlanId() == null
                || paramEntity.getCheckItemId() == null){
            return ResultUtil.error("请选择要删除的参数！");
        }
        paramEntityMapper.deleteParam(paramEntity);
        return ResultUtil.success("删除参数申报计划下的产品参数成功！",null);
    }

    @Override
    public Result updateParam(DeclarationParamEntity paramEntity) {
        if(paramEntity.getProductId() == null || paramEntity.getPlanId() == null
                || paramEntity.getCheckItemId() == null){
            return ResultUtil.error("请选择要编辑的参数！");
        }
//        paramEntityMapper.updateParam(paramEntity);
        return ResultUtil.success("修改参数申报计划下的产品参数成功！",null);
    }

    @Override
    public Result getParamList(DeclarationParamEntity paramEntity) {
        if(paramEntity.getPageNum() == null || paramEntity.getPageSize() == null){
            return ResultUtil.error("缺少分页参数！");
        }
        PageHelper.startPage(paramEntity.getPageNum(),paramEntity.getPageSize());
        List<DeclarationParamEntity> productList = paramEntityMapper.getParamList(paramEntity);
        PageInfo<DeclarationParamEntity> pageInfo = new PageInfo<>(productList);
        return ResultUtil.success("查询申报计划下的产品参数列表成功！",pageInfo);
    }

    @Override
    public Result addParamStandard(DeclarationParamEntity paramEntity) {
        if(paramEntity.getPlanId() == null || paramEntity.getProductId() == null || paramEntity.getCheckItemId() == null){
            return ResultUtil.error("请先选择申报参数！");
        }
        DeclarationParamEntity declarationParamEntity = paramEntityMapper.checkParam(paramEntity);
        paramEntity.setAttribute(declarationParamEntity.getAttribute());//填报属性
        paramEntity.setCreateUser(declarationParamEntity.getCreateUser());//创建人
        paramEntity.setCreateTime(declarationParamEntity.getCreateTime());//创建时间
        paramEntityMapper.insert(paramEntity);
        return ResultUtil.success("新增检测项检测依据成功！",null);
    }

    @Override
    public Result deleteParamStandard(DeclarationParamEntity paramEntity) {
        if(paramEntity.getProductId() == null || paramEntity.getPlanId() == null
                || paramEntity.getCheckItemId() == null || paramEntity.getStandardId() == null){
            return ResultUtil.error("请选择要删除的参数检测依据！");
        }
        paramEntityMapper.deleteParamStandard(paramEntity);
        return ResultUtil.success("删除参数申报参数检测依据成功！",null);
    }

    @Override
    public Result getParamDetail(DeclarationParamEntity paramEntity) {
        if(paramEntity.getPlanId() == null || paramEntity.getProductId() == null || paramEntity.getCheckItemId() == null){
            return ResultUtil.error("请先选择申报参数！");
        }
        List<DeclarationParamEntity> paramDetail = paramEntityMapper.getParamDetail(paramEntity);
        return ResultUtil.success("查询申报参数详情成功！",paramDetail);
    }
}

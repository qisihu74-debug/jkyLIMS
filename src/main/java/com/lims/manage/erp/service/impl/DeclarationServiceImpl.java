package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.lims.manage.erp.entity.DeclarationPlanEntity;
import com.lims.manage.erp.mapper.DeclarationParamEntityMapper;
import com.lims.manage.erp.mapper.DeclarationPlanEntityMapper;
import com.lims.manage.erp.mapper.DeclarationProductEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DeclarationService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

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

        return null;
    }


}

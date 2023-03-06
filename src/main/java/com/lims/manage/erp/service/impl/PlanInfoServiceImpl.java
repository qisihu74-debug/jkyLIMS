package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.constant.CommonConstant;
import com.lims.manage.erp.entity.DataInfo;
import com.lims.manage.erp.entity.PlanInfo;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import com.lims.manage.erp.entity.UserPlanInfo;
import com.lims.manage.erp.mapper.PlanInfoDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.PlanInfoService;
import com.lims.manage.erp.service.SysUserRoleService;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.service.UserPlanInfoService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.PlanInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 计划信息业务层实现类
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class PlanInfoServiceImpl extends ServiceImpl<PlanInfoDao, PlanInfo> implements PlanInfoService {

    @Resource
    UserPlanInfoService userPlanInfoService;

    @Resource
    SysUserRoleService sysUserRoleService;

    @Override
    public IPage<PlanInfoVo> pageList(Page<PlanInfo> page, PlanInfo planInfo) {
        planInfo.setPartakeUserId(ShiroUtils.getUserInfo().getUserId().toString());
        return baseMapper.pageList(page, planInfo);
    }

    @Override
    public Result<?> addPlanInfo(PlanInfo planInfo, MultipartFile file) {
        //根据用户角色进行判断
        SysUserRoleEntity sysUserRoleEntity = sysUserRoleService.getOne(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getUserId, ShiroUtils.getUserInfo().getUserId()).eq(SysUserRoleEntity::getRoleId, CommonConstant.USER_ROLE_PLAN));
        if (sysUserRoleEntity == null) {
            return ResultUtil.error(500, "该用户没有新建权限");
        }
        //附件存在上传附件到服务器
        if (file != null) {
            String name = file.getOriginalFilename();
            String[] strings = name.split("\\.");
            String upload = MinIoUtil.upload(BucketsConst.file_syn, file, GenID.getID() + "." + strings[strings.length - 1]);
            if (!StringUtils.isEmpty(upload)) {
                planInfo.setEnclosureUrl(upload);
            } else {
                return ResultUtil.error("上传附件保存出错");
            }
        }
        //planInfo.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
        planInfo.setCreateBy(ShiroUtils.getUserInfo().getUsername());
        planInfo.setCreateTime(new Date());
        this.save(planInfo);
        return ResultUtil.success("创建考试/培训计划成功");
    }

    @Override
    public PlanInfoVo getPlanInfoDetail(String planId) {
        return baseMapper.getPlanInfoDetail(planId);
    }

    @Override
    public Result<?> enrollPlanInfo(String planId) {
        //根据计划id查询计划信息
        PlanInfo planInfo = this.getById(planId);
        if (planInfo != null) {
            //根据当前用户id和计划id查询用户是否已经报名过
            LambdaQueryWrapper<UserPlanInfo> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(UserPlanInfo::getUserId, ShiroUtils.getUserInfo().getUserId().toString());
            wrapper.eq(UserPlanInfo::getPlanId, planId);
            UserPlanInfo userPlanInfo = userPlanInfoService.getOne(wrapper);
            if (userPlanInfo != null) {
                return ResultUtil.error("该计划已报名");
            }
            //根据计划的开始时间判断当前是否可以报名
            if (planInfo.getPlanBeginTime().after(new Date())) {
                userPlanInfo = new UserPlanInfo();
                userPlanInfo.setPlanId(planId);
                userPlanInfo.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
                userPlanInfo.setEnrollTime(new Date());
                userPlanInfo.setPartakeStatus(CommonConstant.PLAN_PARTAKE_STATUS_ENROLL);
                userPlanInfo.setCreateBy(ShiroUtils.getUserInfo().getUsername());
                userPlanInfo.setCreateTime(new Date());
                userPlanInfoService.save(userPlanInfo);
            } else {
                return ResultUtil.error("当前计划暂无法报名");
            }
        } else {
            return ResultUtil.error("计划未找到");
        }
        return ResultUtil.success("报名成功");
    }

    @Override
    public Result<?> delPlanInfo(String planId) {
        //根据用户角色进行判断
        SysUserRoleEntity sysUserRoleEntity = sysUserRoleService.getOne(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getUserId, ShiroUtils.getUserInfo().getUserId()).eq(SysUserRoleEntity::getRoleId, CommonConstant.USER_ROLE_PROBLEM_CLASSIFICATION));
        if (sysUserRoleEntity == null) {
            return ResultUtil.error(500, "该用户没有计划删除权限");
        }
        //根据计划id获取计划信息
        PlanInfo planInfo = this.getById(planId);
        if (planInfo != null) {
            //如果当前计划不是当前用户创建的则无法删除
            // if(ShiroUtils.getUserInfo().getUserId().toString().equals(planInfo.getUserId())){
            //如果计划已经开始则无法删除
            if (planInfo.getPlanBeginTime().after(new Date())) {
                //如果有附件地址
                if (!StringUtils.isEmpty(planInfo.getEnclosureUrl())) {
                    String fileName = planInfo.getEnclosureUrl().substring(planInfo.getEnclosureUrl().lastIndexOf("/") + 1, planInfo.getEnclosureUrl().lastIndexOf("?"));
                    MinIoUtil.deleteFile(BucketsConst.file_syn, fileName);
                }
                this.removeById(planId);
                return ResultUtil.success("计划信息删除成功");
            } else {
                return ResultUtil.error(500, "计划开始无法删除");
            }
            /*}else{
                return ResultUtil.error(500,"没有删除权限");
            }*/

        } else {
            return ResultUtil.error(500, "没有找到对应计划信息");
        }
    }
}

package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.UserPlanInfo;

import java.util.List;

/**
 * 用户参加计划业务层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface UserPlanInfoService extends IService<UserPlanInfo> {

    /**
     * 获取计划报名的用户信息
     * @param planId 计划id
     * @return List<SysUserEntity>
     */
    List<SysUserEntity> getPlanUserInfo(String planId);
}

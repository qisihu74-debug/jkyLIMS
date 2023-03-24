package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.UserPlanInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户参加计划信息dao层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface UserPlanInfoDao extends BaseMapper<UserPlanInfo> {

    /**
     * 获取计划报名的用户信息
     * @param planId 计划id
     * @return List<SysUserEntity>
     */
    List<SysUserEntity> getPlanUserInfo(@Param("planId")String planId);
}

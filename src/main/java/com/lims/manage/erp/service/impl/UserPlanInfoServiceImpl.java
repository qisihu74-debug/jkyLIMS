package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.UserPlanInfo;
import com.lims.manage.erp.mapper.UserPlanInfoDao;
import com.lims.manage.erp.service.UserPlanInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户参加计划信息业务层实现类
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class UserPlanInfoServiceImpl extends ServiceImpl<UserPlanInfoDao, UserPlanInfo> implements UserPlanInfoService {

    @Override
    public List<SysUserEntity> getPlanUserInfo(String planId) {
        return baseMapper.getPlanUserInfo(planId);
    }
}

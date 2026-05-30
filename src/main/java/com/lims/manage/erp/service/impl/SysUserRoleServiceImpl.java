package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import com.lims.manage.erp.mapper.SysUserRoleDao;
import com.lims.manage.erp.service.SysUserRoleService;
import org.springframework.stereotype.Service;

/**
 * @Description 用户与角色业务实现
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
@Service("sysUserRoleService")
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleDao, SysUserRoleEntity> implements SysUserRoleService {

}
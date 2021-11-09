package com.lims.manage.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.demo.entity.SysMenuEntity;

import java.util.List;

/**
 * @Description 权限DAO
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
public interface SysMenuDao extends BaseMapper<SysMenuEntity> {

    /**
     * 根据角色查询用户权限
     * @Author gjl
     * @CreateTime 2021/11/09 10:14
     * @Param  roleId 角色ID
     * @Return List<SysMenuEntity> 权限集合
     */
    List<SysMenuEntity> selectSysMenuByRoleId(Long roleId);
	
}

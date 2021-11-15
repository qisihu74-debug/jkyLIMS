package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysRoleEntity;

import java.util.List;

/**
 * @Description 角色DAO
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
public interface SysRoleDao extends BaseMapper<SysRoleEntity> {

    /**
     * 通过用户ID查询角色集合
     * @Author gjl
     * @CreateTime 2021/11/09 18:01
     * @Param  userId 用户ID
     * @Return List<SysRoleEntity> 角色名集合
     */
    List<SysRoleEntity> selectSysRoleByUserId(Long userId);
	
}

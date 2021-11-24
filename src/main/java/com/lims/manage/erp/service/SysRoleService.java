package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.SysRoleEntity;

import java.util.List;

/**
 * @Description 角色业务接口
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
public interface SysRoleService extends IService<SysRoleEntity> {

    /**
     * 通过用户ID查询角色集合
     * @Author gjl
     * @CreateTime 2021/11/09 18:01
     * @Param  userId 用户ID
     * @Return List<SysRoleEntity> 角色名集合
     */
    List<SysRoleEntity> selectSysRoleByUserId(Long userId);

    /**
     * 展示角色信息
     * @param sysRoleEntity
     * @return
     */
    List<SysRoleEntity> selectSysRoleList(SysRoleEntity sysRoleEntity);

    /**
     * 更新角色
     * @param sysRoleEntity
     * @return
     */
    int updateSysRoleByUserId(SysRoleEntity sysRoleEntity);

    /**
     * 新增角色
     * @param sysRoleEntity
     * @return
     */
    SysRoleEntity addSysRoleByUserId(SysRoleEntity sysRoleEntity);

    /**
     *
     */
    int deleteSysRoleByUserId(Long roleId);

}


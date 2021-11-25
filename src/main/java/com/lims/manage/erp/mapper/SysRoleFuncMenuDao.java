package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.entity.SysMenuEntity;
import com.lims.manage.erp.entity.SysRoleFunction;
import com.lims.manage.erp.entity.SysRoleMenuEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/11/24 11:39
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface SysRoleFuncMenuDao {
    /**
     * 根据角色id获取所有角色下的菜单信息
     * @param roleId
     * @return
     */
    List<SysFunction> getFunctionsByRoleId(Long roleId);

    /**
     * 根据角色id查询角色下的权限
     * @param roleId
     * @return
     */
    List<SysMenuEntity> getMenusByRoleId(Long roleId);

    /**
     * 根据角色id删除角色下菜单
     * @param roleId
     */
    void delFuncByRoleId(Long roleId);

    /**
     * 根据角色id删除角色下权限
     * @param roleId
     */
    void delMenuByRoleId(Long roleId);

    /**
     * 授权保存角色菜单
     * @param roleFunctions
     */
    void insertBatchRoleFunc(List<SysRoleFunction> roleFunctions);

    /**
     * 授权保存角色权限
     * @param roleMenuEntities
     */
    void insertBatchRoleMenu(List<SysRoleMenuEntity> roleMenuEntities);

    /**
     * 添加权限
     * @param entity
     */
    void add(SysMenuEntity entity);

    /**
     * 查询所有菜单列表
     * @return
     */
    List<SysFunction> getFunctions();

    /**
     * 获取所有权限
     * @return
     */
    List<SysMenuEntity> getMenus();
}

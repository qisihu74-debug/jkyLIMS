package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
    void insertBatchRoleFunc(@Param("roleFunctions") List<SysRoleFunction> roleFunctions);

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

    /**
     * 通过用户id 得到 角色所属的 菜单项集合。
     * @param userId
     * @return
     */
    List<SysRoleFunction> selectSetMenu(Long userId);

    /**
     * 通过用户id 得到 角色所属的 菜单项集合包括父级id。
     * @param userId
     * @return
     */
    List<SysRoleFunctionParent> selectSetMenuPid(Long userId);

    /**
     * 查询角色已有菜单
     *
     * @param roleId
     * @return
     */
    List<Long> getFunctionIdByRoleIdS(Long roleId);
}

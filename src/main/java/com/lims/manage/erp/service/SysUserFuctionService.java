package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.entity.SysRoleMenuEntity;
import com.lims.manage.erp.entity.TreeFunction;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.SysRoleFuncMenuVo;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.service
 * @desc
 * @date 2021/11/10 14:14
 * @Copyright © 河南交科院
 */
public interface SysUserFuctionService {

    /**
     * 根据用户id获取用户菜单列表
     * @param userId
     * @return
     */
    List<SysFunction> getFunctionByuserId(Long userId);

    List<TreeFunction> GetList();

    List<TreeFunction> GetListPeer();

    /**
     * 依据 用户ID 展示所属菜单项。
     *
     * @param userid
     * @return
     */
    List<TreeFunction> GetListUpgrade(Long userid, String userName);

    /**
     * 查询角色现有权限
     *
     * @param roleId
     * @return
     */
    List<Long> getRoleMenu(Long roleId);

    /**
     * 查询角色ID已有权限集合
     *
     * @param roleId
     * @return
     */
    List<TreeFunction> getRoleMenuList(Long roleId);

    /**
     * 查询角色ID已有权限Ids集合
     *
     * @param roleId
     * @return
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 角色授权
     *
     * @param
     * @return
     */
    Boolean grant(SysRoleFuncMenuVo entity);

    Result postRoleSettingPermissions(List<SysRoleMenuEntity> list);

    Result postcancelRolePermissions(List<SysRoleMenuEntity> list);

    /**
     * 菜单列表
     *
     * @return
     */
    Result list();

    /**
     * 根据用户id获取用户菜单列表
     *
     * @param userId
     * @return
     */
    Result getReturnPermissionSet(Long userId);

    /**
     * 获取账号信息 -- 当拥有 开发者权限 发挥 true
     *
     * @return
     */
    Boolean getAccountInformation(Long userId);

}

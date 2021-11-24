package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.FunctionMenuEntity;
import com.lims.manage.erp.entity.SysMenuEntity;
import com.lims.manage.erp.entity.SysRoleFuncMenuEntity;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2021/11/24 11:38
 * @Copyright © 河南交科院
 */
public interface SysRoleFuncMenuService {
    /**
     *角色授权详情展示
     * @param roleId
     * @return
     */
    List<FunctionMenuEntity> getFuncAndMenuByRoleId(Long roleId);

    /**
     * 角色授权
     * @param entity
     * @return
     */
    Boolean grant(SysRoleFuncMenuEntity entity);

    /**
     * 添加权限
     * @param entity
     * @return
     */
    Boolean add(SysMenuEntity entity);
}

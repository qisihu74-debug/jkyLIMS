package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.entity.SysMenuEntity;
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
}

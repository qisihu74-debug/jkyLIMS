package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.FunctionMenuEntity;
import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.entity.SysMenuEntity;
import com.lims.manage.erp.mapper.SysRoleFuncMenuDao;
import com.lims.manage.erp.service.SysRoleFuncMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2021/11/24 11:38
 * @Copyright © 河南交科院
 */
@Service
public class SysRoleFuncMenuServiceImpl implements SysRoleFuncMenuService {
    @Autowired
    private SysRoleFuncMenuDao sysRoleFuncMenuDao;

    @Override
    public List<FunctionMenuEntity> getFuncAndMenuByRoleId(Long roleId) {
        List<FunctionMenuEntity> list = new ArrayList<>();
        //获取角色下的菜单列表
        List<SysFunction> funcs = sysRoleFuncMenuDao.getFunctionsByRoleId(roleId);
        //获取菜单下的权限列表
        List<SysMenuEntity> menuList = sysRoleFuncMenuDao.getMenusByRoleId(roleId);
        for (SysFunction sysFunction:funcs) {
            FunctionMenuEntity functionMenuEntity = new FunctionMenuEntity();
            functionMenuEntity.setFunctionId(sysFunction.getFunctionId());
            functionMenuEntity.setFunctionPid(sysFunction.getFunctionPid());
            functionMenuEntity.setFunctionName(sysFunction.getName());
            list.add(functionMenuEntity);
        }
        //合并角色下的权限集合
        Map<Long,List<SysMenuEntity>> map = new HashMap<>();
        for (SysMenuEntity sysMenuEntity:menuList) {
            if (CollectionUtils.isEmpty(map.get(sysMenuEntity.getFuctionId()))){
                List<SysMenuEntity> entities = new ArrayList<>();
                SysMenuEntity sysMenuEntity1 = new SysMenuEntity();
                sysMenuEntity1.setMenuId(sysMenuEntity.getMenuId());
                sysMenuEntity1.setName(sysMenuEntity.getName());
                entities.add(sysMenuEntity1);
                map.put(sysMenuEntity.getFuctionId(),entities);
            }else {
                List<SysMenuEntity> entityList = map.get(sysMenuEntity.getFuctionId());
                entityList.add(sysMenuEntity);
                map.put(sysMenuEntity.getFuctionId(),entityList);
            }
        }
        //设置菜单下的权限
        for (FunctionMenuEntity entity:list) {
            if (map.get(entity.getFunctionId()) != null){
                entity.setMenuIds(map.get(entity.getFunctionId()));
            }
        }
        return list;
    }

}

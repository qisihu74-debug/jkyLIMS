package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.FunctionMenuEntity;
import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.entity.SysMenuEntity;
import com.lims.manage.erp.entity.SysRoleFuncMenuEntity;
import com.lims.manage.erp.entity.SysRoleFunction;
import com.lims.manage.erp.entity.SysRoleMenuEntity;
import com.lims.manage.erp.mapper.SysRoleFuncMenuDao;
import com.lims.manage.erp.service.SysRoleFuncMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Slf4j
public class SysRoleFuncMenuServiceImpl implements SysRoleFuncMenuService {
    @Autowired
    private SysRoleFuncMenuDao sysRoleFuncMenuDao;

    @Override
    public List<FunctionMenuEntity> getFuncAndMenuByRoleId(Long roleId) {
        List<FunctionMenuEntity> list = new ArrayList<>();
        //获取角色下的菜单列表
        List<SysFunction> funcs = sysRoleFuncMenuDao.getFunctionsByRoleId(roleId);
        //获取所有菜单
        List<SysFunction> allFuncs = sysRoleFuncMenuDao.getFunctions();
        for (SysFunction abean :allFuncs) {
            for (SysFunction bean:funcs) {
                if (abean.getFunctionId().equals(bean.getFunctionId())){
                    abean.setFlag(true);
                }
            }
        }
        //获取菜单下的权限列表
        List<SysMenuEntity> menuList = sysRoleFuncMenuDao.getMenusByRoleId(roleId);
        //获取所有权限
        List<SysMenuEntity> allMenuList = sysRoleFuncMenuDao.getMenus();
        //整合已有的菜单、权限设置状态
        for (SysMenuEntity entity:allMenuList) {
            for (SysMenuEntity menuEntity:menuList) {
                if (entity.getMenuId().equals(menuEntity.getMenuId())){
                    entity.setFlag(true);
                }
            }
        }
        for (SysFunction sysFunction:allFuncs) {
            FunctionMenuEntity functionMenuEntity = new FunctionMenuEntity();
            functionMenuEntity.setFunctionId(sysFunction.getFunctionId());
            functionMenuEntity.setFunctionPid(sysFunction.getFunctionPid());
            functionMenuEntity.setFunctionName(sysFunction.getName());
            functionMenuEntity.setSort(sysFunction.getSort());
            functionMenuEntity.setFlag(sysFunction.getFlag());
            list.add(functionMenuEntity);
        }
        //合并角色下的权限集合
        Map<Long, List<SysMenuEntity>> map = batchMessage(allMenuList);
        //设置菜单下的权限
        for (FunctionMenuEntity entity:list) {
            if (map.get(entity.getFunctionId()) != null){
                entity.setMenuIds(map.get(entity.getFunctionId()));
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean grant(SysRoleFuncMenuEntity entity) {
        Boolean flag = false;
        List<SysRoleFunction> roleFunctions = new ArrayList<>();
        List<SysRoleMenuEntity> roleMenuEntities = new ArrayList<>();
        List<FunctionMenuEntity> list = entity.getList();
        for (FunctionMenuEntity functionMenuEntity:list) {
            SysRoleFunction sysRoleFunction = new SysRoleFunction();
            sysRoleFunction.setRoleId(entity.getRoleId());
            sysRoleFunction.setFunctionId(functionMenuEntity.getFunctionId());
            roleFunctions.add(sysRoleFunction);
            List<SysMenuEntity> menuIds = functionMenuEntity.getMenuIds();
            for (SysMenuEntity sysMenuEntity:menuIds) {
                SysRoleMenuEntity sysRoleMenuEntity = new SysRoleMenuEntity();
                sysRoleMenuEntity.setRoleId(entity.getRoleId());
                sysRoleMenuEntity.setMenuId(sysMenuEntity.getMenuId());
                roleMenuEntities.add(sysRoleMenuEntity);
            }
        }
        //角色菜单重新更新
        sysRoleFuncMenuDao.delFuncByRoleId(entity.getRoleId());
        sysRoleFuncMenuDao.insertBatchRoleFunc(roleFunctions);
        //角色权限重新更新
        sysRoleFuncMenuDao.delMenuByRoleId(entity.getRoleId());
        sysRoleFuncMenuDao.insertBatchRoleMenu(roleMenuEntities);
        flag = true;
        return flag;
    }

    @Override
    public Boolean add(SysMenuEntity entity) {
        Boolean flag = false;
        try {
            sysRoleFuncMenuDao.add(entity);
            flag = true;
        }catch (Exception e){
            log.error("添加权限失败:{}");
        }
        return flag;
    }

    /**
     * 处理权限集合
     * @param menuList
     * @return
     */
    public Map<Long,List<SysMenuEntity>> batchMessage(List<SysMenuEntity> menuList){
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
        return map;
    }

}

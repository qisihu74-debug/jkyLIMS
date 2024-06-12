package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.SysMenuDao;
import com.lims.manage.erp.mapper.SysRoleFuncMenuDao;
import com.lims.manage.erp.mapper.SysRoleMenuDao;
import com.lims.manage.erp.mapper.SysUserFuctionDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysUserFuctionService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.SysRoleFuncMenuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.service.impl
 * @desc
 * @date 2021/11/10 14:16
 * @Copyright © 河南交科院
 */
@Service
public class SysSysUserFuctionServiceImpl implements SysUserFuctionService {
    @Autowired
    private SysUserFuctionDao fuctionDao;
    @Autowired
    private SysRoleFuncMenuDao sysRoleFuncMenuDao;
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private SysMenuDao sysMenuDao;
    @Autowired
    private SysRoleMenuDao sysRoleMenuDao;

    @Override
    public List<SysFunction> getFunctionByuserId(Long userId) {

        return fuctionDao.getFunctionByuserId(userId);
    }

    @Override
    public List<TreeFunction> GetList() {
        List<TreeFunction> dataList = fuctionDao.getList();
        List<TreeFunction> bigTree = new ArrayList<>();
        for (TreeFunction treeEntity : dataList) {
            List children = new ArrayList<>();
            //再次遍历list，找到user的子节点
            for (TreeFunction node : dataList) {
                if (node.getFunctionPid().equals(treeEntity.getFunctionId())) {
                    children.add(node);
                }
            }
            treeEntity.setChildren(children);
            if (treeEntity.getChildren() != null && treeEntity.getChildren().size() > 0) {
                treeEntity.setCatesFlag(true);
            } else {
                treeEntity.setCatesFlag(false);
            }
            if (treeEntity.getFunctionPid() == 0) {
                bigTree.add(treeEntity);
            }
        }
        return bigTree;
    }

    @Override
    public List<TreeFunction> GetListPeer() {
        // 获取菜单集合
        List<TreeFunction> functionList = fuctionDao.getList();
        // 查询权限集合
        List<SysMenuEntity> menuList = sysMenuDao.selectList(null);
        // 遍历数据
        for (TreeFunction treeFunction : functionList) {
            for (SysMenuEntity sysMenuEntity : menuList) {
                if (treeFunction.getFunctionId().equals(sysMenuEntity.getFuctionId())) {
                    List<SysMenuEntity> menuEntityList = treeFunction.getMenuEntityList();
                    menuEntityList.add(sysMenuEntity);
                }
            }
        }
        return functionList;
    }

    @Override
    public List<TreeFunction> GetListUpgrade(Long userid, String userName) {
        PageHelper.clearPage();
        List<TreeFunction> dataList = returnListUpgrade1(userid);
        // 补充dataList
        methodConfigureMenuPermissions(userid, dataList);
        if (CollectionUtils.isEmpty(dataList)) {
            System.out.println("此用户不包含菜单信息，请配置");
            return null;
        }
        List<TreeFunction> bigTree = new ArrayList<>();
        for (TreeFunction treeEntity : dataList) {
            treeEntity.setUserId(userid);
            treeEntity.setUserName(userName);
            List children = new ArrayList<>();
            //再次遍历list，找到user的子节点
            for (TreeFunction node : dataList) {
                if (node.getFunctionPid().equals(treeEntity.getFunctionId())) {
                    children.add(node);
                }
            }
            treeEntity.setChildren(children);
            if (treeEntity.getChildren() != null && treeEntity.getChildren().size() > 0) {
                treeEntity.setCatesFlag(true);
            } else {
                treeEntity.setCatesFlag(false);
            }
            if (treeEntity.getFunctionPid() == 0) {
                bigTree.add(treeEntity);
            }
        }
        return bigTree;
    }

    @Override
    public List<Long> getRoleMenu(Long roleId) {
        // 获取菜单列表
        List<Long> funtionIds = sysRoleFuncMenuDao.getFunctionIdByRoleIdS(roleId);
        // 角色查询菜单 == null 直接返回
        if (CollectionUtils.isEmpty(funtionIds)) {
            return funtionIds;
        }
        // 查询全部菜单信息
        List<TreeFunction> list = fuctionDao.getList();
        // key = （ 获取全部菜单中 指向pid） ，value = (指向个数)
        Map<Long, Integer> map = new HashMap<>();
        for (TreeFunction function : list) {
            for (TreeFunction function1 : list) {
                if (function1.getFunctionPid().equals(function.getFunctionId())) {
                    // pid = id
                    if (map.get(function1.getFunctionPid()) != null) {
                        Integer pid = map.get(function1.getFunctionPid()) + 1;
                        map.put(function1.getFunctionPid(), pid);
                    } else {
                        map.put(function1.getFunctionPid(), 1);
                    }
                }
            }
        }
        // 循环 map数据
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(map.keySet())) {
            for (Long key : map.keySet()) {
                Iterator<Long> it = funtionIds.iterator();
                while (it.hasNext()) {
                    Long functionId = it.next();
                    if (map.get(key).equals(functionId)) {
                        it.remove();
                    }
                }
            }
        }
        return funtionIds;
    }

    /**
     * 查询角色ID已有权限集合
     *
     * @param roleId
     * @return
     */
    public List<TreeFunction> getRoleMenuList(Long roleId) {

        List<Long> funtionIds = getRoleMenu(roleId);
        if (CollectionUtils.isEmpty(funtionIds)) {
            return new ArrayList<>();
        }
        // 获取roleId 拥有的权限集合
        QueryWrapper<SysRoleMenuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        List<SysRoleMenuEntity> roleMenuEntityList = sysRoleMenuDao.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(roleMenuEntityList)) {
            // 角色为空
            return new ArrayList<>();
        }
        HashMap<Long, String> roleMenuMap = new HashMap<Long, String>();
        for (SysRoleMenuEntity sysRoleMenuEntity : roleMenuEntityList) {
            roleMenuMap.put(sysRoleMenuEntity.getMenuId(), "true");
        }
        HashMap<Long, String> funtionIdMap = new HashMap<Long, String>();
        for (Long funtionId : funtionIds) {
            funtionIdMap.put(funtionId, "true");
        }
        // 查询菜单集合
        List<TreeFunction> functionList = GetListPeer();
        for (TreeFunction treeFunction : functionList) {
            if (CollectionUtil.isNotEmpty(treeFunction.getMenuEntityList())) {
                List<SysMenuEntity> menuEntityList = treeFunction.getMenuEntityList();
                for (SysMenuEntity sysMenuEntity : menuEntityList) {
                    if (roleMenuMap.get(sysMenuEntity.getMenuId()) != null) {
                        sysMenuEntity.setFlag(true);
                    }
                }
            }
        }
        Iterator<TreeFunction> it = functionList.iterator();
        while ((it.hasNext())) {
            TreeFunction treeFunction = it.next();
            if (funtionIdMap.get(treeFunction.getFunctionId()) == null) {
                it.remove();
            }
        }
        return functionList;
    }

    /**
     * 查询角色ID已有权限Ids集合
     *
     * @param roleId
     * @return
     */
    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        // 获取roleId 拥有的权限集合
        QueryWrapper<SysRoleMenuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("menu_id");
        queryWrapper.eq("role_id", roleId);
        List<SysRoleMenuEntity> roleMenuEntityList = sysRoleMenuDao.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(roleMenuEntityList)) {
            return null;
        }
        List<Long> menuIds = new ArrayList<>();
        for (SysRoleMenuEntity sysRoleMenuEntity : roleMenuEntityList) {
            menuIds.add(sysRoleMenuEntity.getMenuId());
        }
        return menuIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean grant(SysRoleFuncMenuVo entity) {
        Boolean flag = false;
        List<SysRoleFunction> roleFunctions = new ArrayList<>();
        for (Long id : entity.getList()) {
            SysRoleFunction sysRoleFunction = new SysRoleFunction();
            sysRoleFunction.setRoleId(entity.getRoleId());
            sysRoleFunction.setFunctionId(id);
            roleFunctions.add(sysRoleFunction);
        }
        //角色菜单重新更新
        sysRoleFuncMenuDao.delFuncByRoleId(entity.getRoleId());
        sysRoleFuncMenuDao.insertBatchRoleFunc(roleFunctions);
        flag = true;
        return flag;
    }

    /**
     * 1、用户拥有 多个角色 2、多个角色下 展示具体菜单项ID
     *
     * @param userid
     * @return
     */
    public List<TreeFunction> returnListUpgrade(Long userid) {
        List<SysRoleFunctionParent> menuIdList = sysRoleFuncMenuDao.selectSetMenuPid(userid);
        if (menuIdList.isEmpty() || menuIdList.get(0) == null) {
            System.out.println("此用户不包含菜单信息，请配置");
            return null;
        }
        // 得到用户id下 所属菜单。
        Map<Long, SysRoleFunction> map = new HashMap<>();
        for (SysRoleFunctionParent sysRoleFunction : menuIdList) {
            map.put(sysRoleFunction.getFunctionId(), sysRoleFunction);
            map.put(sysRoleFunction.getFunctionPid(), sysRoleFunction);
        }
        // 菜单ID信息 展示所有 去除 functionIdSet
        List<TreeFunction> dataList = fuctionDao.getList();
        Iterator<TreeFunction> iterator = dataList.iterator();
        // 保留父级id 存放到map中
        for (TreeFunction treeFunction : dataList) {
            for (SysRoleFunctionParent sysRoleFunction : menuIdList) {
                if (treeFunction.getFunctionId().equals(sysRoleFunction.getFunctionPid())) {
                    // 主要取决于key 不要求vlue数值的准确性
                    map.put(treeFunction.getFunctionId(), sysRoleFunction);
                }
                if (treeFunction.getFunctionPid().equals(sysRoleFunction.getFunctionId())) {
                    // 主要取决于key 不要求vlue数值的准确性
                    map.put(treeFunction.getFunctionPid(), sysRoleFunction);
                }
            }
        }
        for (TreeFunction treeFunction : dataList) {
            if (map.get(treeFunction.getFunctionId()) != null && map.get(treeFunction.getFunctionPid()) == null) {
//                map.put(treeFunction.getFunctionPid(), null);
                System.out.println("输出展示信息" + treeFunction.getFunctionId());
                map.put(treeFunction.getFunctionPid(), null);
                map.put(treeFunction.getFunctionId(), null);
            }
        }
        while (iterator.hasNext()) {
            TreeFunction item = iterator.next();
            SysRoleFunction removeEntity = map.get(item.getFunctionId());
            if (removeEntity == null) {
                iterator.remove();
            }
        }
        return dataList;
    }

    /**
     * 1、用户拥有 多个角色 2、多个角色下 展示具体菜单项ID
     * 暂时废弃 6月28 误删（测试菜单详情处理）
     * @param userid
     * @return
     */
    public List<TreeFunction> returnListUpgrade1(Long userid) {
        PageHelper.clearPage();
        List<SysRoleFunctionParent> menuIdList = sysRoleFuncMenuDao.selectSetMenuPid(userid);
        //记录日志
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户(userid)=" + userid + "获取菜单：" + ShiroUtils.getUserInfo().getUsername() + "用户获取菜单大小\t" + menuIdList.size() + "成功！", Const.SYS_MANAGER_LOG, true);
        // 获取菜单列表
        List<TreeFunction> dataList = fuctionDao.getList();
        // 通过用户id 返回 对应的菜单权限信息
        //记录日志
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "获取菜单全部：" + ShiroUtils.getUserInfo().getUsername() + "用户获取菜单大小\t" + dataList.size() + "成功！", Const.SYS_MANAGER_LOG, true);
        if (CollectionUtil.isEmpty(menuIdList)) {
            // 抛出null = 此用户不包含菜单信息，请配置
            return null;
        }
        // 得到用户id下 所属菜单。
        Map<Long, SysRoleFunction> map = new HashMap<>();
        for (SysRoleFunctionParent sysRoleFunction : menuIdList) {
            map.put(sysRoleFunction.getFunctionId(), sysRoleFunction);
            map.put(sysRoleFunction.getFunctionPid(), sysRoleFunction);
        }
        for (SysRoleFunctionParent sysRoleFunction : menuIdList) {
            for (TreeFunction treeFunction : dataList) {
                if (treeFunction.getFunctionId().equals(sysRoleFunction.getFunctionPid())) {
                    // 主要取决于key 不要求vlue数值的准确性
                    map.put(treeFunction.getFunctionId(), sysRoleFunction);
                }
                if (treeFunction.getFunctionPid().equals(sysRoleFunction.getFunctionId())) {
                    // 主要取决于key 不要求vlue数值的准确性
                    map.put(treeFunction.getFunctionPid(), sysRoleFunction);
                }
            }
        }
        for (TreeFunction data : dataList) {
            if (map.get(data.getFunctionId()) != null && map.get(data.getFunctionPid()) == null) {
                SysRoleFunction removeEntity = new SysRoleFunction();
                removeEntity.setId(0L);
                map.put(data.getFunctionPid(), removeEntity);
            }
        }
        // 菜单ID信息 展示所有 去除 functionIdSet
        Iterator<TreeFunction> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            TreeFunction item = iterator.next();
            SysRoleFunction removeEntity = map.get(item.getFunctionId());
            if (removeEntity == null) {
                iterator.remove();
            }
        }
        return dataList;
    }

    /**
     * 通过userId 及菜单信息 进行配置
     *
     * @param userId
     * @param dataList
     */
    public void methodConfigureMenuPermissions(Long userId, List<TreeFunction> dataList) {

        // 通过用户id 获取拥有权限集合
        List<SysMenuEntity> menuListByUserId = sysMenuDao.selectSysMenuEntityListByUserId(userId);
        Map<Long, String> menuListByUserIdMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(menuListByUserId)) {
            for (SysMenuEntity sysMenuEntity : menuListByUserId) {
                menuListByUserIdMap.put(sysMenuEntity.getMenuId(), "true");
            }
        }
        // 获取权限列表
        List<SysMenuEntity> menuList = sysMenuDao.selectList(null);
        // key = 菜单fuctionId，value = 拥有的权限列表
        Map<Long, List<SysMenuEntity>> map = new HashMap<>();
        for (SysMenuEntity sysMenuEntity : menuList) {
            // 用户拥有权限设置为 true
            if (menuListByUserIdMap.get(sysMenuEntity.getMenuId()) != null) {
                sysMenuEntity.setFlag(true);
            } else {
                // 用户没有此权限 设置为 false
                sysMenuEntity.setFlag(false);
            }
            if (map.get(sysMenuEntity.getFuctionId()) == null) {
                List<SysMenuEntity> menuEntityList = new ArrayList<>();
                menuEntityList.add(sysMenuEntity);
                map.put(sysMenuEntity.getFuctionId(), menuEntityList);
            } else {
                List<SysMenuEntity> menuEntityList = map.get(sysMenuEntity.getFuctionId());
                menuEntityList.add(sysMenuEntity);
                map.put(sysMenuEntity.getFuctionId(), menuEntityList);
            }
        }
        for (TreeFunction treeFunction : dataList) {
            if (map.get(treeFunction.getFunctionId()) != null) {
                List<SysMenuEntity> menuPermissionSet = map.get(treeFunction.getFunctionId());
                treeFunction.setMenuEntityList(menuPermissionSet);
            }
        }
    }

    /**
     * 角色设置权限
     *
     * @param list
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result postRoleSettingPermissions(List<SysRoleMenuEntity> list) {
        for (SysRoleMenuEntity sysRoleMenuEntity : list) {
            sysRoleMenuEntity.setId(null);
            sysRoleMenuDao.insert(sysRoleMenuEntity);
        }
        return ResultUtil.success();
    }

    /**
     * 取消角色设置权限
     *
     * @param list
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result postcancelRolePermissions(List<SysRoleMenuEntity> list) {
        for (SysRoleMenuEntity sysRoleMenuEntity : list) {
            sysRoleMenuEntity.setId(null);
            QueryWrapper<SysRoleMenuEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("menu_id", sysRoleMenuEntity.getMenuId());
            queryWrapper.eq("role_id", sysRoleMenuEntity.getRoleId());
            sysRoleMenuDao.delete(queryWrapper);
        }
        return ResultUtil.success();
    }
}

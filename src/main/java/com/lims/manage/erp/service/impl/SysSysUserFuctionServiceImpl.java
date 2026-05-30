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
import com.lims.manage.erp.util.StringUtils;
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
//        // 获取菜单列表
//        List<Long> funtionIds = sysRoleFuncMenuDao.getFunctionIdByRoleIdS(roleId);
//        // 角色查询菜单 == null 直接返回
//        if (CollectionUtils.isEmpty(funtionIds)) {
//            return funtionIds;
//        }
//        // 查询全部菜单信息
//        List<TreeFunction> list = fuctionDao.getList();
//        // key = （ 获取全部菜单中 指向pid） ，value = (指向个数)
//        Map<Long, Integer> map = new HashMap<>();
//        for (TreeFunction function : list) {
//            for (TreeFunction function1 : list) {
//                if (function1.getFunctionPid().equals(function.getFunctionId())) {
//                    // pid = id
//                    if (map.get(function1.getFunctionPid()) != null) {
//                        Integer pid = map.get(function1.getFunctionPid()) + 1;
//                        map.put(function1.getFunctionPid(), pid);
//                    } else {
//                        map.put(function1.getFunctionPid(), 1);
//                    }
//                }
//            }
//        }
//        // 循环 map数据
//        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(map.keySet())) {
//            for (Long key : map.keySet()) {
//                Iterator<Long> it = funtionIds.iterator();
//                while (it.hasNext()) {
//                    Long functionId = it.next();
//                    if (map.get(key).equals(functionId)) {
//                        it.remove();
//                    }
//                }
//            }
//        }
//        return funtionIds;
        return getRoleMenu1(roleId);
    }

    /**
     * -- 1、实现  自身是菜单页 有对应的 下一级中菜单 包含 菜单项（全部包含情况下）勾选。
     * -- 2、如果 不全时 则 进行不展示。 对应的 菜单父级 也不应展示。
     *
     * @param roleId
     * @return
     */
    public List<Long> getRoleMenu1(Long roleId) {

        // 获取菜单列表
        List<Long> funtionIds = sysRoleFuncMenuDao.getFunctionIdByRoleIdS(roleId);
        // 角色查询菜单 == null 直接返回
        if (CollectionUtils.isEmpty(funtionIds)) {
            return funtionIds;
        }

        // 查询全部菜单信息
        List<TreeFunction> list = fuctionDao.getList();
        // 标准的 key = （ 获取全部菜单中 指向pid） ，value = (标准指向个数) - 其余 数量为1个。
        Map<Long, Integer> map = new HashMap<>();
        for (TreeFunction function : list) {
            for (TreeFunction function1 : list) {
                if (function.getFunctionId().equals(function1.getFunctionPid())) {
                    // pid = id
                    if (map.get(function.getFunctionId()) != null) {
                        // 计数器 =
                        Integer count = map.get(function.getFunctionId()) + 1;
                        map.put(function.getFunctionId(), count);
                    } else {
                        map.put(function.getFunctionId(), 1);
                    }
                }
            }
        }
        // 当前角色拥有的菜单列表
        List<SysRoleFunctionParent> roleList = sysRoleFuncMenuDao.selectSetMenuRoleId(roleId);

        // 获取当前 角色拥有的 菜单信息
        Map<Long, Integer> roleMap = new HashMap<>();

        for (SysRoleFunctionParent function : roleList) {
            for (SysRoleFunctionParent function1 : roleList) {
                if (function.getFunctionId().equals(function1.getFunctionPid())) {
                    if (roleMap.get(function.getFunctionId()) != null) {
                        // 计数器 =
                        Integer count = map.get(function.getFunctionId()) + 1;
                        map.put(function.getFunctionId(), count);
                    } else {
                        roleMap.put(function.getFunctionId(), 1);
                    }
                }
            }
        }

        // 当前角色 拥有的菜单列表
        if (CollectionUtil.isNotEmpty(roleMap.keySet())) {
            // 进行待删除的 map信息
            Map<Long, String> delteFunctionIdMap = new HashMap<>();
            // 进行遍历处理
            for (Long key : roleMap.keySet()) {
                Integer functionPidSize = roleMap.get(key);
                Integer functionAllSize = map.get(key);
                if (functionPidSize != functionAllSize) {
                    // 进行移除操作 说明当前角色 拥有的菜单信息 条数缺失。
                    delteFunctionIdMap.put(key, "1");
                }
            }
            if (CollectionUtil.isNotEmpty(delteFunctionIdMap.keySet())) {
                // 进行 遍历移除即可
                Iterator<Long> it = funtionIds.iterator();
                while (it.hasNext()) {
                    Long functionId = it.next();
                    if (delteFunctionIdMap.get(functionId) != null) {
                        it.remove();
                    }
                }
            }
        }

        // funtionIds 执行 新增 或者 删除。
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

    /*
     * 获取全部子集
     * @param id
     * @param list
     * @return
     */
    public static List<TreeFunction> getChildrenList(Long id, List<TreeFunction> list) {
        List<TreeFunction> pdr = new ArrayList<>();
        for (TreeFunction per : list) {
            if (per.getFunctionPid().equals(id)) {
                pdr.add(per);
            }
        }
        return pdr;
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

    /**
     * 1、用户拥有 多个角色 2、多个角色下 展示具体菜单项ID
     * 暂时废弃 6月28 误删（测试菜单详情处理）
     *
     * @param userid
     * @return
     */
    public List<TreeFunction> returnListUpgrade1(Long userid) {
        PageHelper.clearPage();
        // 对于dataType = button的移除。
        List<SysRoleFunctionParent> menuIdList = sysRoleFuncMenuDao.selectSetMenuPid(userid);
        //记录日志
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户(userid)=" + userid + "获取菜单：" + ShiroUtils.getUserInfo().getUsername() + "用户获取菜单大小\t" + menuIdList.size() + "成功！", Const.SYS_MANAGER_LOG, true);
        // 获取菜单列表
        List<TreeFunction> dataList = fuctionDao.getList();
        Map<Long, TreeFunction> allFunctionmap = new HashMap<>();
        for (TreeFunction function : dataList) {
            allFunctionmap.put(function.getFunctionId(), function);
        }
        // 通过用户id 返回 对应的菜单权限信息
        //记录日志
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "获取菜单全部：" + ShiroUtils.getUserInfo().getUsername() + "用户获取菜单大小\t" + dataList.size() + "成功！", Const.SYS_MANAGER_LOG, true);
        if (CollectionUtil.isEmpty(menuIdList)) {
            // 抛出null = 此用户不包含菜单信息，请配置
            return null;
        }

        // menuIdList 遍历 缺失的层级信息 pid
        List<SysRoleFunctionParent> addMenuIdList = new ArrayList<>();

        for (SysRoleFunctionParent sysRoleFunctionParent : menuIdList) {
            Boolean flag = false;
            for (SysRoleFunctionParent functionParent : menuIdList) {
                if (sysRoleFunctionParent.getFunctionPid() == functionParent.getFunctionId()) {
                    flag = true;
                }
            }
            // 需要进行补充 pid层级菜单
            if (!flag) {
                TreeFunction treeFunction = allFunctionmap.get(sysRoleFunctionParent.getFunctionPid());
                if (treeFunction != null) {
                    // 进行赋值即可
                    SysRoleFunctionParent sysRoleFunctionParent1 = new SysRoleFunctionParent();
                    sysRoleFunctionParent1.setFunctionId(treeFunction.getFunctionId());
                    sysRoleFunctionParent1.setTreeName(treeFunction.getTreeName() != null ? treeFunction.getTreeName() : null);
                    sysRoleFunctionParent1.setDataType(treeFunction.getDataType());
                    sysRoleFunctionParent1.setFunctionPid(treeFunction.getFunctionPid());
                    sysRoleFunctionParent1.setSort(treeFunction.getSort() != null ? treeFunction.getSort() : null);
                    addMenuIdList.add(sysRoleFunctionParent1);
                }
            }
        }
        // 进行追加
        if (CollectionUtil.isNotEmpty(addMenuIdList)) {
            for (SysRoleFunctionParent sysRoleFunctionParent : addMenuIdList) {
                menuIdList.add(sysRoleFunctionParent);
            }
        }

        // 返回个人菜单栏时：过滤权限
        Iterator<SysRoleFunctionParent> it = menuIdList.iterator();
        while (it.hasNext()) {
            SysRoleFunctionParent item = it.next();
            if (item.getDataType().equals("button")) {
                it.remove();
            }
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

    @Override
    public Result list() {

        List<TreeFunction> list = searchMenu();
        return ResultUtil.success(list);
    }

    @Override
    public Result getReturnPermissionSet(Long userId) {
        List<SysFunction> functionList = fuctionDao.getReturnPermissionSet(userId, null);
        // if
        if (CollectionUtil.isEmpty(functionList)) {
            return ResultUtil.success(null);
        }
        List<TreeFunction> treeFunctionList = new ArrayList<>();
        Map<Long, SysFunction> map = new HashMap<>();
        for (SysFunction sysFunction : functionList) {
            map.put(sysFunction.getFunctionId(), sysFunction);
        }
        for (SysFunction sysFunction : functionList) {
            if (StringUtils.isNotEmpty(sysFunction.getDataType()) && sysFunction.getDataType().equals("button")) {
                if (map.get(sysFunction.getFunctionId()) != null) {
                    SysFunction sysFunctionPid = map.get(sysFunction.getFunctionPid());
                    TreeFunction treeFunction = new TreeFunction(sysFunction);
                    if (sysFunctionPid != null && StringUtils.isNotEmpty(treeFunction.getMenuValue()) && StringUtils.isNotEmpty(sysFunctionPid.getMenuValue())) {
                        treeFunction.setManageContent(sysFunctionPid.getMenuValue() + ":" + treeFunction.getMenuValue());
                    }
                    treeFunctionList.add(treeFunction);
                }
            }
        }
        return ResultUtil.success(treeFunctionList);
    }

    @Override
    public Boolean getAccountInformation(Long userId) {
        SysRoleFunction sysRoleFunction = sysRoleFuncMenuDao.selectAccountInformation(userId);
        if (sysRoleFunction == null) {
            return false;
        }
        return true;
    }

    public List<TreeFunction> searchMenu() {

        //创建一个新的集合重新存放数据
        List<TreeFunction> directoryTree = new ArrayList<>();
        //查询全部父子级菜单
        List<TreeFunction> menuList = fuctionDao.getList();

        if (CollectionUtil.isEmpty(menuList)) {
            return null;
        }

        for (TreeFunction e : menuList) {
            List<TreeFunction> pdrList = getChildrenList(e.getFunctionId(), menuList);
            e.setChildren(pdrList != null ? pdrList : null);
        }

        for (TreeFunction e : menuList) {
            if (e.getFunctionPid() == 0) {
                directoryTree.add(e);
            }
        }
        return directoryTree;
    }


}

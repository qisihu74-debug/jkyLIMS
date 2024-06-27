package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.entity.SysRoleMenuEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TreeFunction;
import com.lims.manage.erp.mapper.SysUserFuctionDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysUserFuctionService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.SysRoleFuncMenuVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.controller
 * @desc
 * @date 2021/11/10 14:10
 * @Copyright © 河南交科院
 */
@Slf4j
@RestController
@RequestMapping("/userFunction/")
public class UserFuctionController {
    @Autowired
    private SysUserFuctionService sysUserFuctionService;
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private SysUserFuctionDao fuctionDao;

    /**
     * 获取当前登录用户的菜单列表
     *
     * @param request
     * @return
     */
    @GetMapping("getFunction")
    public Result getFunction(ServletRequest request) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        List<SysFunction> list = sysUserFuctionService.getFunctionByuserId(userInfo.getUserId());
        return ResultUtil.success(list);
    }

    /**
     * 查询所有权限
     *
     * @return
     */
    @GetMapping("getAllMenu")
    public List<TreeFunction> getMenuDisplay() {
        return sysUserFuctionService.GetList();
    }

    /**
     * 查询所有权限_同级展示信息
     *
     * @return
     */
    @GetMapping("getAllMenu_peer")
    public Result getMenuDisplayPeer() {
        return ResultUtil.success(sysUserFuctionService.GetListPeer());
    }

    /**
     * 查询角色已有权限
     *
     * @param roleId
     * @return
     */
    @GetMapping("/getRoleMenu")
    //@RequiresRoles("ADMIN")
    public Result getRoleMenu(Long roleId) {
        return ResultUtil.success("查询角色菜单权限成功！", sysUserFuctionService.getRoleMenu(roleId));
    }

    /**
     * 查询角色ID已授权限集合
     *
     * @param roleId
     * @return
     */
    @GetMapping("/getRoleMenuList")
    //@RequiresRoles("ADMIN")
    public Result getRoleMenuList(Long roleId) {
        return ResultUtil.success("查询角色菜单权限成功！", sysUserFuctionService.getRoleMenuList(roleId));
    }

    /**
     * 查询角色ID已授权限集合
     *
     * @param roleId
     * @return
     */
    @GetMapping("/getRoleMenuIds")
    //@RequiresRoles("ADMIN")
    public Result getRoleMenuIds(Long roleId) {
        return ResultUtil.success("查询角色菜单权限成功！", sysUserFuctionService.getRoleMenuIds(roleId));
    }


    //  优化 菜单展示。
    @GetMapping("getMenuDisplayNew")
    public Result getMenuDisplayNew1() {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        log.info("菜单进入获取登录人\tuserId" + userInfo.getUserId() + "\tname=" + userInfo.getUsername());
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        List<TreeFunction> dataList = sysUserFuctionService.GetListUpgrade(userInfo.getUserId(), userInfo.getUsername());
        if (dataList != null && dataList.size() > 0 && !dataList.isEmpty()) {
            log.info("菜单输出获取登录人\tuserId"+userInfo.getUserId()+"\tname="+userInfo.getUsername());
            return ResultUtil.success(dataList);
        }
        return ResultUtil.error("使用人角色未配置菜单");

    }

    /**
     * 角色授权
     *
     * @param entity
     * @return
     */
    @PostMapping("grant")
//    @RequiresPermissions("sys:menu:grant")
    //@RequiresRoles("ADMIN")
    public Result grant(@RequestBody SysRoleFuncMenuVo entity) {
        if (entity.getRoleId() == null) {
            return ResultUtil.error(-1, "请选择授权的角色！");
        }
        if (CollectionUtils.isEmpty(entity.getList())) {
            return ResultUtil.error(-1, "请选择授权的菜单！");
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        Boolean flag = sysUserFuctionService.grant(entity);
        if (flag) {
            //记录日志
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "角色授权ID【" + entity.getRoleId() + "】状态为" + "成功！", Const.SYS_MANAGER_LOG, true);
            //授权完成清除当前用户缓存信息
            ShiroUtils.deleteCache(ShiroUtils.getUserInfo().getUsername(), false);
            return ResultUtil.success("授权成功！");
        } else {
            //记录失败日志
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "角色授权ID【" + entity.getRoleId() + "】状态为" + "失败！", Const.SYS_MANAGER_LOG, false);
            return ResultUtil.error(-1, "授权失败");
        }
    }

    /**
     * 角色设置权限
     *
     * @param list
     * @return
     */
    @PostMapping("roleSettingPermissions")
//    @RequiresPermissions("sys:menu:grant")
    //@RequiresRoles("ADMIN")
    public Result roleSettingPermissions(@RequestBody List<SysRoleMenuEntity> list) {

        return sysUserFuctionService.postRoleSettingPermissions(list);
    }

    /**
     * 取消角色设置权限
     *
     * @param list
     * @return
     */
    @DeleteMapping("cancelRolePermissions")
//    @RequiresPermissions("sys:menu:grant")
    //@RequiresRoles("ADMIN")
    public Result cancelRolePermissions(@RequestBody List<SysRoleMenuEntity> list) {

        return sysUserFuctionService.postcancelRolePermissions(list);
    }

    /**
     * 菜单管理-新增
     *
     * @param treeFunction
     * @return
     */
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result addMenu(@RequestBody TreeFunction treeFunction) {

        // 最大id +1
        LambdaQueryWrapper<SysFunction> queryWrapper = new LambdaQueryWrapper<SysFunction>();
        queryWrapper.orderByDesc(SysFunction::getFunctionId);
        queryWrapper.last("limit 1");
        SysFunction sysFunctionMaxId = fuctionDao.selectOne(queryWrapper);
        if (treeFunction != null && treeFunction.getMenuValue() != null) {
            LambdaQueryWrapper<SysFunction> fuctionWrapper = new LambdaQueryWrapper<SysFunction>();
            fuctionWrapper.eq(SysFunction::getMenuValue, treeFunction.getMenuValue());
            List<SysFunction> functionList = fuctionDao.selectList(fuctionWrapper);
            if (CollectionUtil.isNotEmpty(functionList)) {
                return ResultUtil.error("新增失败，菜单标志符唯一性");
            }
        }
        treeFunction.setFunctionPid(sysFunctionMaxId != null ? sysFunctionMaxId.getFunctionId() + 1 : 1);
        SysFunction sysFunction = new SysFunction(treeFunction);
        fuctionDao.insert(sysFunction);
        return ResultUtil.success();
    }

    /**
     * 菜单管理-编辑
     *
     * @param treeFunction
     * @return
     */
    @PostMapping("/edit")
    @Transactional(rollbackFor = Exception.class)
    public Result editMenu(@RequestBody TreeFunction treeFunction) {
        if (treeFunction != null && treeFunction.getMenuValue() != null) {
            LambdaQueryWrapper<SysFunction> fuctionWrapper = new LambdaQueryWrapper<SysFunction>();
            fuctionWrapper.eq(SysFunction::getMenuValue, treeFunction.getMenuValue());
            fuctionWrapper.ne(SysFunction::getFunctionId, treeFunction.getFunctionId());
            List<SysFunction> functionList = fuctionDao.selectList(fuctionWrapper);
            if (CollectionUtil.isNotEmpty(functionList)) {
                return ResultUtil.error("编辑失败，菜单标志符保持唯一性");
            }
        }
        SysFunction sysFunction = new SysFunction(treeFunction);
        fuctionDao.updateById(sysFunction);
        return ResultUtil.success();
    }

    // 菜单展示。
    @GetMapping("list")
    public Result list() {
        return ResultUtil.success(sysUserFuctionService.list());
    }

}

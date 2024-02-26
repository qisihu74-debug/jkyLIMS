package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.SysRoleEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 角色的管理
 * @Author gjl
 * @CreateTime 2021/11/09 11:38
 */
@RestController
@RequestMapping("/role")
public class UserRoleController {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Autowired
    private LogManagerService logManagerService;

    /**
     * 管理员角色测试接口
     * @Author gjl
     * @CreateTime 2021/11/09 10:38
     * @Return Map<String,Object> 返回结果
     */
    @RequestMapping("/getAdminInfo")
    //@RequiresRoles("ADMIN")
    public Map<String,Object> getAdminInfo(){
        Map<String,Object> map = new HashMap<>();
        map.put("code",200);
        map.put("msg","这里是只有管理员角色能访问的接口");
        return map;
    }

    /**
     * 用户角色测试接口
     * @Author gjl
     * @CreateTime 2021/11/09 10:38
     * @Return Map<String,Object> 返回结果
     */
    @RequestMapping("/getUserInfo")
    //@RequiresRoles("USER")
    public Map<String,Object> getUserInfo(){
        Map<String,Object> map = new HashMap<>();
        map.put("code",200);
        map.put("msg","这里是只有用户角色能访问的接口");
        return map;
    }

    /**
     * 角色测试接口
     * @Author gjl
     * @CreateTime 2021/11/09 10:38
     * @Return Map<String,Object> 返回结果
     */
    @RequestMapping("/getRoleInfo")
    //@RequiresRoles(value={"ADMIN","USER"},logical = Logical.OR)
    @RequiresUser
    public Map<String,Object> getRoleInfo(){
        Map<String,Object> map = new HashMap<>();
        map.put("code",200);
        map.put("msg","这里是只要有ADMIN或者USER角色能访问的接口");
        return map;
    }

    /**
     * 登出
     * @Author gjl
     * @CreateTime 2021/11/09 10:38
     * @Return Map<String,Object> 返回结果
     */
    @RequestMapping("/getLogout")
    @RequiresUser
    public Result getLogout(){
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        //登出Shiro会帮我们清理掉Session和Cache
        ShiroUtils.logout();
        return ResultUtil.success("退出");
    }

    /**
     * 角色信息展示
     *
     * @param sysRoleEntity
     * @return
     */
    @GetMapping("/list")
    //@RequiresPermissions("sys:role:list")
    public Result mehtodStr(SysRoleEntity sysRoleEntity) {
        return ResultUtil.success(sysRoleService.selectSysRoleList(sysRoleEntity));
    }

    @PostMapping("/edit")
    //@RequiresPermissions("sys:role:edit")
    public Result methodEditData(@RequestBody SysRoleEntity sysRoleEntity) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (sysRoleEntity == null) {
            return ResultUtil.error("参数不能为空");
        }
        if (sysRoleEntity.getRoleId() == null) {
            return ResultUtil.error("角色id不能为空");
        }
        if (StringUtils.isEmpty(sysRoleEntity.getRoleName())) {
            return ResultUtil.error("角色名称不能为空");
        }
        int statusNumber = 0;
        try {
            statusNumber = sysRoleService.updateSysRoleByUserId(sysRoleEntity);
        } catch (Exception e) {
        }
        Map<String, Object> map = new HashMap<>();
        if (statusNumber >= 1) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改角色ID【" + sysRoleEntity.getRoleId() + "】状态为" + "成功！", Const.SYS_MANAGER_LOG, true);
            return ResultUtil.success("修改角色成功");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改角色ID【" + sysRoleEntity.getRoleId() + "】状态为" + "失败！", Const.SYS_MANAGER_LOG, false);
        return ResultUtil.error("修改角色失败");
    }

    @PostMapping("/add")
    //@RequiresPermissions("sys:role:add")
    public Result methodAddData(@RequestBody SysRoleEntity sysRoleEntity) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (StringUtils.isEmpty(sysRoleEntity.getRoleName())) {
            return ResultUtil.error("角色名称不能为空");
        }
        Boolean judge = false;
        try {
            judge = sysRoleService.addSysRoleByUserId(sysRoleEntity);
        } catch (Exception e) {
        }
        Map<String, Object> map = new HashMap<>();
        if (judge == false) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "新增角色ID【" + sysRoleEntity.getRoleId() + "】状态为" + "失败！", Const.SYS_MANAGER_LOG, false);
            return ResultUtil.error("新增失败");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "新增角色ID【" + sysRoleEntity.getRoleId() + "】状态为" + "成功！", Const.SYS_MANAGER_LOG, true);
        return ResultUtil.success("新增角色成功");
    }

    @PostMapping("/remove/{roleId}")
    //@RequiresPermissions("sys:role:remove")
    public Result methodAddData(@PathVariable Long roleId) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (roleId == null) {
            return ResultUtil.error("角色id不能为空");
        }
        // 查询角色详情
        SysRoleEntity roleEntity = sysRoleService.getById(roleId);
        if (roleEntity == null) {
            return ResultUtil.error("删除失败-角色不存在");
        }
        if (roleEntity.getIsDelete() != null && roleEntity.getIsDelete().equals(1)) {
            return ResultUtil.error("删除失败-角色不允许删除");
        }
        int statusNumber = 0;
        try {
            statusNumber = sysRoleService.deleteSysRoleByUserId(roleId);
        } catch (Exception e) {
        }
        Map<String, Object> map = new HashMap<>();
        if (statusNumber >= 1) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "删除角色ID【" + roleId + "】", Const.SYS_MANAGER_LOG, true);
            return ResultUtil.success("删除角色成功");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "删除角色ID【" + roleId + "】", Const.SYS_MANAGER_LOG, false);
        return ResultUtil.error("删除角色失败");
    }
}

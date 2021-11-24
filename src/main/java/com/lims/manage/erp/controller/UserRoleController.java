package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.SysRoleEntity;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
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
    @RequiresRoles("ADMIN")
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
    @RequiresRoles("USER")
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
    @RequiresRoles(value={"ADMIN","USER"},logical = Logical.OR)
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
    public Map<String,Object> getLogout(){
        //登出Shiro会帮我们清理掉Session和Cache
        ShiroUtils.logout();
        Map<String,Object> map = new HashMap<>();
        map.put("code",200);
        map.put("msg","登出");
        return map;
    }

    /**
     * 角色信息展示
     * @param sysRoleEntity
     * @return
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:role:list")
    public Map<String,Object> mehtodStr(SysRoleEntity sysRoleEntity)
    {
        Map<String,Object> map = new HashMap<>();
        map.put("data",sysRoleService.selectSysRoleList(sysRoleEntity));
        map.put("code",200);
        map.put("msg","查看角色成功");
        return map;
    }
    @PostMapping("/edit")
    @RequiresPermissions("sys:role:edit")
    public Map<String,Object> methodEditData(@RequestBody SysRoleEntity sysRoleEntity)
    {
        int statusNumber=0;
        try {
            statusNumber = sysRoleService.updateSysRoleByUserId(sysRoleEntity);
        }
        catch (Exception e){
        }
        Map<String,Object> map = new HashMap<>();
        if(statusNumber>=1)
        {
            map.put("code",200);
            map.put("msg","修改角色成功");
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改角色ID【"+sysRoleEntity.getRoleId()+"】状态为"+"成功！", Const.CHANGE_STATE);
            return map;
        }
        map.put("code",204);
        map.put("msg","修改失败");
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改角色ID【"+sysRoleEntity.getRoleId()+"】状态为"+"失败！", Const.CHANGE_STATE);
        return map;
    }
    @PostMapping("/add")
    @RequiresPermissions("sys:role:add")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> methodAddData(@RequestBody SysRoleEntity sysRoleEntity)
    {
        SysRoleEntity sysRoleEntity1 = new SysRoleEntity();
        try {
            sysRoleEntity1 = sysRoleService.addSysRoleByUserId(sysRoleEntity);
        }
        catch (Exception e){
        }
        Map<String,Object> map = new HashMap<>();
        if(sysRoleEntity1!=null)
        {
            map.put("code",200);
            map.put("msg","新增角色成功");
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"新增角色ID【"+sysRoleEntity1.getRoleId()+"】状态为"+"成功！", Const.CHANGE_STATE);
            return map;
        }
        map.put("code",204);
        map.put("msg","新增失败");
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"新增角色ID【"+sysRoleEntity.getRoleId()+"】状态为"+"失败！", Const.CHANGE_STATE);
        return map;
    }

    @PostMapping("/remove/{roleId}")
    @RequiresPermissions("sys:role:remove")
    public Map<String,Object> methodAddData(@PathVariable Long roleId)
    {
        System.out.println("获取需要删除的id\t"+roleId);
        int statusNumber=0;
        try {
            statusNumber = sysRoleService.deleteSysRoleByUserId(roleId);
        }
        catch (Exception e){

        }
        Map<String,Object> map = new HashMap<>();
        if(statusNumber>=1)
        {
            map.put("code",200);
            map.put("msg","删除角色成功");
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"删除角色ID【"+roleId+"】状态为"+"成功！", Const.CHANGE_STATE);
            return map;
        }
        map.put("code",204);
        map.put("msg","删除角色失败");
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"删除角色ID【"+roleId+"】状态为"+"失败！", Const.CHANGE_STATE);
        return map;
    }

}

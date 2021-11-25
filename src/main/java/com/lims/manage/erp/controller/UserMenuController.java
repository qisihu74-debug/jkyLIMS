package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.FunctionMenuEntity;
import com.lims.manage.erp.entity.SysMenuEntity;
import com.lims.manage.erp.entity.SysRoleEntity;
import com.lims.manage.erp.entity.SysRoleFuncMenuEntity;
import com.lims.manage.erp.entity.SysRoleMenuEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 权限测试
 * @Author gjl
 * @CreateTime 2021/11/09 11:38
 */
@RestController
@RequestMapping("/menu")
public class UserMenuController {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Autowired
    private SysRoleFuncMenuService service;
    @Autowired
    private LogManagerService logManagerService;

    /**
     * 获取用户信息集合
     * @Author gjl
     * @CreateTime 2021/11/09 10:36
     * @Return Map<String,Object> 返回结果
     */
    @RequestMapping("/getUserInfoList")
    @RequiresPermissions("sys:user:info")
    public Map<String,Object> getUserInfoList(){
        Map<String,Object> map = new HashMap<>();
        List<SysUserEntity> sysUserEntityList = sysUserService.list();
        map.put("sysUserEntityList",sysUserEntityList);
        return map;
    }

    /**
     * 获取角色信息集合
     * @Author gjl
     * @CreateTime 2021/11/09 10:37
     * @Return Map<String,Object> 返回结果
     */
    @RequestMapping("/getRoleInfoList")
    @RequiresPermissions("sys:role:info")
    public Map<String,Object> getRoleInfoList(){
        Map<String,Object> map = new HashMap<>();
        List<SysRoleEntity> sysRoleEntityList = sysRoleService.list();
        map.put("sysRoleEntityList",sysRoleEntityList);
        return map;
    }

    /**
     * 获取权限信息集合
     * @Author gjl
     * @CreateTime 2021/11/09 10:38
     * @Return Map<String,Object>
     */
    @RequestMapping("/getMenuInfoList")
    @RequiresPermissions("sys:menu:info")
    public Map<String,Object> getMenuInfoList(){
        Map<String,Object> map = new HashMap<>();
        List<SysMenuEntity> sysMenuEntityList = sysMenuService.list();
        map.put("sysMenuEntityList",sysMenuEntityList);
        return map;
    }

    /**
     * 获取所有数据
     * @Author gjl
     * @CreateTime 2021/11/09 10:38
     * @Return Map<String,Object>
     */
    @RequestMapping("/getInfoAll")
    @RequiresPermissions("sys:info:all")
    public Map<String,Object> getInfoAll(){
        Map<String,Object> map = new HashMap<>();
        List<SysUserEntity> sysUserEntityList = sysUserService.list();
        map.put("sysUserEntityList",sysUserEntityList);
        List<SysRoleEntity> sysRoleEntityList = sysRoleService.list();
        map.put("sysRoleEntityList",sysRoleEntityList);
        List<SysMenuEntity> sysMenuEntityList = sysMenuService.list();
        map.put("sysMenuEntityList",sysMenuEntityList);
        return map;
    }

    /**
     * 获取所有数据
     * @Author gjl
     * @CreateTime 2021/11/09 10:38
     * @Return Map<String,Object>
     */
    @RequestMapping("/testRole")
    @RequiresPermissions("aa:cc:dd")
    public Map<String,Object> testRole(){
        Map<String,Object> map = new HashMap<>();

        map.put("sysMenuEntityList","aaa");
        return map;
    }

    /**
     * 添加管理员角色权限(测试动态权限更新)
     * @Author gjl
     * @CreateTime 2021/11/09 10:39
     * @Param  username 用户ID
     * @Return Map<String,Object>
     */
    @RequestMapping("/addMenu")
    public Map<String,Object> addMenu(){
        //添加管理员角色权限
        SysRoleMenuEntity sysRoleMenuEntity = new SysRoleMenuEntity();
        sysRoleMenuEntity.setMenuId(4L);
        sysRoleMenuEntity.setRoleId(1L);
        sysRoleMenuService.save(sysRoleMenuEntity);
        //清除缓存
        String username = "admin";
        ShiroUtils.deleteCache(username,false);
        Map<String,Object> map = new HashMap<>();
        map.put("code",200);
        map.put("msg","权限添加成功");
        return map;
    }

    /**
     * 角色授权详情展示
     * @param roleId
     * @return
     */
    @GetMapping("detail")
    @RequiresPermissions("sys:menu:detail")
    public Result getFuncAndMenuByRoleId(Long roleId){
        if (roleId == null){
            return ResultUtil.error(-1,"缺少必要的参数！");
        }
        List<FunctionMenuEntity> funcAndMenuByRoleId = service.getFuncAndMenuByRoleId(roleId);
        return ResultUtil.success(funcAndMenuByRoleId);
    }

    /**
     * 角色授权
     * @param entity
     * @return
     */
    @PostMapping("grant")
    @RequiresPermissions("sys:menu:grant")
    public Result grant(@RequestBody SysRoleFuncMenuEntity entity){
        if (entity.getRoleId() == null){
            return ResultUtil.error(-1,"请选择授权的角色！");
        }
        if (CollectionUtils.isEmpty(entity.getList())){
            return ResultUtil.error(-1,"请选择授权的菜单！");
        }
        Boolean flag = service.grant(entity);
        if (flag){
            //记录日志
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"角色授权ID【"+entity.getRoleId()+"】状态为"+"成功！", Const.SYS_MANAGER_LOG);
            //授权完成清除当前用户缓存信息
            ShiroUtils.deleteCache(ShiroUtils.getUserInfo().getUsername(),true);
            return ResultUtil.success("授权成功！");
        }else {
            //记录失败日志
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"角色授权ID【"+entity.getRoleId()+"】状态为"+"失败！", Const.SYS_MANAGER_LOG);
            return ResultUtil.error(-1,"授权失败");
        }
    }

    /**
     * 添加权限
     * @param entity
     * @return
     */
    @PostMapping("add")
    @RequiresPermissions("sys:menu:add")
    public Result add(@RequestBody SysMenuEntity entity){
        if (entity.getFuctionId() == null){
            return ResultUtil.error(-1,"请选择权限所属菜单");
        }
        if (StringUtils.isEmpty(entity.getPerms())){
            return ResultUtil.error(-1,"请输入权限接口url");
        }
        if (StringUtils.isEmpty(entity.getName())){
            return ResultUtil.error(-1,"请输入权限名称");
        }
        Boolean flag = service.add(entity);
        if (flag){
            //记录日志 TODO
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"添加权限ID【"+entity.getMenuId()+"】 权限名称:【"+entity.getName()+"】 状态为"+"成功！", Const.SYS_MANAGER_LOG);
            return ResultUtil.success("权限添加成功");
        }else {
            //记录日志 TODO
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"添加权限ID【"+entity.getMenuId()+"】状态为"+"失败！", Const.SYS_MANAGER_LOG);
            return ResultUtil.error(-1,"权限添加失败");
        }
    }
}
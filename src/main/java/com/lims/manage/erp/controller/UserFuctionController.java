package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TreeFunction;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysUserFuctionService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.SysRoleFuncMenuVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 获取当前登录用户的菜单列表
     * @param request
     * @return
     */
    @GetMapping("getFunction")
    public Result getFunction(ServletRequest request){
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        List<SysFunction> list = sysUserFuctionService.getFunctionByuserId(userInfo.getUserId());
        return ResultUtil.success(list);
    }

    /**
     * 查询所有权限
     * @return
     */
    @GetMapping("getAllMenu")
    public List<TreeFunction> getMenuDisplay()
    {
        return sysUserFuctionService.GetList();
    }

    /**
     * 查询所有权限_同级展示信息
     * @return
     */
    @GetMapping("getAllMenu_peer")
    public Result getMenuDisplayPeer()
    {
        return ResultUtil.success(sysUserFuctionService.GetListPeer());
    }

    /**
     * 查询角色已有权限
     * @param roleId
     * @return
     */
    @GetMapping("/getRoleMenu")
    //@RequiresRoles("ADMIN")
    public Result getRoleMenu(Long roleId) {
        return ResultUtil.success("查询角色菜单权限成功！", sysUserFuctionService.getRoleMenu(roleId));
    }

    // 暂时未做限制 直接放行 优化。
    @GetMapping("getMenuDisplayNew")
    public Result getMenuDisplayNew()
    {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        return ResultUtil.success(sysUserFuctionService.GetListUpgrade(userInfo.getUserId()));
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
        if(userInfo==null){
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




}

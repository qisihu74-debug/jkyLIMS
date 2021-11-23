package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DingUserService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysUserRoleService;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.SHA256Util;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.RegisterUserInfoVo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc 用户的增、删、改、查
 * @date 2021/11/19 9:39
 * @Copyright © 河南交科院
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private LogManagerService logManagerService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private DingUserService dingUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    /**
     * 用户新增
     * @param vo
     * @return
     */
    @RequestMapping("/addUser")
    @RequiresPermissions("sys:user:insert")
    @Transactional(rollbackFor = Exception.class)
    public Result addUser(@RequestBody RegisterUserInfoVo vo){
        // 随机生成盐值
        String salt = RandomStringUtils.randomAlphanumeric(20);
        String password = SHA256Util.sha256(Const.DEFAULT_PASSWORD, salt);
        //存放sys_user数据
        SysUserEntity entity = new SysUserEntity(vo,password,salt,new Timestamp(new Date(System.currentTimeMillis()).getTime()));
        sysUserService.save(entity);
        //存放sys_ding_user数据
        DingUserEntity dingUserEntity = new DingUserEntity(entity.getUserId().toString(),vo);
        dingUserService.save(dingUserEntity);
        //存放sys_user_role数据
        List<Long> roleIds = vo.getRoleIds();
        for (Long id:roleIds) {
            SysUserRoleEntity roleEntity = new SysUserRoleEntity();
            roleEntity.setUserId(entity.getUserId());
            roleEntity.setRoleId(id);
            sysUserRoleService.save(roleEntity);
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"新增用户【"+vo.getUsername()+"】成功！", Const.CREATE_USER);
        return ResultUtil.success();
    }
}

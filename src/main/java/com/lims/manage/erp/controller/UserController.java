package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DingUserService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysUserRoleService;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.SHA256Util;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.RegisterUserInfoVo;
import com.lims.manage.erp.vo.UserInfoParamVo;
import com.lims.manage.erp.vo.UserInfoVo;
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
@RequestMapping("/user/")
public class UserController {

    @Autowired
    SysUserService sysUserService;

    @Autowired
    private LogManagerService logManagerService;

    @Autowired
    private DingUserService dingUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    /**
     *获取用户列表
     * @return
     */
    @RequestMapping("list")
    //@RequiresPermissions("sys:user:list")
    public Result getList(@RequestBody UserInfoParamVo vo){
        return ResultUtil.success(sysUserService.getUserInfos(vo));
    }

    /**
     * 用户新增
     * @param vo
     * @return
     */
    @RequestMapping("/addUser")
    //@RequiresPermissions("sys:user:insert")
    @Transactional(rollbackFor = Exception.class)
    public Result addUser(@RequestBody RegisterUserInfoVo vo){
        // 随机生成盐值
        String salt = RandomStringUtils.randomAlphanumeric(20);
        String password = SHA256Util.sha256(Const.DEFAULT_PASSWORD, salt);
        //存放sys_user数据
        SysUserEntity entity = new SysUserEntity(vo,password,salt,new Timestamp(new Date(System.currentTimeMillis()).getTime()));
        sysUserService.save(entity);
//        //存放sys_ding_user数据
//        DingUserEntity dingUserEntity = new DingUserEntity(entity.getUserId().toString(),vo);
//        dingUserService.save(dingUserEntity);
        //存放sys_user_role数据
        List<Long> roleIds = vo.getRoleIds();
        for (Long id:roleIds) {
            SysUserRoleEntity roleEntity = new SysUserRoleEntity();
            roleEntity.setUserId(entity.getUserId());
            roleEntity.setRoleId(id);
            sysUserRoleService.save(roleEntity);
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"新增用户【"+vo.getUsername()+"】成功！", Const.CREATE_USER,true);
        return ResultUtil.success();
    }

    /**
     * 更改用户状态（启用/停用账号）
     * @param userEntity
     * @return
     */
    @RequestMapping("/changeState")
    //@RequiresPermissions("sys:user:changestate")
    public Result changeState(@RequestBody SysUserEntity userEntity){
        if(userEntity == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        Boolean isSuccess = sysUserService.updateUserState(userEntity);
        if(isSuccess){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改用户【"+userEntity.getUsername()+"】状态为"+userEntity.getState()+"成功！", Const.CHANGE_STATE,true);
            return ResultUtil.success();
        }else{
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改用户【"+userEntity.getUsername()+"】状态为"+userEntity.getState()+"失败！", Const.CHANGE_STATE,false);
            return ResultUtil.error(ResultEnum.CHANGE_USER_STATE.getCode(),ResultEnum.CHANGE_USER_STATE.getMsg());
        }
    }

    /**
     * 重置密码
     * @param userEntity
     * @return
     */
    @RequestMapping("/resetPassword")
    //@RequiresPermissions("sys:user:resetpassword")
    public Result resetPassword(@RequestBody SysUserEntity userEntity){
        // 随机生成盐值
        String salt = RandomStringUtils.randomAlphanumeric(20);
        String password = SHA256Util.sha256(Const.DEFAULT_PASSWORD, salt);
        userEntity.setPassword(password);
        userEntity.setSalt(salt);
        Boolean isSuccess = sysUserService.resetPassword(userEntity);
        if(isSuccess){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改用户【"+userEntity.getUsername()+"】密码成功", Const.RESET_PASSWORD,true);
            return ResultUtil.success();
        }else{
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"重置用户【"+userEntity.getUsername()+"】密码失败", Const.RESET_PASSWORD,false);
            return ResultUtil.error(ResultEnum.RESET_PASSWORD.getCode(),ResultEnum.RESET_PASSWORD.getMsg());
        }
    }

    /**
     * 修改密码
     * @param userEntity
     * @return
     */
    @RequestMapping("/updatePassword")
    //@RequiresPermissions("sys:user:updatepassword")
    public Result updatePassword(@RequestBody SysUserEntity userEntity){
        //验证旧密码
        SysUserEntity userInfo = ShiroUtils.getUserInfo();

        // 随机生成盐值
        String salt = RandomStringUtils.randomAlphanumeric(20);
        String password = SHA256Util.sha256(userEntity.getPassword(), salt);
        userEntity.setPassword(password);
        userEntity.setSalt(salt);
        Boolean isSuccess = sysUserService.resetPassword(userEntity);
        if(isSuccess){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改密码成功", Const.UPDATE_PASSWORD,true);
            return ResultUtil.success();
        }else{
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改密码失败", Const.UPDATE_PASSWORD,false);
            return ResultUtil.error(ResultEnum.UPDATE_PASSWORD.getCode(),ResultEnum.UPDATE_PASSWORD.getMsg());
        }
    }

    /**
     *更新用户信息
     * @return
     */
    @RequestMapping("/updateUserInfo")
//    @RequiresPermissions("sys:user:updateuserinfo")
    public Result updateUserInfo(@RequestBody UserInfoVo vo){
        Boolean isSuccess = sysUserService.updateUserInfo(vo);
        if(isSuccess){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改用户【"+vo.getUsername()+"】信息成功！", Const.UPDATE_USERINFO,true);
            return ResultUtil.success();
        }else{
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改用户【"+vo.getUsername()+"】信息失败！", Const.UPDATE_USERINFO,false);
            return ResultUtil.error(ResultEnum.UPDATE_USERINFO.getCode(),ResultEnum.UPDATE_USERINFO.getMsg());
        }
    }

}

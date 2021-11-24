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
import com.lims.manage.erp.entity.SysUserTreeEntity;
import com.lims.manage.erp.service.SysUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("list")
    @RequiresPermissions("sys:user:list")
    public Map<String,Object> methodGetList(SysUserTreeEntity sysUserTreeEntity) {
        System.out.println("得到的部门ID"+sysUserTreeEntity.getDepartment());
        // 返回结果
        Map<String,Object> map = new HashMap<>();
        try {
            if(sysUserTreeEntity.getDepartment()!=null && sysUserTreeEntity.getDepartment()!=""){
                List<SysUserTreeEntity> dataList = sysUserService.selectUserList(sysUserTreeEntity.getDepartment());
                if(dataList.size()>0){
                    map.put("data",dataList);
                    map.put("code",200);
                    map.put("msg","查询成功");
                    return map;
                }
            }
            else if(sysUserTreeEntity.getLoginName()!=null||sysUserTreeEntity.getMobile()!=null||sysUserTreeEntity.getState()!=null){
                List<SysUserTreeEntity> dataList =  sysUserService.selectUserLikeList(sysUserTreeEntity);
                if(dataList.size()>0){
                    map.put("data",dataList);
                    map.put("code",200);
                    map.put("msg","查询成功");
                    return map;
                }
            }
            else {
                List<SysUserTreeEntity> dataList =  sysUserService.selectUserAllList();
                if(dataList.size()>0){
                    map.put("data",dataList);
                    map.put("code",200);
                    map.put("msg","查询成功");
                    return map;
                }

            }

            map.put("code",204);
            map.put("msg","缺少必要参数");
            return map;

        }
        catch (Exception e){

        }
        map.put("code",204);
        map.put("msg","缺少必要参数");
        return map;


    }

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

    /**
     * 更改用户状态（启用/停用账号）
     * @param userEntity
     * @return
     */
    @RequestMapping("/changeState")
    @RequiresPermissions("sys:user:changestate")
    public Result changeState(@RequestBody SysUserEntity userEntity){
        if(userEntity == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        Boolean isSuccess = sysUserService.updateUserState(userEntity);
        if(isSuccess){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改用户【"+userEntity.getUsername()+"】状态为"+userEntity.getState()+"成功！", Const.CHANGE_STATE);
            return ResultUtil.success();
        }else{
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改用户【"+userEntity.getUsername()+"】状态为"+userEntity.getState()+"失败！", Const.CHANGE_STATE);
            return ResultUtil.error(ResultEnum.CHANGE_USER_STATE.getCode(),ResultEnum.CHANGE_USER_STATE.getMsg());
        }
    }

    @RequestMapping("/resetPassword")
    @RequiresPermissions("sys:user:resetpassword")
    public Result resetPassword(@RequestBody SysUserEntity userEntity){
        // 随机生成盐值
        String salt = RandomStringUtils.randomAlphanumeric(20);
        String password = SHA256Util.sha256(Const.DEFAULT_PASSWORD, salt);
        userEntity.setPassword(password);
        userEntity.setSalt(salt);
        Boolean isSuccess = sysUserService.resetPassword(userEntity);
        if(isSuccess){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"修改用户【"+userEntity.getUsername()+"】密码成功", Const.RESET_PASSWORD);
            return ResultUtil.success();
        }else{
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"重置用户【"+userEntity.getUsername()+"】密码失败", Const.RESET_PASSWORD);
            return ResultUtil.error(ResultEnum.RESET_PASSWORD.getCode(),ResultEnum.RESET_PASSWORD.getMsg());
        }
    }
}

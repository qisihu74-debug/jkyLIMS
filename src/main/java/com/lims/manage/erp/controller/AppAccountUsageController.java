package com.lims.manage.erp.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysUserRoleService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Author: DLC
 * @Date: 2023/4/3 15:09
 */
@Slf4j
@RestController
@RequestMapping("/app/accountUsage/")
public class AppAccountUsageController {

    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private LogManagerService logManagerService;

    /**
     * 登录
     * @Author gjl
     * @CreateTime 2021/11/09 9:21
     */
    @RequestMapping("/login")
    public Map<String,Object> login(@RequestBody SysUserEntity sysUserEntity){
        Map<String,Object> map = new HashMap<>();
        //进行身份验证
        try{
            //验证身份和登陆
            Subject subject = SecurityUtils.getSubject();
            if(sysUserEntity.getUsername()==null||sysUserEntity.getPassword()==null){
                map.put("code",500);
                map.put("msg","用户或者密码都不能为空");
                return map;
            }
            UsernamePasswordToken token = new UsernamePasswordToken(sysUserEntity.getUsername(), sysUserEntity.getPassword());
            //进行登录操作
            subject.login(token);
        }catch (IncorrectCredentialsException e) {
            map.put("code",500);
            map.put("msg","用户不存在或者密码错误");
            return map;
        } catch (LockedAccountException e) {
            map.put("code",500);
            map.put("msg","登录失败，该用户已被冻结");
            return map;
        } catch (AuthenticationException e) {
            map.put("code",500);
            map.put("msg","该用户不存在");
            return map;
        } catch (Exception e) {
            map.put("code",500);
            map.put("msg","未知异常");
            return map;
        }
        map.put("code",200);
        map.put("msg","登录成功");
        Map<String,Object> data = new HashMap<>();
        map.put("data", data);
        data.put("token", ShiroUtils.getSession().getId().toString());
        SysUserEntity userData = ShiroUtils.getUserInfo();
        // 用户名
        String username = userData.getUsername();
        // 账号
        String name = userData.getName();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username",username);
        jsonObject.put("name",name);
        data.put("userInfo",jsonObject);

        //获取用户的角色列表
        List<SysUserRoleEntity> roleIdList = sysUserRoleService.list(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getUserId, ShiroUtils.getUserInfo().getUserId()).select(SysUserRoleEntity::getRoleId));
        //只保存角色id
        List<String> roleList=roleIdList.stream().map(a -> String.valueOf(a.getRoleId())).collect(Collectors.toList());
        userData.setRoleList(roleList);
//        map.put("userInfo", userData);
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userData.getUsername()+"登陆成功!", Const.LOGIN_LOG,true);
        return map;
    }


}

package com.lims.manage.erp.controller;


import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysUserRoleService;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.SHA256Util;
import com.lims.manage.erp.util.ShiroUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 用户登录
 * @Author gjl
 * @CreateTime 2021/11/09 15:21
 */
@RestController
@RequestMapping("/userLogin")
public class UserLoginController {

    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserRoleService sysUserRoleService;

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
        map.put("token", ShiroUtils.getSession().getId().toString());
        SysUserEntity userData = ShiroUtils.getUserInfo();
        userData.setPassword(null);
        userData.setSalt(null);
        map.put("userInfo", userData);
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userData.getUsername()+"登陆成功!", Const.LOGIN_LOG,true);
        return map;
    }
    /**
     * 未登录
     * @Author gjl
     * @CreateTime 2021/11/09 9:22
     */
    @RequestMapping("/unauth")
    public Map<String,Object> unauth(){
        Map<String,Object> map = new HashMap<>();
        map.put("code",500);
        map.put("msg","未登录");
        return map;
    }


    /**
     * 添加一个用户演示接口
     * 这里仅作为演示不加任何权限和重复查询校验
     * @Author gjl
     * @CreateTime 2020/1/6 9:22
     */
    @RequestMapping("/testAddUser")
    public Map<String,Object> testAddUser(String userName,String passWord){
        // 设置基础参数
        SysUserEntity sysUser = new SysUserEntity();
        sysUser.setUsername(userName);
        sysUser.setState("NORMAL");
        // 随机生成盐值
        String salt = RandomStringUtils.randomAlphanumeric(20);
        sysUser.setSalt(salt);
        // 进行加密
        sysUser.setPassword(SHA256Util.sha256(passWord, sysUser.getSalt()));
        // 保存用户
        sysUserService.save(sysUser);
        // 保存角色
        SysUserRoleEntity sysUserRoleEntity = new SysUserRoleEntity();
        sysUserRoleEntity.setUserId(sysUser.getUserId()); // 保存用户完之后会把ID返回给用户实体
        sysUserRoleService.save(sysUserRoleEntity);
        // 返回结果
        Map<String,Object> map = new HashMap<>();
        map.put("code",0);
        map.put("msg","添加成功");
        return map;
    }

    /**
     * 用户推出登陆
     * @return
     */
    @GetMapping("/logOut")
    public Result logOut(){
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo!=null){
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+ShiroUtils.getUserInfo().getUsername()+"退出登陆成功!", Const.LOGIN_LOG_OUT,true);
        ShiroUtils.logout();
        }
        return ResultUtil.success("用户退出登陆成功！");
    }

}

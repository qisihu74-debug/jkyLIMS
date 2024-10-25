package com.lims.manage.erp.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.config.manager.AsyncManager;
import com.lims.manage.erp.config.manager.factory.AsyncFactory;
import com.lims.manage.erp.constant.Constants;
import com.lims.manage.erp.entity.DynamicImg;
import com.lims.manage.erp.entity.SysLog;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.mapper.TestTechnicistDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DynamicImgService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.RtspToHlsService;
import com.lims.manage.erp.service.SysUserRoleService;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Autowired
    private DynamicImgService dynamicImgService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    private RtspToHlsService rtspToHlsService;

    @GetMapping("convert")
    public Result convert(String rtspUrl, String hlsOutputPath){
        String url = null;
        try {
            url = rtspToHlsService.startTranscoding(rtspUrl,hlsOutputPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ResultUtil.success(url);
    }


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
        Integer byUserId = sysUserService.getTechnicistIdByUserId(userData.getUserId());
        userData.setTechnicistId(byUserId==null?0:byUserId);
        //获取用户的角色列表
        List<SysUserRoleEntity> roleIdList = sysUserRoleService.list(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getUserId, ShiroUtils.getUserInfo().getUserId()).select(SysUserRoleEntity::getRoleId));
        //只保存角色id
        List<String> roleList=roleIdList.stream().map(a -> String.valueOf(a.getRoleId())).collect(Collectors.toList());
        userData.setRoleList(roleList);
        map.put("userInfo", userData);
        // 根据 token 存储 用户信息
        redisUtil.setRedisTokenUser((String) map.get("token"),userData);
//        AsyncManager.me().execute(AsyncFactory.recordLogininfor(userData.getUsername(), Constants.LOGIN_SUCCESS, "登录成功"));
        return map;
    }

    /**
     * 登录
     * @Author gjl
     * @CreateTime 2021/11/09 9:21
     */
    @RequestMapping("/mobileLogin")
    public Map<String,Object> mobileLogin(@RequestBody SysUserEntity sysUserEntity){
        String token = ShiroUtils.getToken(sysUserEntity.getUsername());

        Map<String,Object> map = new HashMap<>();
        map.put("code",200);
        map.put("msg","登录成功");
        map.put("token", token);
        SysUserEntity userData = sysUserService.selectUserByName(sysUserEntity.getUsername());
        userData.setPassword(null);
        userData.setSalt(null);
        map.put("userInfo", userData);
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(userData.getUsername(), Constants.LOGIN_SUCCESS, "手机登录成功"));
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
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(ShiroUtils.getUserInfo().getUsername(), Constants.LOGIN_SUCCESS, "退出成功"));
            ShiroUtils.logout();
        }
        return ResultUtil.success("用户退出登陆成功！");
    }

    /**
     * 管理员上传客户委托系统的轮播图片
     * @return
     */
    @Log(title = "个人中心", businessType = BusinessType.UPDATE)
    @PostMapping("uploadImgs")
    public Result uploadImgs(@RequestParam(value = "json") String json, MultipartFile[] file){
        DynamicImg dynamicImg = JSON.parseObject(json,DynamicImg.class);
        if (file == null){
            return ResultUtil.error("缺少必要参数");
        }
        List<String> urls = Lists.newArrayList();
        for (MultipartFile multipartFile:file) {
            String name = multipartFile.getOriginalFilename();
            String url = MinIoUtil.upload("active-img", multipartFile, name);
            String substring = url.substring(0, url.indexOf("?"));
            urls.add(substring);
        }
        String s = "";
        for (int i =0;i<urls.size();i++){
            s = s + urls.get(i);
            if (i != urls.size()-1){
                s = s + ",";
            }
        }
        dynamicImg.setImgUrl(s);
        List<DynamicImg> list = dynamicImgService.list();
        DynamicImg img = null;
        if (CollectionUtils.isNotEmpty(list)){
            img = list.get(0);
        }
        dynamicImgService.delete();
        //从新设置url
        String[] split1 = null;
                String[] split = img.getImgUrl().split(",");
        if (StringUtils.isNotEmpty(dynamicImg.getImgUrl())){
            split1 = dynamicImg.getImgUrl().split(",");
        }
        Set<String> set = new HashSet<>();
        for (String url:split) {
            set.add(url);
        }
        if (split1 != null && split1.length>0){
            for (String url:split1) {
                set.add(url);
            }
        }
        String ss = "";
        Object[] objects = set.toArray();
        for (int i =0;i<objects.length;i++){
            ss = ss + objects[i];
            if (i != set.size()-1){
                ss = ss + ",";
            }
        }
        dynamicImg.setImgUrl(ss);
        boolean batch = dynamicImgService.save(dynamicImg);
        if (batch){
            return ResultUtil.success("上传成功");
        }else {
            return ResultUtil.error("上传失败");
        }
    }

    /**
     * 查询已上传的图片
     * @return
     */
    @GetMapping("getImgList")
    public Result getImgList(){
        DynamicImg dynamicImg = dynamicImgService.list().get(0);
        List<String> list = Lists.newArrayList();
        if (StringUtils.isNotEmpty(dynamicImg.getImgUrl())){
            String[] strings = dynamicImg.getImgUrl().split(",");
            for (String url:strings) {
                list.add(url);
            }
        }
        dynamicImg.setUrls(list);
        return ResultUtil.success(list);
    }

    /**
     * 查询已上传的图片
     * @return
     */
    @GetMapping("getTitle")
    public Result getImg(){
        DynamicImg dynamicImg = dynamicImgService.list().get(0);
        Map<String,String> map = new HashedMap();
        map.put("title",dynamicImg.getTitle());
        map.put("content",dynamicImg.getContent());
        map.put("filingInfo",dynamicImg.getFilingInfo());
        map.put("topDesc",dynamicImg.getTopDesc());
        return ResultUtil.success(map);
    }

    /**
     * 删除图片
     * @return
     */
    @GetMapping("deleteImg")
    @Transactional(rollbackFor = Exception.class)
    public Result deleteImg(String url){
        if (StringUtils.isEmpty(url)){
            return ResultUtil.error("缺少必要的参数");
        }
        LambdaQueryWrapper<DynamicImg> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        DynamicImg dynamicImg = dynamicImgService.getOne(lambdaQueryWrapper);
        String[] split = dynamicImg.getImgUrl().split(",");
        List<String> urls = Lists.newArrayList();
        for (String s:split) {
            if (!s.equals(url)){
                urls.add(s);
            }
        }
        //组装url
        String ss = "";
        for (int i =0;i<urls.size();i++){
            ss = ss + urls.get(i);
            if (urls.size()>1){
                if (i != urls.size()-1){
                    ss = ss + ",";
                }
            }
        }
       //更新url
        LambdaUpdateWrapper<DynamicImg> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(DynamicImg::getId,dynamicImg.getId());
        updateWrapper.set(DynamicImg::getImgUrl,ss);
        dynamicImgService.update(null,updateWrapper);
        //删除文件服务器的图片
        String[] strings = url.split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        MinIoUtil.deleteFile(bluckName,fileName);
        return ResultUtil.success("删除成功");
    }

    /**
     * 日志列表查询
     * @param logType
     * @param pageNum
     * @param pageSize
     * @param operator
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("getLogList")
    public Result getLogList(Integer logType, Integer pageNum, Integer pageSize,
                             String operator, Long startDate, Long endDate){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数");
        }
        PageInfo<SysLog> pageInfo = logManagerService.getLogList(logType,pageNum,pageSize,operator,startDate,endDate);
        return ResultUtil.success(pageInfo);
    }
}

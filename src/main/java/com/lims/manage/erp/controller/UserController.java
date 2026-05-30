package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.mapper.SysUserRoleDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.AccountValidatorUtil;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.SHA256Util;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.RegisterUserInfoVo;
import com.lims.manage.erp.vo.SysUserPasswordVo;
import com.lims.manage.erp.vo.UserInfoParamVo;
import com.lims.manage.erp.vo.UserInfoVo;
import lombok.SneakyThrows;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
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

    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysUserRoleDao sysUserRoleDao;
    @Autowired
    private DeptService deptService;


    /**
     * 获取所有用户名称列表
     * @return Result<?>
     */
    @GetMapping(value = "getUserNameList")
    public Result<?> getUserNameList(){
        return ResultUtil.success("获取用户名称列表",sysUserService.getUserNameList());
    }

    /**
     * 获取除了当前用户外所有用户名称列表
     * @return Result<?>
     */
    @GetMapping(value = "getExceptUserNameList")
    public Result<?> getExceptUserNameList(){
        return ResultUtil.success("获取除了当前用户外所有用户名称列表",sysUserService.getExceptUserNameList());
    }

    /**
     * 获取用户列表——二次开发
     *
     * @return
     */
    @PostMapping("list")
//    @RequiresPermissions("sys:user:list")
    public Result getList(@RequestBody UserInfoParamVo vo) {
        return ResultUtil.success(sysUserService.getUserInfos(vo));
    }

    /**
     * 查询人员信息
     *
     * @param search
     * @return
     */
    @GetMapping("getUserList")
    public Result personList(String search) {
        return ResultUtil.success(sysUserService.personList(search));
    }

    /**
     * 用户新增
     *
     * @param vo
     * @return
     */
    @RequestMapping("/addUser")
//    @RequiresPermissions("sys:user:insert")
    @Transactional(rollbackFor = Exception.class)
    public Result addUser(@RequestBody UserInfoVo vo) {
        if (vo.getUsername() == null) {
            return ResultUtil.error("登陆账号不能为空");
        }
        if (vo.getMobile() == null) {
            return ResultUtil.error("手机号不能为空");
        }
        // 效验账号格式
        if (!AccountValidatorUtil.isUsername(vo.getUsername())) {
            return ResultUtil.error("用户名不符合定义");
        }
        // 效验手机号格式
        if (!AccountValidatorUtil.isMobile(vo.getMobile())) {
            return ResultUtil.error("手机号不符合通用定义");
        }
        // 效验emal
        if (vo.getEmail() != null) {
            if (!AccountValidatorUtil.isEmail(vo.getEmail())) {
                return ResultUtil.error("emal不符合通用定义");
            }
        }
        // 用户新增，账号不能重复
        if (sysUserDao.getOne(vo.getUsername()) != null) {
            return ResultUtil.error("当前账号已存在");
        }
        if (CollectionUtil.isNotEmpty(vo.getRoleIdsLong())) {
            for (Long roleId : vo.getRoleIdsLong()) {
                if (roleId == 999) {
                    // 查询 最高管理者角色 拥有
                    List<Long> topManagementList = sysUserRoleDao.selectTopManagement();
                    if (CollectionUtil.isNotEmpty(topManagementList)) {
                        return ResultUtil.error("创建失败： 最高管理者角色 已经被授权");
                    }
                }
            }
        }
        // 查询此账号是否存在
        DingUserEntity sysDingUserData = dingUserService.getById(vo.getDingUserId());
        if (sysDingUserData == null) {
            return ResultUtil.error("使用人不存在！，请重新选择使用人");
        }
        // 钉钉用户id 存在其他使用人
        if (!sysUserService.getTheUserList(vo.getDingUserId())) {
            return ResultUtil.error("使用人 已拥有账号");
        }


        vo.setUserId(GenID.getID());
        RegisterUserInfoVo userInfoVo = new RegisterUserInfoVo();
        // 处理部门信息
        if (CollectionUtil.isNotEmpty(vo.getDepartmentIdLong())) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            for (Long deptId : vo.getDepartmentIdLong()) {
                stringBuilder.append("" + deptId + ",");
            }
            stringBuilder.append("]");
            userInfoVo.setDeptId(stringBuilder.toString());
        }
        userInfoVo.setUsername(vo.getUsername());
        userInfoVo.setMobile(vo.getMobile());
        userInfoVo.setEmail(vo.getEmail());
        // 随机生成盐值
        String salt = RandomStringUtils.randomAlphanumeric(20);
        String password = SHA256Util.sha256(Const.DEFAULT_PASSWORD, salt);
        // 默认账号正常启动
        userInfoVo.setState("NORMAL");
        //存放sys_user数据
        SysUserEntity entity = new SysUserEntity(userInfoVo, password, salt, new Timestamp(new Date(System.currentTimeMillis()).getTime()));
        if (vo.getDingUserId() != null) {
            entity.setDingUserId(vo.getDingUserId());
        }
        entity.setTime(new Timestamp(new Date(System.currentTimeMillis()).getTime()));
        entity.setUserId(vo.getUserId());
        if (StringUtils.isNotEmpty(sysDingUserData.getName())) {
            entity.setName(sysDingUserData.getName());
        }
        sysUserService.save(entity);
        // 角色信息
        if (vo.getRoleIdsLong() != null && vo.getRoleIdsLong().size() > 0) {
            List<SysUserRoleEntity> newRoles = Lists.newArrayList();
            //增加新权限
            for (Long roleId : vo.getRoleIdsLong()) {
                SysUserRoleEntity roleEntity = new SysUserRoleEntity();
                roleEntity.setUserId(entity.getUserId());
                roleEntity.setRoleId(roleId);
                newRoles.add(roleEntity);
            }
            for (SysUserRoleEntity roleEntity : newRoles) {
                sysUserRoleDao.insertNewRole(roleEntity);
            }
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "新增用户【" + vo.getUsername() + "】成功！", Const.CREATE_USER, true);
        return ResultUtil.success();
    }

    /**
     * 更改用户状态（启用/停用账号）
     *
     * @param userEntity
     * @return
     */
    @RequestMapping("/changeState")
//    @RequiresPermissions("sys:user:changestate")
    public Result changeState(@RequestBody SysUserEntity userEntity) {
        if (userEntity == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        if (userEntity.getUserId() == null) {
            return ResultUtil.error("用户ID不能为空");
        }
        if (userEntity.getState() == null) {
            return ResultUtil.error("状态不能为空");
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token已过期，请重新登录");
        }
        userEntity.setTime(new Timestamp(new Date(System.currentTimeMillis()).getTime()));
        Boolean isSuccess = sysUserService.updateUserState(userEntity);
        if (isSuccess) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改用户【" + userEntity.getUsername() + "】状态为" + userEntity.getState() + "成功！", Const.CHANGE_STATE, true);
            return ResultUtil.success();
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改用户【" + userEntity.getUsername() + "】状态为" + userEntity.getState() + "失败！", Const.CHANGE_STATE, false);
            return ResultUtil.error(ResultEnum.CHANGE_USER_STATE.getCode(), ResultEnum.CHANGE_USER_STATE.getMsg());
        }
    }

    /**
     * 重置密码
     *
     * @param userEntity
     * @return
     */
    @RequestMapping("/resetPassword")
//    @RequiresPermissions("sys:user:resetpassword")
    public Result resetPassword(@RequestBody SysUserEntity userEntity) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已失效");
        }
        if (userEntity.getUserId() == null) {
            return ResultUtil.error("用户ID不能为空");
        }
        // 随机生成盐值
        String salt = RandomStringUtils.randomAlphanumeric(20);
        String password = SHA256Util.sha256(Const.DEFAULT_PASSWORD, salt);
        userEntity.setPassword(password);
        userEntity.setSalt(salt);
        userEntity.setTime(new Timestamp(new Date(System.currentTimeMillis()).getTime()));
        Boolean isSuccess = sysUserService.resetPassword(userEntity);
        if (isSuccess) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改用户【" + userEntity.getUsername() + "】密码成功", Const.RESET_PASSWORD, true);
            return ResultUtil.success();
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "重置用户【" + userEntity.getUsername() + "】密码失败", Const.RESET_PASSWORD, false);
            return ResultUtil.error(ResultEnum.RESET_PASSWORD.getCode(), ResultEnum.RESET_PASSWORD.getMsg());
        }
    }

    /**
     * 修改密码
     *
     * @param
     * @return
     */
    @RequestMapping("/updatePassword")
//    @RequiresPermissions("sys:user:updatepassword")
    public Result updatePassword(@RequestBody SysUserPasswordVo sysUserPasswordVo) {
        //验证旧密码
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已失效");
        }
        if (sysUserPasswordVo.getUsername() == null) {
            return ResultUtil.error("用户为空");
        }
        if (sysUserPasswordVo.getOldPassword() == null) {
            return ResultUtil.error("旧密码为空");
        }
        if (sysUserPasswordVo.getNewPassword() == null) {
            return ResultUtil.error("新密码为空");
        }
        // 效验新密码
        if (!AccountValidatorUtil.isPassword(sysUserPasswordVo.getNewPassword())) {
            return ResultUtil.error("密码和数字组成大于等于6位");
        }
        // 旧密码
        SysUserEntity oldData = sysUserDao.getUserInformation(sysUserPasswordVo.getUsername());
        if (oldData == null) {
            return ResultUtil.error("此账号不存在");
        }
        if (!oldData.getUsername().equals(sysUserPasswordVo.getUsername())) {
            return ResultUtil.error("用户名不匹配");
        }
        // 页面传递的密码
        String password = SHA256Util.sha256(sysUserPasswordVo.getOldPassword(), oldData.getSalt());
        if (!oldData.getPassword().equals(password)) {
            return ResultUtil.error("输入旧密码不对");
        }
        // 随机生成盐值
        String salt = RandomStringUtils.randomAlphanumeric(20);
        String newpassword = SHA256Util.sha256(sysUserPasswordVo.getNewPassword(), salt);
        SysUserEntity userEntity = new SysUserEntity(oldData.getUserId(), sysUserPasswordVo.getUsername(), newpassword, salt);
        userEntity.setTime(new Timestamp(new Date(System.currentTimeMillis()).getTime()));
        Boolean isSuccess = sysUserService.resetPassword(userEntity);
        if (isSuccess) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改密码成功", Const.UPDATE_PASSWORD, true);
            return ResultUtil.success();
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改密码失败", Const.UPDATE_PASSWORD, false);
            return ResultUtil.error(ResultEnum.UPDATE_PASSWORD.getCode(), ResultEnum.UPDATE_PASSWORD.getMsg());
        }
    }

    /**
     * 更新用户信息
     *
     * @return
     */
    @RequestMapping("/updateUserInfo")
//    @RequiresPermissions("sys:user:updateuserinfo")
    public Result updateUserInfo(@RequestBody UserInfoVo vo) {

        if (vo.getUsername() == null) {
            return ResultUtil.error("登陆账号不能为空");
        }
        if (vo.getMobile() == null) {
            return ResultUtil.error("手机号不能为空");
        }
        if (vo.getUserId() == null) {
            return ResultUtil.error("缺少必填参数");
        }
        // 效验账号格式
        if (!AccountValidatorUtil.isUsername(vo.getUsername())) {
            return ResultUtil.error("用户名不符合定义");
        }
        // 效验手机号格式
        if (!AccountValidatorUtil.isMobile(vo.getMobile())) {
            return ResultUtil.error("手机号不符合通用定义");
        }
        // 效验emal
        if (vo.getEmail() != null) {
            if (!AccountValidatorUtil.isEmail(vo.getEmail())) {
                return ResultUtil.error("emal不符合通用定义");
            }
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token已过期，请重新登录");
        }
        if (vo.getDingUserId() != null && vo.getDingUserId().length() > 0) {
            // 查询此账号是否存在
            DingUserEntity sysDingUserData = dingUserService.getById(vo.getDingUserId());
            if (sysDingUserData == null) {
                return ResultUtil.error("使用人不存在！，请重新选择使用人");
            }
            if (sysDingUserData.getName() != null && !sysDingUserData.getName().isEmpty()) {
                vo.setName(sysDingUserData.getName());
            }
            // 钉钉用户id 是否被其他账号使用
            Boolean flag = sysUserService.getTheUser(vo.getUserId(), vo.getDingUserId());
            // =true 使用人变动
            if (flag) {
                // 钉钉用户id 存在其他使用人
                if (!sysUserService.getTheUserList(vo.getDingUserId())) {
                    return ResultUtil.error("使用人 已拥有账号");
                }
            }
        }
        if (CollectionUtil.isNotEmpty(vo.getRoleIdsLong())) {
            for (Long roleId : vo.getRoleIdsLong()) {
                if (roleId == 999) {
                    // 查询 最高管理者角色 拥有
                    List<Long> topManagementList = sysUserRoleDao.selectTopManagement();
                    if (CollectionUtil.isNotEmpty(topManagementList)) {
                        if (!topManagementList.get(0).equals(vo.getUserId())) {
                            return ResultUtil.error("编辑失败： 最高管理者角色 已经被授权");
                        }
                    }
                }
            }
        }
        // 修改时间
        vo.setTime(new Timestamp(new Date(System.currentTimeMillis()).getTime()));
        // 用户修改时，当前账号不能与其他账号重复
        // 获取用户名 为空 发生改变
        if (sysUserDao.getOldData(vo.getUsername(), vo.getUserId()) == null) {
            if (sysUserDao.getOne(vo.getUsername()) != null) {
                return ResultUtil.error("当前账号已存在");
            }
            // 更改用户时： 通过userId 和 员工使用人 查询绑定问题 并返回 部门集合 List<Long>
            List<Long> deptList = deptService.getDepartmentIdLong(vo.getUserId(), vo.getDingUserId());
            vo.setDepartmentIdLong(deptList);
            Boolean isSuccess = sysUserService.updateUserInfo(vo);
            if (isSuccess) {
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改用户【" + vo.getUsername() + "】信息成功！", Const.UPDATE_USERINFO, true);
                return ResultUtil.success();
            } else {
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改用户【" + vo.getUsername() + "】信息失败！", Const.UPDATE_USERINFO, false);
                return ResultUtil.error(ResultEnum.UPDATE_USERINFO.getCode(), ResultEnum.UPDATE_USERINFO.getMsg());
            }
        } else {
            // 更改用户时： 通过userId 和 员工使用人 查询绑定问题 并返回 部门集合 List<Long>
            List<Long> deptList = deptService.getDepartmentIdLong(vo.getUserId(), vo.getDingUserId());
            vo.setDepartmentIdLong(deptList);
            Boolean isSuccess = sysUserService.updateUserInfo(vo);
            if (isSuccess) {
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改用户【" + vo.getUsername() + "】信息成功！", Const.UPDATE_USERINFO, true);
                return ResultUtil.success();
            } else {
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "修改用户【" + vo.getUsername() + "】信息失败！", Const.UPDATE_USERINFO, false);
                return ResultUtil.error(ResultEnum.UPDATE_USERINFO.getCode(), ResultEnum.UPDATE_USERINFO.getMsg());
            }
        }
    }

    @PostMapping("delete")
    public Result deleteUser(@RequestBody SysUserEntity sysUserEntity) {
        if (sysUserEntity.getUserId() == null) {
            return ResultUtil.error("未选中需要删除的人员id");
        }
        // 查询此账号是否存在
        SysUserEntity userEntity = sysUserService.getById(sysUserEntity.getUserId());
        if (userEntity == null) {
            return ResultUtil.error("账号为空");
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (userEntity.getState().contains("NORMAL")) {
            return ResultUtil.error("用户状态正常 不允许删除");
        }
        boolean statusflag = false;
        try {
            statusflag = sysUserService.removeById(sysUserEntity.getUserId());
        } catch (Exception e) {

        }
        if (statusflag) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "删除系统用户ID【" + sysUserEntity.getUserId() + "】", Const.SYS_MANAGER_LOG, true);
            // 删除角色与用户之间的关系
            sysUserRoleDao.removeOldRole(sysUserEntity.getUserId());
            // 更新 用户与使用人之间的部门关系
            deptService.updateDepartmentId(sysUserEntity.getUserId());
            return ResultUtil.success("删除用户成功");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + ShiroUtils.getUserInfo().getUsername() + "删除系统用户ID【" + sysUserEntity.getUserId() + "】", Const.SYS_MANAGER_LOG, false);
        return ResultUtil.error("删除用户失败");
    }

    /**
     * 上传个人签名
     * @param file
     * @return
     */
    @SneakyThrows
    @RequestMapping("uploadSignature")
    public Result uploadSignature(MultipartFile file){
        File newFile = new File(file.getOriginalFilename());
        if (file == null){
            return ResultUtil.error("请上传签名图片");
        }
        FileUtils.copyInputStreamToFile(file.getInputStream(), newFile);
        Image img = ImageIO.read(newFile); //imgFile为图片文件
        if(img == null){
            return ResultUtil.error("请上传图片");
        }
        boolean flag = sysUserService.uploadSignature(file);
        if (flag){
            return ResultUtil.success("个人签名上传成功");
        }else {
            return ResultUtil.error("个人签名上传失败");
        }
    }

    /**
     * 给无系统用户的人员创建账号
     * @return
     */
    @GetMapping("batchAddUser")
    public Result batchAddUser(){
        //查询所有未被创建账号的信息
        List<DingUserEntity> list = dingUserService.getInfo();
        for (DingUserEntity entity :list){
            UserInfoVo userInfoVo = new UserInfoVo();
            userInfoVo.setName(entity.getName());
            //用户名称默认为姓名全拼
            userInfoVo.setUsername(convertToPinyin(entity.getName()));
            userInfoVo.setMobile(entity.getMobile());
            userInfoVo.setEmail(entity.getEmail());
            List<Long> roles = Lists.newArrayList();
            roles.add(4749275677132642L);
            userInfoVo.setRoleIdsLong(roles);
            userInfoVo.setDingUserId(entity.getUserid());
            Result result = addUser(userInfoVo);
        }
        return ResultUtil.success("添加账号成功");
    }

    /**
     * 将中文姓名转为拼音全称
     * @param chineseName
     * @return
     */
    public String convertToPinyin(String chineseName) {
        StringBuilder pinyin = new StringBuilder();
        char[] nameChars = chineseName.toCharArray();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char nameChar : nameChars) {
            if (Character.toString(nameChar).matches("[\\u4e00-\\u9fa5]+")) { // 判断是否为汉字
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(nameChar, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyin.append(pinyinArray[0]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyin.append(nameChar); // 非汉字字符直接追加
            }
        }
        return pinyin.toString();
    }
}

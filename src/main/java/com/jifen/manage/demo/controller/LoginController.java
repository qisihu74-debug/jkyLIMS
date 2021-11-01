package com.jifen.manage.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jifen.manage.demo.entity.FunctionEntity;
import com.jifen.manage.demo.entity.User;
import com.jifen.manage.demo.filter.PassToken;
import com.jifen.manage.demo.http.HttpClientUtil;
import com.jifen.manage.demo.result.Result;
import com.jifen.manage.demo.result.ResultEnum;
import com.jifen.manage.demo.result.ResultUtil;
import com.jifen.manage.demo.service.FunctionService;
import com.jifen.manage.demo.service.LoginService;
import com.jifen.manage.demo.util.DESUtils;
import com.jifen.manage.demo.util.IdGenerator;
import com.jifen.manage.demo.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author gjl
 */


@RestController
@Slf4j
@RequestMapping("/admin/")
public class LoginController {
   @Autowired
   private LoginService loginService;
   @Autowired
   private FunctionService functionService;

    @PassToken
    @PostMapping("login")
    public Result getadmin(@RequestBody @Valid User user){
        if (StringUtils.isEmpty(user.getUserName())){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NICK.getCode(),ResultEnum.VERIFY_FAIL_NICK.getMsg());
        }
        if (StringUtils.isEmpty(user.getPassWord())){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_PASS_WORD.getCode(),ResultEnum.VERIFY_FAIL_PASS_WORD.getMsg());
        }
        User admin = loginService.getAdmin(user.getUserName());
        if (admin == null){
            return ResultUtil.error(ResultEnum.STUDENT_NOT_EXIST.getCode(),"用户不存在！");
        }
        if (admin.getIsValid() != 1){
            return ResultUtil.error(-1,"用户审核中请耐心等待！");
        }
        String decode = DESUtils.decrypt(admin.getPassWord(),admin.getUserCode());
        if(user.getPassWord().equals(decode)){
           User res=new User();
           res.setUserName(admin.getUserName());
           res.setUserCode(admin.getUserCode());
           res.setPassWord(decode);
           res.setToken(TokenUtil.getToken(res));
           List<FunctionEntity> list = functionService.getFunctionsById(admin.getId());
           res.setList(list);
           return ResultUtil.success(res);
       }else {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL.getCode(),ResultEnum.VERIFY_FAIL.getMsg());
        }
    }

    /**
     *
     * @return
     */
    @PassToken
    @PostMapping("register")
    public Result register(@RequestBody User user){
        if (StringUtils.isEmpty(user.getUserName())){
            return ResultUtil.error(-1,"请输入账号");
        }
        if (StringUtils.isEmpty(user.getPassWord())){
            return ResultUtil.error(-1,"请输入密码");
        }
        if (StringUtils.isEmpty(user.getUserType()) || Integer.valueOf(user.getUserType())<2
                || Integer.valueOf(user.getUserType())>3){
            return ResultUtil.error(-1,"注册类型不正确，非商家或用户！");
        }
        if (user.getIdObverseFile() == null || user.getIdentificationPositive() == null){
            return ResultUtil.error(-1,"请上传身份证正反面照片！");
        }
        if (Integer.valueOf(user.getUserType()) == 3){
            return ResultUtil.error(-1,"请上传营业执照照片！");
        }
        //判断用户是否已注册
        User user1 = loginService.getUserByName(user.getUserName());
        if (user1 != null){
            return ResultUtil.error(-1,user.getUserName()+" :此用户已注册！");
        }
        //判定身份证号和手机号
        User user2 = loginService.getUserById(user.getIdentification());
        if (user2 != null){
            return ResultUtil.error(-1,user.getUserName()+" :身份证已存在无法注册重复使用！");
        }
        User user3 = loginService.getUserByMobile(user.getMobile());
        if (user3 != null){
            return ResultUtil.error(-1,user.getUserName()+" :手机号已存在无法注册重复使用！");
        }
        user.setUserCode(IdGenerator.get());
        String encode = DESUtils.encrypt(user.getPassWord(), user.getUserCode());
        user.setPassWord(encode);
        try {
            loginService.save(user);
            return ResultUtil.success();
        }catch (Exception e){
            log.error("用户注册失败:{}",e);
            return ResultUtil.error(-1,"保存失败");
        }
    }

    /**
     * 账号列表
     * @return
     */
    @GetMapping("adminList")
    public Result adminList(){
        List<User> list = loginService.adminList();
        return ResultUtil.success(list);
    }

    /**
     * 测试http请求
     * @return
     */
    @GetMapping("testHttp")
    public Result testHttp(){
        String url = "https://route.showapi.com/1626-1?isbn=9787208061644&showapi_appid=740854&showapi_sign=5005fe06612c4e419d73a1e3e6d0c3f1";

        Pair<Integer, String> pair = HttpClientUtil.get(url);
        Integer left = pair.getLeft();
        String right = pair.getRight();
        if (left == 200){
            JSONObject jsonObject = JSON.parseObject(right);
            jsonObject.get("date");
        }
        return null;
    }

    @PassToken
    @GetMapping("test")
    public Result test(){
        return ResultUtil.success("请求接口成功==app开发");
    }
}

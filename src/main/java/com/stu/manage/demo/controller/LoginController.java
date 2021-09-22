package com.stu.manage.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.stu.manage.demo.entity.FunctionEntity;
import com.stu.manage.demo.entity.Login;
import com.stu.manage.demo.entity.LoginToken;
import com.stu.manage.demo.filter.PassToken;
import com.stu.manage.demo.http.HttpClientUtil;
import com.stu.manage.demo.result.Result;
import com.stu.manage.demo.result.ResultEnum;
import com.stu.manage.demo.result.ResultUtil;
import com.stu.manage.demo.service.FunctionService;
import com.stu.manage.demo.service.LoginService;
import com.stu.manage.demo.util.CryptoUtil;
import com.stu.manage.demo.util.GenID;
import com.stu.manage.demo.util.TokenUtil;
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
    public Result getadmin(@RequestBody @Valid Login login){
        if (StringUtils.isEmpty(login.getNick())){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NICK.getCode(),ResultEnum.VERIFY_FAIL_NICK.getMsg());
        }
        if (StringUtils.isEmpty(login.getPassWord())){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_PASS_WORD.getCode(),ResultEnum.VERIFY_FAIL_PASS_WORD.getMsg());
        }
        Login admin = loginService.getAdmin(login.getNick());
        String decode = CryptoUtil.decode(admin.getAdminId(), admin.getPassWord());
        if(login.getPassWord().equals(decode)){
           LoginToken res=new LoginToken();
           res.setName(login.getAdminName());
           res.setAdminId(admin.getAdminId());
           res.setToken(TokenUtil.getToken(res));
           res.setNick(login.getNick());
           List<FunctionEntity> list = functionService.getFunctionsById(admin.getId());
           res.setList(list);
           //TODO
           res.setUserInfo(null);
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
    public Result register(@RequestBody Login login){
        if (StringUtils.isEmpty(login.getAdminName())){
            return ResultUtil.error(-1,"请输入账号");
        }
        if (StringUtils.isEmpty(login.getPassWord())){
            return ResultUtil.error(-1,"请输入密码");
        }
        login.setAdminId(GenID.getUUID());
        String encode = CryptoUtil.encode(login.getAdminId(), login.getPassWord());
        login.setPassWord(encode);
        int save = loginService.save(login);
        if (save == 1){
            return ResultUtil.success();
        }else {
            return ResultUtil.error(-1,"保存失败");
        }
    }

    /**
     * 账号列表
     * @return
     */
    @GetMapping("adminList")
    public Result adminList(){
        List<Login> list = loginService.adminList();
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

}

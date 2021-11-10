package com.lims.manage.demo.controller;

import com.lims.manage.demo.entity.SysFunction;
import com.lims.manage.demo.entity.SysUserEntity;
import com.lims.manage.demo.result.Result;
import com.lims.manage.demo.result.ResultUtil;
import com.lims.manage.demo.service.SysUserFuctionService;
import com.lims.manage.demo.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}

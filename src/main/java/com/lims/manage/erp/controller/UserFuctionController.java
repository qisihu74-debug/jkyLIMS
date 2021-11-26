package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TreeFunction;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.SysUserFuctionService;
import com.lims.manage.erp.util.ShiroUtils;
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

    // 暂时未做限制 直接放行。
    @GetMapping("getMenuDisplay")
    public List<TreeFunction> getMenuDisplay()
    {
        return sysUserFuctionService.GetList();
    }

    // 暂时未做限制 直接放行 优化。
    @GetMapping("getMenuDisplayNew")
    public List<TreeFunction> getMenuDisplayNew()
    {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        return sysUserFuctionService.GetListUpgrade(userInfo.getUserId());
    }




}

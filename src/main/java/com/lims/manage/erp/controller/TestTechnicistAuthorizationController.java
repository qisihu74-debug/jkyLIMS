package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.extension.api.ApiController;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestTechnicistAuthorizationService;
import com.lims.manage.erp.vo.AuthorizationSaveReq;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.util.ShiroUtils;
import javax.annotation.Resource;

@RestController
@RequestMapping("testTechnicistAuthorization")
public class TestTechnicistAuthorizationController extends ApiController {

    @Resource
    private TestTechnicistAuthorizationService authorizationService;
    @Resource
    private SysUserDao sysUserDao;

    @GetMapping("/list")
    public Result getAuthorizedList(@RequestParam(required = false) Integer technicistId) {
        if (ShiroUtils.getUserInfo() == null || sysUserDao.isManagerOrAbove(ShiroUtils.getUserInfo().getUserId()) == 0) {
            return ResultUtil.error(403, "权限不足");
        }
        return authorizationService.getAuthorizedList(technicistId);
    }

    @GetMapping("/excluded")
    public Result getExcluded(@RequestParam(required = false) Integer technicistId) {
        if (ShiroUtils.getUserInfo() == null || sysUserDao.isManagerOrAbove(ShiroUtils.getUserInfo().getUserId()) == 0) {
            return ResultUtil.error(403, "权限不足");
        }
        return authorizationService.getExcluded(technicistId);
    }

    @PostMapping("/save")
    public Result save(@RequestBody AuthorizationSaveReq req) {
        if (req == null || req.getTechnicistId() == null) {
            return ResultUtil.error(4001, "缺少 technicistId");
        }
        if (ShiroUtils.getUserInfo() == null || sysUserDao.isAdmin(ShiroUtils.getUserInfo().getUserId()) == 0) {
            return ResultUtil.error(403, "仅系统管理员可操作");
        }
        return authorizationService.save(req);
    }
}

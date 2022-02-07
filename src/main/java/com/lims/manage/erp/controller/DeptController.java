package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DeptService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dept")
public class DeptController {
    @Autowired
    private DeptService deptService;
    /**
     * 查询全部组织架构信息
     */
    @RequestMapping("/getAllDept")
    //@RequiresPermissions("sys:dept:list")
    public Result getAllDept(){
        return ResultUtil.success(deptService.getAllDept());
    }
}

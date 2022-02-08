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

    /**
     * 获取部门信息 无层级
     * @return
     */
    @RequestMapping("/getAllDepartment")
    //@RequiresPermissions("sys:dept:list")
    public Result getAllDepartment (){
        return ResultUtil.success(deptService.list());
    }

}

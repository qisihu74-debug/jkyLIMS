package com.lims.manage.erp.controller;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DeptService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("dept")
public class DeptController {
    @Autowired
    private DeptService deptService;
    @Autowired
    private LogManagerService logManagerService;
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

    /**
     * 查询部门列表
     * @return
     */
    @GetMapping("list")
    //@RequiresRoles("ADMIN")
    public Result list(Integer pageNum, Integer pageSize, String search){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数");
        }
        PageInfo pageInfo = deptService.findList(pageNum, pageSize, search);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 获取部门下的人员列表
     * @param id
     * @param isInclude 是否包含下级：1包含下级，0不包含
     * @return
     */
    @GetMapping("personList")
    ////@RequiresRoles("ADMIN")
    public Result personList(Long id, String isInclude, Integer pageNum, Integer pageSize,String search){
        if (StringUtils.isEmpty(isInclude)){
            return ResultUtil.error("请指明查询的部门人员是否包含下级！");
        }
        return ResultUtil.success(deptService.personList(id,isInclude,pageNum,pageSize,search));
    }

    /**
     * 编辑人员信息
     * @param personEntity
     * @return
     */
    @PostMapping("person_edit")
    //@RequiresRoles("ADMIN")
    public Result postEditPerson(@RequestBody DingUserEntity personEntity) {
        if (personEntity.getUserid() == null) {
            return ResultUtil.error("所属员工id为空");
        }
        if(personEntity.getMobile()==null){
            return ResultUtil.error("手机号为空");
        }
        Boolean flag = deptService.updatePersonDetails(personEntity);
        if (flag) {
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            if (userInfo != null) {
                logManagerService.addOpSysLog(userInfo, "管理员：" + userInfo.getUsername() + "修改用户：" + personEntity.getUserid() + "成功",
                        Const.PERSON_LOG, true);
            }
            return ResultUtil.success("用户信息修改成功！");
        } else {
            return ResultUtil.error("用户信息修改失败！");
        }
    }

}

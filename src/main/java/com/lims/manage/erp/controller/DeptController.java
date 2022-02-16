package com.lims.manage.erp.controller;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DeptService;
import com.lims.manage.erp.service.DingUserService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.PagingToolVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("dept")
public class DeptController {
    @Autowired
    private DeptService deptService;
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private DingUserService dingUserService;

    /**
     * 部门新增
     *
     * @param entity
     * @return
     */
    @PostMapping("add")
    //@RequiresRoles("ADMIN")
    public Result add(@RequestBody DingDeptEntity entity) {
        if (entity.getParentId() == null) {
            return ResultUtil.error("请选择上级部门");
        }
        if (StringUtils.isEmpty(entity.getName())) {
            return ResultUtil.error("部门名称为空");
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token已过期！");
        }
        // 验证部门编号
        if (entity.getCode() != null) {
            if (deptService.getDeptCode(entity.getCode())) {
                return ResultUtil.error("部门编号已存在，请重新输入！");
            }
        }
        DingDeptEntity deptEntity = deptService.getDeptByName(entity.getName());
        if (deptEntity != null) {
            return ResultUtil.error("部门已经存在！");
        }
        DingDeptEntity byPid = deptService.selectByPid(0L);
        if (entity.getParentId() == null && byPid == null) {
            entity.setParentId(0L);
        } else if (entity.getParentId() == null && byPid != null) {
            return ResultUtil.error("请选择上级部门！");
        }
        Boolean flag = deptService.add(entity);
        if (flag) {
            logManagerService.addOpSysLog(userInfo, "管理员：" + userInfo.getUsername() + "添加部门：" + entity.getName() + "成功",
                    Const.DEPT_LOG, true);
            return ResultUtil.success("部门新增成功！");
        } else {
            return ResultUtil.error("部门新增失败！");
        }
    }

    /**
     * 部门编辑
     *
     * @param entity
     * @return
     */
    @PostMapping("edit")
    //@RequiresRoles("ADMIN")
    public Result edit(@RequestBody DingDeptEntity entity) {
        if (StringUtils.isEmpty(entity.getName())) {
            return ResultUtil.error("部门名称为空");
        }
        if (entity.getId() == null) {
            return ResultUtil.error("缺少必要参数");
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token已过期！");
        }

        DingDeptEntity bean = deptService.getById(entity.getId());
        if (bean == null) {
            return ResultUtil.error("所选中部门不存在！");
        }
        // 顶级部门所属上级不能修改
        if(entity.getParentId()!=null){
            if(bean.getParentId().equals(0L)&&!entity.getParentId().equals(0L)){
                return ResultUtil.error("顶级部门所属上级不能修改！");
            }
        }
        // 验证部门编号
        if (entity.getCode() != null&&entity.getCode().length()>0) {
            // 验证是否变动
            if (deptService.getDeptExists(entity.getCode(), entity.getId())) {
                if (deptService.getDeptCode(entity.getCode())) {
                    return ResultUtil.error("部门编号已存在，请重新输入！");
                }
            }
        }
        DingDeptEntity deptEntity = deptService.getDeptByName(entity.getName());
        if ((!bean.getName().equals(entity.getName())) && deptEntity != null) {
            return ResultUtil.error("部门已经存在！");
        }
        Boolean flag = deptService.edit(entity);
        if (flag) {
            logManagerService.addOpSysLog(userInfo, "管理员：" + userInfo.getUsername() + "编辑部门:" + bean.getName() + "成功",
                    Const.DEPT_LOG, true);
            return ResultUtil.success("部门编辑成功！");
        } else {
            return ResultUtil.error("部门编辑失败！");
        }
    }

    /**
     * 部门删除
     *
     * @param id
     * @return
     */
    @GetMapping("delete")
    //@RequiresRoles("ADMIN")
    public Result delete(Long id) {
        if (id == null) {
            return ResultUtil.error("缺少必要的参数");
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token已过期！");
        }
        //判断部门是否有下级部门，或者部门下是否有人员信息，如果存在请先处理后进行删除
        PagingToolVo pageInfo = deptService.personList(id, Const.LOGIN_LOG_OUT, Const.PAGE_NUM, Const.PAGE_SIZE, null);
        if (!CollectionUtils.isEmpty(pageInfo.getList()) && pageInfo.getList().size() > 0) {
            return ResultUtil.error("部门下存在人员信息，请先完成对人员信息处理再进行删除操作！");
        }
        List<DingDeptEntity> deptEntities = deptService.sonList(id);
        if (!CollectionUtils.isEmpty(deptEntities) && deptEntities.size() > 1) {
            return ResultUtil.error("部门下存在子集部门，请先处理后再进行删除操作！");
        }
        Boolean flag = deptService.delete(id);
        if (flag) {
            logManagerService.addOpSysLog(userInfo, "管理员:" + userInfo.getUsername() + "删除部门：" + deptEntities.get(0).getName() + "成功",
                    Const.DEPT_LOG, true);
            return ResultUtil.success("部门删除成功");
        } else {
            return ResultUtil.error("部门删除失败");
        }
    }

    /**
     * 查询全部组织架构信息
     */
    @RequestMapping("/getAllDept")
    //@RequiresPermissions("sys:dept:list")
    public Result getAllDept() {
        return ResultUtil.success(deptService.getAllDept());
    }

    /**
     * 获取部门信息 无层级
     *
     * @return
     */
    @RequestMapping("/getAllDepartment")
    //@RequiresPermissions("sys:dept:list")
    public Result getAllDepartment() {
        return ResultUtil.success(deptService.list());
    }

    /**
     * 查询部门列表
     *
     * @return
     */
    @GetMapping("list")
    //@RequiresRoles("ADMIN")
    public Result list(Integer pageNum, Integer pageSize, String search) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数");
        }
        PageInfo pageInfo = deptService.findList(pageNum, pageSize, search);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 根据部门id查询部门子集列表
     *
     * @param id
     * @return
     */
    @GetMapping("sonList")
    //@RequiresRoles("ADMIN")
    public Result sonList(Long id) {
        List<DingDeptEntity> list = deptService.sonList(id);
        return ResultUtil.success(list);
    }

    /**
     * 根据部门id查询部门父级列表
     *
     * @param id
     * @return
     */
    @GetMapping("parentList")
    //@RequiresRoles("ADMIN")
    public Result parentList(Long id) {
        if (id == null) {
            return ResultUtil.error("缺少必要参数");
        }
        List<DingDeptEntity> list = deptService.parentList(id);
        return ResultUtil.success(list);
    }

    /**
     * 获取部门下的人员列表
     *
     * @param id
     * @param isInclude 是否包含下级：1包含下级，0不包含
     * @return
     */
    @GetMapping("personList")
    ////@RequiresRoles("ADMIN")
    public Result personList(Long id, String isInclude, Integer pageNum, Integer pageSize, String search) {
        if (StringUtils.isEmpty(isInclude)) {
            return ResultUtil.error("请指明查询的部门人员是否包含下级！");
        }
        return ResultUtil.success(deptService.personList(id, isInclude, pageNum, pageSize, search));
    }


    /**
     * 编辑人员信息
     *
     * @param personEntity
     * @return
     */
    @PostMapping("person_edit")
    //@RequiresRoles("ADMIN")
    public Result postEditPerson(@RequestBody DingUserEntity personEntity) {
        if (personEntity.getUserid() == null) {
            return ResultUtil.error("所属员工id为空");
        }
        if (personEntity.getMobile() == null) {
            return ResultUtil.error("手机号为空");
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token已过期！");
        }
        if (personEntity.getJobnumber() != null) {
            // 修改时 查询工号是否变动
            if (deptService.getSelectOneEdit(personEntity)) {
                // 变动时
                // 验证工号是否重复
                if (!deptService.getSelectOne(personEntity)) {
                    return ResultUtil.error("工号已存在");
                }
            }
        }
        // 获取用户信息
        DingUserEntity tableEntity = dingUserService.getById(personEntity.getUserid());
        if (tableEntity == null) {
            return ResultUtil.error("修改失败，此人员信息为空");
        }
        Boolean flag = deptService.updatePersonDetails(personEntity);
        if (flag) {
            logManagerService.addOpSysLog(userInfo, "管理员：" + userInfo.getUsername() + "修改用户：" + personEntity.getUserid() + "成功",
                    Const.PERSON_LOG, true);
            return ResultUtil.success("用户信息修改成功！");
        } else {
            return ResultUtil.error("用户信息修改失败！");
        }
    }

    @PostMapping("person_add")
    //@RequiresRoles("ADMIN")
    public Result postAddPerson(@RequestBody DingUserEntity personEntity) {
        if (StringUtils.isEmpty(personEntity.getName())) {
            return ResultUtil.error("人员名称不能为空");
        }
        if (personEntity.getMobile() == null) {
            return ResultUtil.error("手机号不能为空");
        }
        if (personEntity.getDepartment() == null) {
            return ResultUtil.error("所属组织不能为空");
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token已经过期！");
        }
        if (personEntity.getJobnumber() != null) {
            // 验证工号是否重复
            if (!deptService.getSelectOne(personEntity)) {
                return ResultUtil.error("工号已存在");
            }
        }
        // 新增时 判断 部门id 是否存在
//        DeptEntity Department = deptService.getDeptById(personEntity.getDeptId());
//        if (Department == null) {
//            return ResultUtil.error("所属部门id不存在");
//        }
        Boolean flag = deptService.addPersonDetails(personEntity);
        if (flag) {
            logManagerService.addOpSysLog(userInfo, "管理员：" + userInfo.getUsername() + "添加用户：" + personEntity.getName() + "成功",
                    Const.PERSON_LOG, true);
            return ResultUtil.success("用户信息新增成功！");
        }
        return ResultUtil.error("用户信息新增失败！");
    }

    @RequestMapping("person_delete")
    //@RequiresRoles("ADMIN")
    public Result postDeletePerson(@RequestBody DingUserEntity personEntity) {
        if (personEntity.getIds().isEmpty()) {
            return ResultUtil.error("待删除员工id不能为空");
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token已经过期！");
        }
        Boolean flag = deptService.deletePersonDetail(personEntity.getIds());
        if (flag) {
            logManagerService.addOpSysLog(userInfo, "管理员：" + userInfo.getUsername() + "删除用户：" + personEntity.getIds() + "成功",
                    Const.PERSON_LOG, true);
            return ResultUtil.success("用户信息删除成功！");
        } else {
            return ResultUtil.error("用户信息删除失败！");
        }
    }

}

package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.mapper.TestProductCommitteeDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 产品委员会
 */
@Service("testProductCommitteeService")
public class TestProductCommitteeServiceImpl extends ServiceImpl<TestProductCommitteeDao, TestProductCommitteeEntity> implements TestProductCommitteeService {

    /*日志*/
    @Resource
    private LogManagerService logManagerService;

    @Resource
    private SysUserService userService;
    @Resource
    private DeptDao deptDao;
    @Resource
    private TestTechnicistService testTechnicistService;
    @Resource
    private TestTeamService testTeamService;

    @Resource
    private TestProductCommitteeService productCommitteeService;


    @Override
    public Result addProductCommittee(TestProductCommitteeEntity productCommittee) {
        TestProductCommitteeEntity committeeEntity = new TestProductCommitteeEntity();
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }

        SysUserEntity sysUserEntity = userService.getById(productCommittee.getUserId());

        committeeEntity.setUserId(productCommittee.getUserId());
        String identity = StringUtils.isNotBlank(productCommittee.getCouncilIdentity())
                ? productCommittee.getCouncilIdentity() : "委员";
        committeeEntity.setCouncilIdentity(identity);
        committeeEntity.setCouncilName(sysUserEntity.getName());

        LabelValueVo deptName = null;
        if (StringUtils.isNotBlank(sysUserEntity.getDepartment())) {
            String dept = sysUserEntity.getDepartment().replaceAll("\\[", "")
                    .replaceAll("\\]", "");
            long deptId = Integer.parseInt(dept.split(",")[0]);
            deptName = deptDao.getRoleInfoById(deptId);
            if (deptName != null) {
                committeeEntity.setDepartment(deptName.getLabel());
            }
        }

        if (null != sysUserEntity.getUserId()) {
            QueryWrapper<TestTechnicist> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("del_flag", 0);
            queryWrapper.eq("user_id", sysUserEntity.getUserId());
            TestTechnicist technicist = testTechnicistService.getOne(queryWrapper);
            if (null != technicist && null != technicist.getTeamId()) {
                TestTeam testTeam = testTeamService.getById(technicist.getTeamId());
                if (null != testTeam && StringUtils.isNotBlank(testTeam.getName())) {
                    committeeEntity.setPosition(testTeam.getName());
                }
            }
        }


        committeeEntity.setDelFlag(0);
        committeeEntity.setCreateTime(new Date());
        committeeEntity.setUpdateTime(new Date());

        if (productCommitteeService.save(committeeEntity)) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    "用户：" + userInfo.getUsername() + "添加产品委员会" + committeeEntity.getCouncilId() + "成功!",
                    Const.TEAM_MANAGEMENT_LOG, true);
            return ResultUtil.success("添加成功!");
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    "用户：" + userInfo.getUsername() + "添加产品委员会" + committeeEntity.getCouncilId() + "失败!",
                    Const.TEAM_MANAGEMENT_LOG, false);
            return ResultUtil.error("添加失败!");
        }

    }

    @Override
    public Result delProductCommittee(String councilId) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }

        TestProductCommitteeEntity committeeEntity = getById(councilId);
        committeeEntity.setDelFlag(1);

        if (this.updateById(committeeEntity)) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    "用户：" + userInfo.getUsername() + "删除产品委员会" + committeeEntity.getCouncilId() + "成功!",
                    Const.TEAM_MANAGEMENT_LOG, true);
            return ResultUtil.success("删除成功");

        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    "用户：" + userInfo.getUsername() + "删除产品委员会" + committeeEntity.getCouncilId() + "失败!",
                    Const.TEAM_MANAGEMENT_LOG, false);
            return ResultUtil.error("删除失败!");

        }

    }

}

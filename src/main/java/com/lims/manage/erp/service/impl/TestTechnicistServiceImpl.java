package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.entity.TestTechnicist;
import com.lims.manage.erp.mapper.TestTechnicistDao;
import com.lims.manage.erp.entity.TestTechnicist;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestTechnicistService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TestTechnicistVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 技术人员(TestTechnicistVo)表服务实现类
 *
 * @author makejava
 * @since 2022-02-23 09:14:45
 */
@Service("testTechnicistService")
public class TestTechnicistServiceImpl extends ServiceImpl<TestTechnicistDao, TestTechnicist> implements TestTechnicistService {
    @Resource
    private TestTechnicistDao testTechnicistDao;
    @Resource
    private LogManagerService logManagerService;
    @Resource
    private SysOssService sysOssService;

    @Override
    public Result addTestTechnicist(TestTechnicist TestTechnicist) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (TestTechnicist.getUserId()==null){
            return ResultUtil.error("用户编号不能为空");
        }
        if (TestTechnicist.getTeamId()==null){
            return ResultUtil.error("团队编号不能为空");
        }
        if (this.getOne(new QueryWrapper<TestTechnicist>().eq("user_id",TestTechnicist.getUserId()).eq("del_flag",0))!=null){
            return ResultUtil.error("该技术人员已存在");
        }
        TestTechnicist.setStatus("0");
        TestTechnicist.setDelFlag(0);
        TestTechnicist.setCreateTime(new Date());
        if (this.save(TestTechnicist)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加技术人员"+TestTechnicist.getId()+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加技术人员失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updTestTechnicist(TestTechnicist TestTechnicist) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (TestTechnicist.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (TestTechnicist.getUserId()==null){
            return ResultUtil.error("用户编号不能为空");
        }
        if (TestTechnicist.getTeamId()==null){
            return ResultUtil.error("团队编号不能为空");
        }
        if (this.getOne(new QueryWrapper<TestTechnicist>().eq("user_id",TestTechnicist.getUserId()).eq("del_flag",0).ne("id",TestTechnicist.getId()))!=null){
            return ResultUtil.error("该技术人员已存在");
        }
        TestTechnicist.setUpdateTime(new Date());
        if (this.updateById(TestTechnicist)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改技术人员"+TestTechnicist.getId()+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改技术人员"+TestTechnicist.getId()+"失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delTestTechnicist(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestTechnicist> testLaboratoryList=new ArrayList<>();
        for (Long aLong : idList) {
            TestTechnicist testLaboratory=new TestTechnicist();
            testLaboratory.setUpdateTime(new Date());
            testLaboratory.setDelFlag(1);
            testLaboratory.setId(aLong.intValue());
            sysOssService.delAnnounce(this.getById(aLong).getCertificateUrl());
            testLaboratoryList.add(testLaboratory);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testLaboratoryList)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除技术人员"+idStr+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除技术人员"+idStr+"失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public IPage<TestTechnicistVo> getListPage(IPage<TestTechnicistVo> page, Wrapper<TestTechnicist> queryWrapper) {
        return testTechnicistDao.getListPage(page,queryWrapper);
    }

    @Override
    public List<SysUserEntity> getUserList() {
        return testTechnicistDao.getUserList();
    }
}


package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.mapper.TestTeamDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestTeamService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.TreeBuilder;
import com.lims.manage.erp.vo.Node;
import com.lims.manage.erp.vo.TestTeamVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 团队管理(TestTeamVo)表服务实现类
 *
 * @author makejava
 * @since 2022-02-23 09:14:46
 */
@Service("testTeamService")
public class TestTeamServiceImpl extends ServiceImpl<TestTeamDao, TestTeam> implements TestTeamService {
    @Resource
    private TestTeamDao testTeamDao;
    @Resource
    private LogManagerService logManagerService;
    @Override
    public Result addTestTeam(TestTeam testTeam) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testTeam.getName()==null){
            return ResultUtil.error("科室名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestTeam>().eq("name",testTeam.getName()).eq("del_flag",0))!=null){
            return ResultUtil.error("科室名称重复");
        }
        testTeam.setStatus("0");
        testTeam.setDelFlag(0);
        testTeam.setCreateTime(new Date());
        if (this.save(testTeam)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加科室"+testTeam.getId()+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加科室失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updTestTeam(TestTeam testTeam) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testTeam.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testTeam.getName()==null){
            return ResultUtil.error("科室名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestTeam>().eq("name",testTeam.getName()).eq("del_flag",0).ne("id",testTeam.getId()))!=null){
            return ResultUtil.error("科室名称重复");
        }
        testTeam.setUpdateTime(new Date());
        if (this.updateById(testTeam)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改科室"+testTeam.getId()+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改科室"+testTeam.getId()+"失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delTestTeam(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestTeam> testLaboratoryList=new ArrayList<>();
        for (Long aLong : idList) {
            TestTeam testLaboratory=new TestTeam();
            testLaboratory.setUpdateTime(new Date());
            testLaboratory.setDelFlag(1);
            testLaboratory.setId(aLong.intValue());
            testLaboratoryList.add(testLaboratory);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testLaboratoryList)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除科室"+idStr+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除科室"+idStr+"失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public IPage<TestTeamVo> getListPage(IPage<TestTeamVo> page, Wrapper<TestTeam> queryWrapper) {
        return testTeamDao.getListPage(page,queryWrapper);
    }

    @Override
    public List<Node> getTree() {
        //获取所有团队
        List<Node> teamList=baseMapper.getTree();
        return TreeBuilder.buildTree(teamList);
    }

    @Override
    public List<TestTeam> getTreeByName(String name) {
        LambdaQueryWrapper<TestTeam> wrapper=Wrappers.lambdaQuery();
        wrapper.eq(TestTeam::getDelFlag,0);
        wrapper.like(TestTeam::getName,name);
        wrapper.select(TestTeam::getId,TestTeam::getName);
        return baseMapper.selectList(wrapper);
    }
}


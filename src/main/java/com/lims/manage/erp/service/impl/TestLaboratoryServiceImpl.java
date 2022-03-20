package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.mapper.TestLaboratoryDao;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestLaboratoryService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TestLaboratoryVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 实验室管理(TestLaboratory)表服务实现类
 *
 * @author makejava
 * @since 2022-02-25 10:08:36
 */
@Service("testLaboratoryService")
public class TestLaboratoryServiceImpl extends ServiceImpl<TestLaboratoryDao, TestLaboratory> implements TestLaboratoryService {
    @Resource
    private TestLaboratoryDao testLaboratoryDao;
    @Resource
    private LogManagerService logManagerService;
    @Resource
    private SysOssService sysOssService;
    @Override
    public Result addLaboratory(TestLaboratory testLaboratory) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testLaboratory.getName()==null){
            return ResultUtil.error("实验室名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestLaboratory>().eq("name",testLaboratory.getName()).eq("del_flag",0))!=null){
            return ResultUtil.error("实验室名称重复");
        }
        testLaboratory.setStatus("0");
        testLaboratory.setDelFlag(0);
        testLaboratory.setCreateTime(new Date());
        testLaboratory.setUpdateTime(null);
        if (this.save(testLaboratory)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加实验室"+testLaboratory.getId()+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加实验室失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updLaboratory(TestLaboratory testLaboratory) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testLaboratory.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testLaboratory.getName()==null){
            return ResultUtil.error("实验室名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestLaboratory>().eq("name",testLaboratory.getName()).eq("del_flag",0).ne("id",testLaboratory.getId()))!=null){
            return ResultUtil.error("实验室名称重复");
        }
        testLaboratory.setUpdateTime(new Date());
        if (this.updateById(testLaboratory)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改实验室"+testLaboratory.getId()+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改实验室"+testLaboratory.getId()+"失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delLaboratory(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestLaboratory> testLaboratoryList=new ArrayList<>();
        for (Long aLong : idList) {
            TestLaboratory testLaboratory=new TestLaboratory();
            testLaboratory.setUpdateTime(new Date());
            testLaboratory.setDelFlag(1);
            testLaboratory.setId(aLong.intValue());
            sysOssService.delAnnounce(this.getById(aLong).getPicture());
            testLaboratoryList.add(testLaboratory);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testLaboratoryList)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除实验室"+idStr+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除实验室"+idStr+"失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public IPage<TestLaboratoryVo> getPageList(Page<TestLaboratoryVo> page, QueryWrapper<TestLaboratory> queryWrapper) {
        return testLaboratoryDao.getListPage(page,queryWrapper);
    }
}


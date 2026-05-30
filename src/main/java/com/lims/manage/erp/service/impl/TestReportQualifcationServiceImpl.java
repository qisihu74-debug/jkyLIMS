package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.mapper.TestReportQualifcationDao;
import com.lims.manage.erp.entity.TestReportQualifcation;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestReportQualifcationService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TestReportQualifcationVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * (TestReportQualifcation)表服务实现类
 *
 * @author makejava
 * @since 2022-03-14 14:33:33
 */
@Service("testReportQualifcationService")
public class TestReportQualifcationServiceImpl extends ServiceImpl<TestReportQualifcationDao, TestReportQualifcation> implements TestReportQualifcationService {
    @Resource
    private TestReportQualifcationDao testReportQualifcationDao;
    @Resource
    private LogManagerService logManagerService;

    @Override
    public Result addtestReportQualifcation(TestReportQualifcation testReportQualifcation) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        testReportQualifcation.setStatus("0");
        if (this.save(testReportQualifcation)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加技术指标"+testReportQualifcation.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加技术指标失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }

    }

    @Override
    public Result updtestReportQualifcation(TestReportQualifcation testReportQualifcation) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testReportQualifcation.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        testReportQualifcation.setUpdateTime(new Date());
        if (this.updateById(testReportQualifcation)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改技术指标"+testReportQualifcation.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改技术指标"+testReportQualifcation.getId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result deltestReportQualifcation(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestReportQualifcation> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestReportQualifcation testStandardFile=new TestReportQualifcation();
            testStandardFile.setId(aLong.intValue());
            testStandardFile.setDelFlag(1);
            testMethods.add(testStandardFile);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testMethods)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除技术指标"+idStr+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除技术指标"+idStr+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }

    }

    @Override
    public IPage<TestReportQualifcationVo> getPageList(Page<TestReportQualifcationVo> page, QueryWrapper<TestReportQualifcation> queryWrapper) {
        return testReportQualifcationDao.getListPage(page,queryWrapper);
    }
}


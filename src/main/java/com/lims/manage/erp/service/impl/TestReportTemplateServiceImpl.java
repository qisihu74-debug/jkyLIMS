package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.TestReportTemplateDao;
import com.lims.manage.erp.entity.TestReportTemplate;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestReportTemplateService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * (TestReportTemplate)表服务实现类
 *
 * @author makejava
 * @since 2022-03-02 16:22:10
 */
@Service("testReportTemplateService")
public class TestReportTemplateServiceImpl extends ServiceImpl<TestReportTemplateDao, TestReportTemplate> implements TestReportTemplateService {
    @Resource
    private LogManagerService logManagerService;
    @Resource
    private SysOssService sysOssService;

    @Override
    public Result addReportTemplate(TestReportTemplate testReportTemplate) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        testReportTemplate.setStatus("0");
        testReportTemplate.setDelFlag(0);
        testReportTemplate.setCreateTime(new Date());
        if (this.save(testReportTemplate)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加报告模板"+testReportTemplate.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加报告模板失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updReportTemplate(TestReportTemplate testReportTemplate) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testReportTemplate.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        testReportTemplate.setUpdateTime(new Date());
        if (this.updateById(testReportTemplate)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改报告模板"+testReportTemplate.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改报告模板"+testReportTemplate.getId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delReportTemplate(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestReportTemplate> testReportTemplates=new ArrayList<>();
        for (Long aLong : idList) {
            TestReportTemplate testReportTemplate=new TestReportTemplate();
            testReportTemplate.setUpdateTime(new Date());
            testReportTemplate.setDelFlag(1);
            testReportTemplate.setId(aLong.intValue());
            sysOssService.delAnnounce(this.getById(aLong).getReportFileUri());
            testReportTemplates.add(testReportTemplate);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testReportTemplates)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除报告模板"+idStr+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除报告模板"+idStr+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }
}


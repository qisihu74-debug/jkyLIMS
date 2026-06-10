package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestReportTemplate;
import com.lims.manage.erp.mapper.TestOriginalRecordTemplateDao;
import com.lims.manage.erp.entity.TestOriginalRecordTemplate;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestOriginalRecordTemplateService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TorttpiVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 原始记录模板(TestOriginalRecordTemplate)表服务实现类
 *
 * @author makejava
 * @since 2022-03-16 14:12:44
 */
@Service("testOriginalRecordTemplateService")
public class TestOriginalRecordTemplateServiceImpl extends ServiceImpl<TestOriginalRecordTemplateDao, TestOriginalRecordTemplate> implements TestOriginalRecordTemplateService {
    @Resource
    private LogManagerService logManagerService;
    @Resource
    private TestOriginalRecordTemplateDao testOriginalRecordTemplateDao;

    @Resource
    private SysOssService sysOssService;
    @Override
    public Result addtestOriginalRecordTemplate(TestOriginalRecordTemplate testOriginalRecordTemplate) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testOriginalRecordTemplate.getName()==null){
            return ResultUtil.error("原始模板名称不能为空");
        }
        /*if (this.getOne(new QueryWrapper<TestOriginalRecordTemplate>().eq("name",testOriginalRecordTemplate.getName()))!=null){
            return ResultUtil.error("原始模板名称重复");
        }*/
        Integer maxId = testOriginalRecordTemplateDao.getMaxId();
        testOriginalRecordTemplate.setId(maxId);
        testOriginalRecordTemplate.setPid(maxId);
        testOriginalRecordTemplate.setStatus("0");
        testOriginalRecordTemplate.setDelFlag(0);
        testOriginalRecordTemplate.setCreateTime(new Date());
        if (this.save(testOriginalRecordTemplate)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加原始记录模板"+testOriginalRecordTemplate.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加原始记录模板"+testOriginalRecordTemplate.getId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }

    }

    @Override
    public Result updtestOriginalRecordTemplate(TestOriginalRecordTemplate testOriginalRecordTemplate) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testOriginalRecordTemplate.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testOriginalRecordTemplate.getName()==null){
            return ResultUtil.error("模板名称不能为空");
        }
        /*if (this.getOne(new QueryWrapper<TestOriginalRecordTemplate>().eq("name",testOriginalRecordTemplate.getName()).eq("del_flag",0).ne("id",testOriginalRecordTemplate.getId()))!=null){
            return ResultUtil.error("名称重复");
        }*/
        testOriginalRecordTemplate.setUpdateTime(new Date());
        if (this.updateById(testOriginalRecordTemplate)){
            if (testOriginalRecordTemplate.getCopyUrl() != null
                    && !testOriginalRecordTemplate.getCopyUrl().equals(testOriginalRecordTemplate.getFileUrl())){
                sysOssService.delAnnounce(testOriginalRecordTemplate.getCopyUrl());
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改原始记录模板"+testOriginalRecordTemplate.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改原始记录模板"+testOriginalRecordTemplate.getId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delTtestOriginalRecordTemplate(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestOriginalRecordTemplate> testOriginalRecordTemplate=new ArrayList<>();
        for (Long aLong : idList) {
            TestOriginalRecordTemplate testProductType=new TestOriginalRecordTemplate();
            testProductType.setUpdateTime(new Date());
            testProductType.setDelFlag(1);
            testProductType.setId(aLong.intValue());
            TestOriginalRecordTemplate current = this.getById(aLong);
            String url=current == null ? null : current.getFileUrl();
            if (url!=null){
                sysOssService.delAnnounce(url);
            }
            testOriginalRecordTemplate.add(testProductType);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testOriginalRecordTemplate)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除原始记录模板"+idStr+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除原始记录模板"+idStr+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }

    }

    @Override
    public IPage<TorttpiVo> getPageList(Page<TorttpiVo> page, QueryWrapper<TestOriginalRecordTemplate> queryWrapper) {
        return testOriginalRecordTemplateDao.getPageList(page, queryWrapper);
    }

    @Override
    public Result getAllList() {
        LambdaQueryWrapper<TestOriginalRecordTemplate> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TestOriginalRecordTemplate::getDelFlag, 0);
        List<TestOriginalRecordTemplate> list = testOriginalRecordTemplateDao.selectList(lambdaQueryWrapper);
        for (TestOriginalRecordTemplate data : list) {
            data.setFileUrl(null);
        }
        return ResultUtil.success(list);
    }

    @Override
    public Result changeTestOriginalRecordTemplate(TestOriginalRecordTemplate testOriginalRecordTemplate) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testOriginalRecordTemplate.getName()==null){
            return ResultUtil.error("原始模板名称不能为空");
        }
        if (testOriginalRecordTemplate.getId()==null){
            return ResultUtil.error("变更对象ID为空");
        }
        Integer oldId = testOriginalRecordTemplate.getId();
        TestOriginalRecordTemplate detail = testOriginalRecordTemplateDao.getDetail(oldId);
        if (detail == null) {
            return ResultUtil.error("原始记录模板不存在");
        }
        Integer pid = testOriginalRecordTemplate.getPid() != null
                ? testOriginalRecordTemplate.getPid()
                : (detail.getPid() != null ? detail.getPid() : oldId);
        detail.setUpdateTime(new Date());
        testOriginalRecordTemplateDao.insertRecord(detail);
        testOriginalRecordTemplateDao.deleteById(oldId);
        //保存新原始记录
        testOriginalRecordTemplate.setStatus("0");
        testOriginalRecordTemplate.setDelFlag(0);
        testOriginalRecordTemplate.setPid(pid);
        testOriginalRecordTemplate.setCreateTime(new Date());
        if (this.save(testOriginalRecordTemplate)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加原始记录模板"+testOriginalRecordTemplate.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加原始记录模板"+testOriginalRecordTemplate.getId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }

    }

    @Override
    public PageInfo getRecords(Integer pid, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<TestOriginalRecordTemplate> recordList = testOriginalRecordTemplateDao.getRecordList(pid);
        return new PageInfo<>(recordList);
    }

}


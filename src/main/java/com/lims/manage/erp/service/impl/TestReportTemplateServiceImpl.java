package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.entity.TestReportTemplateProductRef;
import com.lims.manage.erp.mapper.TestReportTemplateDao;
import com.lims.manage.erp.entity.TestReportTemplate;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestReportTemplateProductRefService;
import com.lims.manage.erp.service.TestReportTemplateService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TestReportTemplateVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * (TestReportTemplate)表服务实现类
 *
 * @author makejava
 * @since 2022-03-02 16:22:10
 */
@Slf4j
@Service("testReportTemplateService")
public class TestReportTemplateServiceImpl extends ServiceImpl<TestReportTemplateDao, TestReportTemplate> implements TestReportTemplateService {
    @Resource
    private LogManagerService logManagerService;
    @Resource
    private SysOssService sysOssService;
    @Resource
    private TestReportTemplateProductRefService testReportTemplateProductRefService;
    @Resource
    private TestReportTemplateDao templateDao;

    public  List<TestReportTemplateProductRef> getTestReportTemplateProductRef(Integer id,List<Integer> ids){
        List<TestReportTemplateProductRef> testReportTemplateProductRefs=new ArrayList<>();
        if (ids!=null &&ids.size()!=0){
            for (Integer productId : ids) {
                TestReportTemplateProductRef testReportTemplateProductRef=new TestReportTemplateProductRef();
                testReportTemplateProductRef.setProductId(productId);
                testReportTemplateProductRef.setTemplateId(id);
                testReportTemplateProductRefs.add(testReportTemplateProductRef);
            }
        }
        return testReportTemplateProductRefs;
    }
    @Override
    public Result addReportTemplate(TestReportTemplateVo testReportTemplate) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (this.getOne(new QueryWrapper<TestReportTemplate>().eq("report_code",testReportTemplate.getTestReportTemplate().getReportCode()).eq("del_flag",0))!=null){
            return ResultUtil.error("检测模板编号重复");
        }
        testReportTemplate.getTestReportTemplate().setStatus("0");
        testReportTemplate.getTestReportTemplate().setDelFlag(0);
        testReportTemplate.getTestReportTemplate().setCreateTime(new Date());
        if (this.save(testReportTemplate.getTestReportTemplate())){
            testReportTemplateProductRefService.saveBatch(this.getTestReportTemplateProductRef(testReportTemplate.getTestReportTemplate().getId(),testReportTemplate.getProductIds()));
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加报告模板"+testReportTemplate.getTestReportTemplate().getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加报告模板失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updReportTemplate(TestReportTemplateVo testReportTemplate) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testReportTemplate.getTestReportTemplate().getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (this.getOne(new QueryWrapper<TestReportTemplate>().ne("id",testReportTemplate.getTestReportTemplate().getId()).eq("report_code",testReportTemplate.getTestReportTemplate().getReportCode()).eq("del_flag",0))!=null){
            return ResultUtil.error("检测模板编号重复");
        }
        testReportTemplate.getTestReportTemplate().setUpdateTime(new Date());
        if (this.updateById(testReportTemplate.getTestReportTemplate())){
            //编辑报告模板删除文件
            sysOssService.delAnnounce(testReportTemplate.getTestReportTemplate().getReportFileUri());
            testReportTemplateProductRefService.remove(new QueryWrapper<TestReportTemplateProductRef>().eq("template_id",testReportTemplate.getTestReportTemplate().getId()));
            testReportTemplateProductRefService.saveBatch(this.getTestReportTemplateProductRef(testReportTemplate.getTestReportTemplate().getId(),testReportTemplate.getProductIds()));
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改报告模板"+testReportTemplate.getTestReportTemplate().getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改报告模板"+testReportTemplate.getTestReportTemplate().getId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
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
            String url=this.getById(aLong).getReportFileUri();
            if (url!=null){
                sysOssService.delAnnounce(url);
                log.info("管理员:"+ShiroUtils.getUserInfo().getUsername()+"删除文件："+url);
            }
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

    @Override
    public Result getList(Serializable id) {
        QueryWrapper<TestReportTemplate> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("del_flag",0);

        queryWrapper.in("id",this.getTemplateIdList(id));
        List<TestReportTemplate> testReportTemplates=this.list(queryWrapper);
        return ResultUtil.success(testReportTemplates);
    }

    @Override
    public Result getUpdOne(Serializable id) {
        TestReportTemplate testMethod=this.getOne(new QueryWrapper<TestReportTemplate>().eq("id",id).eq("del_flag",0));
        TestReportTemplateVo testReportTemplateVo=new TestReportTemplateVo();
        testReportTemplateVo.setTestReportTemplate(testMethod);
        testReportTemplateVo.setProductIds(this.getProductIdList(id));
        return ResultUtil.success(testReportTemplateVo);
    }

    @Override
    public String getNameById(Integer reportModelId) {
        return templateDao.getNameById(reportModelId);
    }

    public List<Integer> getTemplateIdList(Serializable id){
        List<TestReportTemplateProductRef> testReportTemplateProductRefs=testReportTemplateProductRefService.list(new QueryWrapper<TestReportTemplateProductRef>().eq("product_id",id));
        List<Integer> integerList=new ArrayList<>();
        if (testReportTemplateProductRefs!=null){
            for (TestReportTemplateProductRef testReportTemplateProductRef : testReportTemplateProductRefs) {
                integerList.add(testReportTemplateProductRef.getTemplateId());
            }
        }
        return integerList;
    }

    public List<Integer> getProductIdList(Serializable id){
        List<TestReportTemplateProductRef> testReportTemplateProductRefs=testReportTemplateProductRefService.list(new QueryWrapper<TestReportTemplateProductRef>().eq("template_id",id));
        List<Integer> integerList=new ArrayList<>();
        if (testReportTemplateProductRefs!=null){
            for (TestReportTemplateProductRef testReportTemplateProductRef : testReportTemplateProductRefs) {
                integerList.add(testReportTemplateProductRef.getProductId());
            }
        }
        return integerList;
    }
}


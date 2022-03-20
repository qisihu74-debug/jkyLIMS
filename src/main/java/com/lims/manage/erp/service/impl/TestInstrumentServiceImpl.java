package com.lims.manage.erp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itextpdf.text.io.StreamUtil;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.mapper.TestInstrumentDao;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestInstrumentService;
import com.lims.manage.erp.service.TestLaboratoryService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TestInstrumentVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 仪器设备(TestInstrument)表服务实现类
 *
 * @author makejava
 * @since 2022-02-25 10:05:51
 */
@Service("testInstrumentService")
public class TestInstrumentServiceImpl extends ServiceImpl<TestInstrumentDao, TestInstrument> implements TestInstrumentService {
    @Resource
    private TestLaboratoryService testLaboratoryService;
    @Resource
    private TestInstrumentDao testInstrumentDao;
    @Resource
    private LogManagerService logManagerService;

    @Resource
    private SysOssService sysOssService;
    @Override
    public Result addInstrument(TestInstrument testInstrument) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testInstrument.getLaboratoryId()!=null){
            TestLaboratory testLaboratory=testLaboratoryService.getById(testInstrument.getLaboratoryId());
            if (StrUtil.isEmptyIfStr(testLaboratory)){
                return ResultUtil.error("没有这个实验室！");
            }
        }else {
            return ResultUtil.error("所在实验室参数为空！");
        }
        QueryWrapper<TestInstrument> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("laboratory_id",testInstrument.getLaboratoryId());
        queryWrapper.eq("name",testInstrument.getName());
        if (this.list(queryWrapper).size()>0){
            return ResultUtil.error("该实验室已有该设备！");
        }
        testInstrument.setStatus("0");
        testInstrument.setDelFlag(0);
        testInstrument.setCreateTime(new Date());
        testInstrument.setUpdateTime(null);
        if (this.save(testInstrument)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加设备仪器"+testInstrument.getId()+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG,true);
            return ResultUtil.success("设备添加成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加设备仪器失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败");
        }
    }

    @Override
    public Result updInstrument(TestInstrument testInstrument) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testInstrument.getId()==null){
            return ResultUtil.error("修改对象ID为空！");
        }
        if (testInstrument.getLaboratoryId()!=null){
            TestLaboratory testLaboratory=testLaboratoryService.getById(testInstrument.getLaboratoryId());
            if (StrUtil.isEmptyIfStr(testLaboratory)){
                return ResultUtil.error("没有这个实验室！");
            }
        }
        QueryWrapper<TestInstrument> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("laboratory_id",testInstrument.getLaboratoryId());
        queryWrapper.eq("name",testInstrument.getName());
        queryWrapper.ne("id",testInstrument.getId());
        queryWrapper.eq("del_flag",0);
        if (this.list(queryWrapper).size()>0){
            return ResultUtil.error("该实验室已有该设备！");
        }
        testInstrument.setUpdateTime(new Date());
        if (this.updateById(testInstrument)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改设备仪器"+testInstrument.getId()+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改设备仪器"+testInstrument.getId()+"失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败");
        }
    }

    @Override
    public Result delInstruments(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestInstrument> testInstrumentList=new ArrayList<>();
        for (Long aLong : idList) {
            TestInstrument testInstrument=new TestInstrument();
            testInstrument.setUpdateTime(new Date());
            testInstrument.setDelFlag(1);
            testInstrument.setId(aLong.intValue());
            sysOssService.delAnnounce(this.getById(aLong).getPicture());
            sysOssService.delAnnounce(this.getById(aLong).getContractUrl());
            sysOssService.delAnnounce(this.getById(aLong).getInvoiceUrl());

            testInstrumentList.add(testInstrument);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testInstrumentList)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除设备仪器"+idStr+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除设备仪器"+idStr+"失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public IPage<TestInstrumentVo> getPageList(Page<TestInstrumentVo> page, QueryWrapper<TestInstrument> queryWrapper) {
        return testInstrumentDao.getPageList(page,queryWrapper);
    }
}


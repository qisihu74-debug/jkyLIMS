package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.entity.TestInstrumentType;
import com.lims.manage.erp.mapper.TestInstrumentAppraisalRecordDao;
import com.lims.manage.erp.entity.TestInstrumentAppraisalRecord;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestInstrumentAppraisalRecordService;
import com.lims.manage.erp.service.TestInstrumentService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 设备仪器检定记录表(TestInstrumentAppraisalRecord)表服务实现类
 *
 * @author makejava
 * @since 2022-03-01 11:44:18
 */
@Service("testInstrumentAppraisalRecordService")
public class TestInstrumentAppraisalRecordServiceImpl extends ServiceImpl<TestInstrumentAppraisalRecordDao, TestInstrumentAppraisalRecord> implements TestInstrumentAppraisalRecordService {
    @Resource
    private TestInstrumentService testInstrumentService;
    @Resource
    private LogManagerService logManagerService;
    @Resource
    private SysOssService sysOssService;
    @Override
    public Result addInstrumentAppraisalRecord(TestInstrumentAppraisalRecord testInstrumentAppraisalRecord) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testInstrumentAppraisalRecord.getCode()!=null){
            TestInstrument testInstrument=testInstrumentService.getOne(new QueryWrapper<TestInstrument>().eq("id",testInstrumentAppraisalRecord.getCode()).eq("del_flag",0));
            if (testInstrument!=null){
                if (testInstrumentAppraisalRecord.getFileUrl()!=null){
                    testInstrumentAppraisalRecord.setName(testInstrument.getName());
                    testInstrumentAppraisalRecord.setCreateTime(new Date());
                    testInstrumentAppraisalRecord.setDelFlag(0);
                    testInstrumentAppraisalRecord.setStatus("0");
                    if (this.save(testInstrumentAppraisalRecord)){
                        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加设备检定记录"+testInstrumentAppraisalRecord.getId()+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG,true);
                        return ResultUtil.success("添加成功!");
                    }else {
                        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改设备检定记录失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
                        return ResultUtil.error("添加失败，未知异常!");
                    }
                }else {
                    return ResultUtil.error("必须上传相关资料");
                }
            }else {
                return ResultUtil.error("没有找到对应的设备信息");
            }
        }else {
                return ResultUtil.error("设备编号为空");
        }
    }

    @Override
    public Result updInstrumentAppraisalRecord(TestInstrumentAppraisalRecord testInstrumentAppraisalRecord) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testInstrumentAppraisalRecord.getCode()!=null&&testInstrumentAppraisalRecord.getId()!=null){
            TestInstrument testInstrument=testInstrumentService.getOne(new QueryWrapper<TestInstrument>().eq("id",testInstrumentAppraisalRecord.getCode()).eq("del_flag",0));
            if (testInstrument!=null){
                if (testInstrumentAppraisalRecord.getFileUrl()!=null){
                    testInstrumentAppraisalRecord.setName(testInstrument.getName());
                    testInstrumentAppraisalRecord.setUpdateTime(new Date());
                    if (this.updateById(testInstrumentAppraisalRecord)){
                        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改设备检定记录"+testInstrumentAppraisalRecord.getId()+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG,true);
                        return ResultUtil.success("修改成功!");
                    }else {
                        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改设备检定记录"+testInstrumentAppraisalRecord.getId()+"失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
                        return ResultUtil.error("修改失败，未知异常!");
                    }
                }else {
                    return ResultUtil.error("必须上传相关资料");
                }
            }else {
                return ResultUtil.error("没有找到对应的设备信息");
            }
        }else {
            return ResultUtil.error("设备编号为空");
        }
    }

    @Override
    public Result delInstrumentAppraisalRecord(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestInstrumentAppraisalRecord> testInstrumentList=new ArrayList<>();
        for (Long aLong : idList) {
            TestInstrumentAppraisalRecord testInstrument=new TestInstrumentAppraisalRecord();
            testInstrument.setUpdateTime(new Date());
            testInstrument.setDelFlag(1);
            testInstrument.setId(aLong.intValue());
            String url=this.getById(aLong).getFileUrl();
            if (url!=null){
                sysOssService.delAnnounce(url);
            }
            testInstrumentList.add(testInstrument);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testInstrumentList)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除设备检定记录"+idStr+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除设备检定记录"+idStr+"失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }
}


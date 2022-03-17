package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.TestStandardFileDao;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestStandardFileService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 检验依据标准表(TestStandardFile)表服务实现类
 *
 * @author makejava
 * @since 2022-03-09 10:22:55
 */
@Service("testStandardFileService")
public class TestStandardFileServiceImpl extends ServiceImpl<TestStandardFileDao, TestStandardFile> implements TestStandardFileService {
    @Resource
    private LogManagerService logManagerService;
//添加
    @Override
    public Result addTestStandardFile(TestStandardFile testStandardFile) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testStandardFile.getName()==null){
            return ResultUtil.error("文件名称不能为空");
        }
        testStandardFile.setStatus("0");
        testStandardFile.setCreateTime(new Date());
        if (this.save(testStandardFile)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加检测标准"+testStandardFile.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加检测标准失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

//    修改
    @Override
    public Result updTestStandardFile(TestStandardFile testStandardFile) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testStandardFile.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testStandardFile.getName()==null){
            return ResultUtil.error("名称不能为空");
        }
        if (this.updateById(testStandardFile)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改检测标准"+testStandardFile.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改检测标准"+testStandardFile.getId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }

    }
//删除
    @Override
    public Result delTestStandardFile(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestStandardFile> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestStandardFile testStandardFile=new TestStandardFile();
            testStandardFile.setId(aLong.intValue());
            testStandardFile.setStatus("0");
            testStandardFile.setFileUrl("");
            testStandardFile.setDelFlag(1);
            testMethods.add(testStandardFile);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testMethods)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除检测标准"+idStr+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除检测标准"+idStr+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }

    }
}


package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.TestMethodDao;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestMethodService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 检测方法(TestMethod)表服务实现类
 *
 * @author makejava
 * @since 2022-03-02 10:04:05
 */
@Service("testMethodService")
public class TestMethodServiceImpl extends ServiceImpl<TestMethodDao, TestMethod> implements TestMethodService {
    @Resource
    private LogManagerService logManagerService;
    @Override
    public Result addTestMethod(TestMethod TestMethod) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (TestMethod.getName()==null){
            return ResultUtil.error("检测方法名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestMethod>().eq("name",TestMethod.getName()).eq("del_flag",0))!=null){
            return ResultUtil.error("检测方法名称重复");
        }
        TestMethod.setStatus("0");
        TestMethod.setDelFlag(0);
        TestMethod.setCreateTime(new Date());
        if (this.save(TestMethod)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加检测方法"+TestMethod.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加检测方法失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updTestMethod(TestMethod TestMethod) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (TestMethod.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (TestMethod.getName()==null){
            return ResultUtil.error("检测方法名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestMethod>().eq("name",TestMethod.getName()).eq("del_flag",0).ne("id",TestMethod.getId()))!=null){
            return ResultUtil.error("检测方法名称重复");
        }
        TestMethod.setUpdateTime(new Date());
        if (this.updateById(TestMethod)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改检测方法"+TestMethod.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改检测方法"+TestMethod.getId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delTestMethod(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestMethod> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestMethod testMethod=new TestMethod();
            testMethod.setUpdateTime(new Date());
            testMethod.setDelFlag(1);
            testMethod.setId(aLong.intValue());
            testMethods.add(testMethod);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testMethods)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除检测方法"+idStr+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除检测方法"+idStr+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }
}


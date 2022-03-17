package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.mapper.MehordDao;
import com.lims.manage.erp.entity.Mehord;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.MehordService;
import com.lims.manage.erp.service.TestProductService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * (Mehord)表服务实现类
 *
 * @author makejava
 * @since 2022-03-07 16:29:31
 */
@Service("mehordService")
public class MehordServiceImpl extends ServiceImpl<MehordDao, Mehord> implements MehordService {
    @Resource
    private TestProductService testProductService;
    @Resource
    private LogManagerService logManagerService;

    @Override
    public Result addMethod(Mehord Mehord) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (Mehord.getPlan()==null){
            return ResultUtil.error("检测妙计不能为空");
        }
        if (Mehord.getTitle()==null){
            return ResultUtil.error("检测标题不能为空");
        }
        Mehord.setStatus("0");
        Mehord.setDelFlag(0);
        Mehord.setCreateTime(new Date());
        System.out.println(testProductService.getById(Mehord.getProducttypeid()));
        Mehord.setProducttypename(testProductService.getById(Mehord.getProducttypeid()).getProductName());
        if (this.save(Mehord)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加妙计"+Mehord.getId()+"成功!", Const.KNOWLEDGE_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加妙计失败!", Const.KNOWLEDGE_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updMethod(Mehord Mehord) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (Mehord.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (Mehord.getPlan()==null){
            return ResultUtil.error("检测妙计不能为空");
        }
        if (this.getOne(new QueryWrapper<Mehord>().eq("plan",Mehord.getPlan()).eq("del_flag",0).ne("id",Mehord.getId()))!=null){
            return ResultUtil.error("检测方法名称重复");
        }
        Mehord.setUpdateTime(new Date());
        if (this.updateById(Mehord)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改妙计"+Mehord.getId()+"成功!", Const.KNOWLEDGE_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改妙计"+Mehord.getId()+"失败!", Const.KNOWLEDGE_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }

    }

    @Override
    public Result delMethod(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<Mehord> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            Mehord testMethod=new Mehord();
            testMethod.setUpdateTime(new Date());
            testMethod.setDelFlag(1);
            testMethod.setId(aLong.intValue());
            testMethods.add(testMethod);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testMethods)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除妙计"+idStr+"成功!", Const.KNOWLEDGE_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除妙计"+idStr+"失败!", Const.KNOWLEDGE_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }
}


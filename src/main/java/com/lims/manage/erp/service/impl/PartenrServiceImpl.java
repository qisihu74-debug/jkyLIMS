package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.PartenrDao;
import com.lims.manage.erp.entity.Partenr;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.PartenrService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 合作伙伴（用户）(Partenr)表服务实现类
 *
 * @author makejava
 * @since 2022-03-09 16:01:29
 */
@Service("partenrService")
public class PartenrServiceImpl extends ServiceImpl<PartenrDao, Partenr> implements PartenrService {
    @Resource
    private LogManagerService logManagerService;
    @Override
    public Result addpartenr(Partenr partenr) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (partenr.getPartnername()==null){
            return ResultUtil.error("合作公司名称不能为空");
        }
        if (this.getOne(new QueryWrapper<Partenr>().eq("partnername",partenr.getPartnername()))!=null){
            return ResultUtil.error("合作公司名称重复");
        }
        partenr.setStatus("0");
        if (partenr.getRemark() == null) {
            partenr.setRemark("");
        }
        partenr.setCreateTime(new Date());
        if (this.save(partenr)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加合作伙伴"+partenr.getId()+"成功!", Const.PARTNERSHIP_MANAGEMENT_LOG,true);
            return ResultUtil.success("合作伙伴信息添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加合作伙伴信息失败!", Const.PARTNERSHIP_MANAGEMENT_LOG,false);
            return ResultUtil.error("合作伙伴信息添加失败，未知异常!");
        }

    }

    @Override
    public Result updpartenr(Partenr partenr) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (partenr.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (partenr.getPartnername()==null){
            return ResultUtil.error("合作公司名称不能为空");
        }
        if (this.getOne(new QueryWrapper<Partenr>().eq("partnername",partenr.getPartnername()).eq("del_flag",0).ne("id",partenr.getId()))!=null){
            return ResultUtil.error("合作公司名称重复");
        }
        partenr.setUpdateTime(new Date());
        if (this.updateById(partenr)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改合作伙伴"+partenr.getId()+"成功!", Const.PARTNERSHIP_MANAGEMENT_LOG,true);
            return ResultUtil.success("合作伙伴信息修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改合作伙伴"+partenr.getId()+"失败!", Const.PARTNERSHIP_MANAGEMENT_LOG,false);
            return ResultUtil.error("合作伙伴信息修改失败，未知异常!");
        }

    }

    @Override
    public Result delPatent(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<Partenr> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            Partenr partenr=new Partenr();
            partenr.setId(aLong.intValue());
            partenr.setStatus("0");
            partenr.setRemark("0");
            partenr.setDelFlag(1);
            partenr.setUpdateTime(new Date());
            testMethods.add(partenr);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testMethods)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除合作伙伴"+idStr+"成功!", Const.PARTNERSHIP_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除合作伙伴"+idStr+"失败!", Const.PARTNERSHIP_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }

    }
}


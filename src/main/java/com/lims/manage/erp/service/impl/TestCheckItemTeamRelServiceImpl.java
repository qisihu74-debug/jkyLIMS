package com.lims.manage.erp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestCheckItemTeamRelDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestCheckItemTeamRelService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TestCheckItemTeamRelVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 团队检测项目(TestCheckItemTeamRel)表服务实现类
 *
 * @author makejava
 * @since 2022-03-18 10:01:57
 */
@Service("testCheckItemTeamRelService")
public class TestCheckItemTeamRelServiceImpl extends ServiceImpl<TestCheckItemTeamRelDao, TestCheckItemTeamRel> implements TestCheckItemTeamRelService {
    @Resource
    private TestCheckItemTeamRelDao testCheckItemTeamRelDao;
    @Resource
    private LogManagerService logManagerService;
    @Override
    public IPage<TestCheckItemTeamRelVo> getPageList(Page<TestCheckItemTeamRelVo> page, QueryWrapper<TestCheckItemTeamRel> queryWrapper) {
        return testCheckItemTeamRelDao.getPageList(page,queryWrapper);
    }

    @Override
    public Result addTestCheckItemTeamRel(TestCheckItemTeamRel testCheckItemTeamRel) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testCheckItemTeamRel.getCheckItemId()!=null&&testCheckItemTeamRel.getTeamId()!=null&&testCheckItemTeamRel.getProductId()!=null){
            QueryWrapper<TestCheckItemTeamRel> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("product_id",testCheckItemTeamRel.getProductId());
            queryWrapper.eq("check_item_id",testCheckItemTeamRel.getCheckItemId());
            queryWrapper.eq("team_id",testCheckItemTeamRel.getTeamId());
            if (this.list(queryWrapper).size()>0){
                return ResultUtil.error("同产品检测项重复！");
            }
            if (this.save(testCheckItemTeamRel)){
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加科室检测项"+testCheckItemTeamRel.getId()+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
                return ResultUtil.success("添加成功");
            }else {
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加科室检测项失败!", Const.TEAM_MANAGEMENT_LOG,false);
                return ResultUtil.error("添加失败");
            }
        }else {
            return ResultUtil.error("缺少必要参数！");
        }
    }

    @Override
    public Result updTestCheckItemTeamRel(TestCheckItemTeamRel testCheckItemTeamRel) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testCheckItemTeamRel.getId()!=null&&testCheckItemTeamRel.getCheckItemId()!=null&&testCheckItemTeamRel.getTeamId()!=null&&testCheckItemTeamRel.getProductId()!=null){
            QueryWrapper<TestCheckItemTeamRel> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("product_id",testCheckItemTeamRel.getProductId());
            queryWrapper.eq("check_item_id",testCheckItemTeamRel.getCheckItemId());
            queryWrapper.eq("team_id",testCheckItemTeamRel.getTeamId());
            queryWrapper.ne("id",testCheckItemTeamRel.getId());
            if (this.list(queryWrapper).size()>0){
                return ResultUtil.error("同产品检测项重复！");
            }
            if (this.updateById(testCheckItemTeamRel)){
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改科室检测项"+testCheckItemTeamRel.getId()+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
                return ResultUtil.success("修改成功");
            }else {
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改科室检测项"+testCheckItemTeamRel.getId()+"失败!", Const.TEAM_MANAGEMENT_LOG,false);
                return ResultUtil.error("修改失败");
            }
        }else {
            return ResultUtil.error("缺少必要参数！");
        }
    }

    @Override
    public Result delTestCheckItemTeamRel(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        String idStr=idList.toString();
        if (this.removeByIds(idList)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除科室检测项"+idStr+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除科室检测项"+idStr+"失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }


}


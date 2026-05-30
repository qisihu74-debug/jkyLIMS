package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.SysLog;
import com.lims.manage.erp.entity.SysOperLog;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.LogManagerDao;
import com.lims.manage.erp.mapper.SysOperateLogDao;
import com.lims.manage.erp.service.LogManagerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2021/11/15 16:41
 * @Copyright © 河南交科院
 */
@Service
public class LogManagerServiceImpl extends ServiceImpl<LogManagerDao, SysLog> implements LogManagerService {
    @Autowired
    private SysOperateLogDao sysOperateLogDao;
    @Override
    public void addBatchOpSysLog(SysUserEntity sysUser, List<String> optDescs, String type) {
        List<SysLog> logs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(optDescs)){
            for (String des:optDescs) {
                SysLog sysLog = new SysLog();
                sysLog.setOperate_time(new Timestamp(new Date(System.currentTimeMillis()).getTime()));
                sysLog.setOperateDesc(des);
                sysLog.setType(type);
                sysLog.setUserId(sysUser.getUserId());
                sysLog.setUserDept(sysUser.getUserDept());
                sysLog.setUserName(sysUser.getUsername());
                logs.add(sysLog);
            }
        }
        super.saveBatch(logs);
    }

    @Override
    public void addOpSysLog(SysUserEntity sysUser, String optDescs, String type,boolean state) {
        SysLog sysLog = new SysLog();
        sysLog.setOperate_time(new Timestamp(new Date(System.currentTimeMillis()).getTime()));
        sysLog.setOperateDesc(optDescs);
        sysLog.setType(type);
        sysLog.setUserId(sysUser.getUserId());
        sysLog.setUserDept(sysUser.getUserDept());
        sysLog.setUserName(sysUser.getUsername());
        sysLog.setIsState(state);
        this.baseMapper.insert(sysLog);
    }

    @Override
    public PageInfo<SysLog> getLogList(Integer logType, Integer pageNum, Integer pageSize, String operator, Long startDate, Long endDate) {
        PageHelper.startPage(pageNum,pageSize);
        LambdaQueryWrapper<SysLog> queryWrapper = new LambdaQueryWrapper<>();
        List<Integer> types = Lists.newArrayList();
        if (logType != null){
            if (logType == 5){
                types.add(5);
                types.add(6);
                types.add(7);
                types.add(8);
                types.add(9);
            }else {
                types.add(logType);
            }
        }
        queryWrapper.in(org.apache.commons.collections.CollectionUtils.isNotEmpty(types),SysLog::getType,types)
                .and(startDate != null || endDate != null, wrapper ->wrapper.ge(startDate != null,SysLog::getOperate_time, startDate == null?null:new Date(startDate))
                        .or().le(endDate != null,SysLog::getOperate_time,endDate == null?new Date(System.currentTimeMillis()):new Date(endDate))
                        .or().like(StringUtils.isNotEmpty(operator),SysLog::getUserName,operator)
                )
                .orderByDesc(SysLog::getOperate_time);
        List<SysLog> logs = this.baseMapper.selectList(queryWrapper);
        PageInfo<SysLog> pageInfo = new PageInfo<>(logs);
        return pageInfo;
    }

    @Override
    public void insertOperlog(SysOperLog operLog) {
        sysOperateLogDao.save(operLog);
    }
}

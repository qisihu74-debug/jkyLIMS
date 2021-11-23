package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysLog;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.LogManagerDao;
import com.lims.manage.erp.service.LogManagerService;
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
    public void addOpSysLog(SysUserEntity sysUser, String optDescs, String type) {
        SysLog sysLog = new SysLog();
        sysLog.setOperate_time(new Timestamp(new Date(System.currentTimeMillis()).getTime()));
        sysLog.setOperateDesc(optDescs);
        sysLog.setType(type);
        sysLog.setUserId(sysUser.getUserId());
        sysLog.setUserDept(sysUser.getUserDept());
        sysLog.setUserName(sysUser.getUsername());
        this.baseMapper.insert(sysLog);
    }
}

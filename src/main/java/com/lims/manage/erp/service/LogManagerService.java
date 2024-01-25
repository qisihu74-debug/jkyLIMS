package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.SysLog;
import com.lims.manage.erp.entity.SysOperLog;
import com.lims.manage.erp.entity.SysUserEntity;

import java.sql.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2021/11/15 16:40
 * @Copyright © 河南交科院
 */
public interface LogManagerService extends IService<SysLog> {

    /**
     * 批量操作日志保存接口
     * @param sysUser 用户信息
     * @param optDescs 日志描述
     * @param type 日志类型
     */
    void addBatchOpSysLog(SysUserEntity sysUser, List<String> optDescs, String type);

    /**
     * 操作日志保存接口
     * @param sysUser 用户信息
     * @param optDescs 日志描述
     * @param type 日志类型
     */
    void addOpSysLog(SysUserEntity sysUser, String optDescs, String type,boolean state);

    PageInfo<SysLog> getLogList(Integer logType, Integer pageNum, Integer pageSize, String operator, Long startDate, Long endDate);

    void insertOperlog(SysOperLog operLog);
}

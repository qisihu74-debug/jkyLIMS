package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.SysLog;
import com.lims.manage.erp.entity.SysMenuEntity;
import com.lims.manage.erp.entity.SysUserEntity;

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
     * 操作日志保存接口
     * @param sysUser
     * @param optDescs
     * @param type
     */
    void addBatchOpSysLog(SysUserEntity sysUser, List<String> optDescs, String type);
}

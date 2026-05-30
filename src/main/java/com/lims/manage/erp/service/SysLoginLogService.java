package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.SysLog;
import com.lims.manage.erp.entity.SysLogininfor;
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
public interface SysLoginLogService extends IService<SysLogininfor> {

    /**
     * 插入用户登录日志
     * @param sysLogininfor
     */
    void insertLogininfor(SysLogininfor sysLogininfor);

}

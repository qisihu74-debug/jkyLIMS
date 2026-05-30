package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysLogininfor;
import com.lims.manage.erp.mapper.SysLoginLogDao;
import com.lims.manage.erp.service.SysLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-01-18 11:28
 * @Copyright © 河南交科院
 */
@Service
public class SysLoginLogServiceImp extends ServiceImpl<SysLoginLogDao, SysLogininfor> implements SysLoginLogService {
    @Autowired
    private SysLoginLogDao sysLoginLogDao;
    @Override
    public void insertLogininfor(SysLogininfor sysLogininfor) {
        sysLoginLogDao.save(sysLogininfor);
    }
}

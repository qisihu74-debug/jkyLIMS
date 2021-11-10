package com.lims.manage.demo.service.impl;

import com.lims.manage.demo.entity.SysFunction;
import com.lims.manage.demo.mapper.SysUserFuctionDao;
import com.lims.manage.demo.service.SysUserFuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.service.impl
 * @desc
 * @date 2021/11/10 14:16
 * @Copyright © 河南交科院
 */
@Service
public class SysSysUserFuctionServiceImpl implements SysUserFuctionService {
    @Autowired
    private SysUserFuctionDao fuctionDao;

    @Override
    public List<SysFunction> getFunctionByuserId(Long userId) {

        return fuctionDao.getFunctionByuserId(userId);
    }
}

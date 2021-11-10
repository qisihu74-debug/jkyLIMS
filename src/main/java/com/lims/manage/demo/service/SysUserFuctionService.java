package com.lims.manage.demo.service;

import com.lims.manage.demo.entity.SysFunction;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.service
 * @desc
 * @date 2021/11/10 14:14
 * @Copyright © 河南交科院
 */
public interface SysUserFuctionService {

    /**
     * 根据用户id获取用户菜单列表
     * @param userId
     * @return
     */
    List<SysFunction> getFunctionByuserId(Long userId);
}

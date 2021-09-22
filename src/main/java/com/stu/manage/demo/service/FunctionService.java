package com.stu.manage.demo.service;

import com.stu.manage.demo.entity.FunctionEntity;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.service
 * @desc
 * @date 2021/9/22 10:21
 * @Copyright © 河南交科院
 */
public interface FunctionService {

    /**
     * 根据登录账号查询权限内的菜单项
     * @param id
     * @return
     */
    List<FunctionEntity> getFunctionsById(int id);
}

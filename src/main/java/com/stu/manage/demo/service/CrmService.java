package com.stu.manage.demo.service;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.service
 * @desc
 * @date 2021/9/16 14:35
 * @Copyright © 河南交科院
 */
public interface CrmService {
    void getCustomer(String tokenUrl, String appKey, String appsecret) throws Exception;
}

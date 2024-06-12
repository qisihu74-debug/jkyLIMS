package com.lims.manage.erp.service;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-06-11 16:32
 * @Copyright © 河南交科院
 */
public interface SnRuleService {
    /**
     * 根据类型获取编号
     * @param type
     * @param code 团队编号
     * @return
     */
    String getSnByType(String type,String code);
}

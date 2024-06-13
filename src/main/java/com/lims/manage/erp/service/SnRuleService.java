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
     * @param type 数据字典中定义的常量目前有：样品编号，委托编号，报告编号，任务编号
     * @param code 任务编号类型时需要传团队编号
     * @return
     */
    String getSnByType(String type,String code);
}

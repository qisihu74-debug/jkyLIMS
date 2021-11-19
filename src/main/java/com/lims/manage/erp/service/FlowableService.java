package com.lims.manage.erp.service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2021/11/19 16:49
 * @Copyright © 河南交科院
 */
public interface FlowableService {

    /**
     * 获取已经部署过的bpmn20.xml
     * @return
     */
    List<String> getDeployed();
}

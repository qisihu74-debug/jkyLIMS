package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.mapper.FlowableDao;
import com.lims.manage.erp.service.FlowableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2021/11/19 16:49
 * @Copyright © 河南交科院
 */
@Service
public class FlowableServiceImpl implements FlowableService {
    @Autowired
    private FlowableDao dao;

    @Override
    public List<String> getDeployed() {
        return dao.getDeployed();
    }
}

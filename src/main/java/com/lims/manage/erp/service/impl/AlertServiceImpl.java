package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.AlertEntity;
import com.lims.manage.erp.mapper.AlertDao;
import com.lims.manage.erp.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2022/5/31 15:49
 * @Copyright © 河南交科院
 */
@Service
public class AlertServiceImpl extends ServiceImpl<AlertDao, AlertEntity> implements AlertService {
    @Autowired
    private AlertDao alertDao;

    @Override
    public void deleteByEntrustId(Long id) {
        alertDao.deleteByEntrustId(id);
    }
}

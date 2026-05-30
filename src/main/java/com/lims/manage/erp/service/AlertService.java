package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.AlertEntity;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2022/5/31 15:49
 * @Copyright © 河南交科院
 */
public interface AlertService extends IService<AlertEntity> {

    void deleteByEntrustId(Long id);
}

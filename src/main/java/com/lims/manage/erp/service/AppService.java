package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.JsonRootBean;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2022-08-18 14:54
 * @Copyright © 河南交科院
 */
public interface AppService extends IService<JsonRootBean> {

    int getIndex();

    void updateIndex(int i);
}

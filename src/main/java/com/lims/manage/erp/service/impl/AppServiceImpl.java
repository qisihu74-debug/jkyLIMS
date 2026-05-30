package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.CodeEntity;
import com.lims.manage.erp.entity.JsonRootBean;
import com.lims.manage.erp.mapper.AppServiceDao;
import com.lims.manage.erp.mapper.CodeDao;
import com.lims.manage.erp.service.AppService;
import com.lims.manage.erp.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2022-08-18 14:54
 * @Copyright © 河南交科院
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppServiceDao, JsonRootBean> implements AppService {

    @Autowired
    private AppServiceDao dao;
    @Override
    public int getIndex() {
        return dao.getIndex();
    }

    @Override
    public void updateIndex(int i) {
        dao.updateIndex(i);
    }
}

package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.StandardNovelty;
import com.lims.manage.erp.mapper.StandardNoveltyDao;
import com.lims.manage.erp.service.StandardNoveltyService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-01-23 11:09
 * @Copyright © 河南交科院
 */
@Service
public class StandardNoveltyServiceImp extends ServiceImpl<StandardNoveltyDao, StandardNovelty> implements StandardNoveltyService {
    @Autowired
    private StandardNoveltyDao standardNoveltyDao;
    @Override
    public void updateBatchByCode(List<StandardNovelty> list) {
        if (CollectionUtils.isNotEmpty(list)){
            standardNoveltyDao.updateBatchByCode(list);
        }
    }

    @Override
    public void delete() {
        standardNoveltyDao.deleteAll();
    }
}

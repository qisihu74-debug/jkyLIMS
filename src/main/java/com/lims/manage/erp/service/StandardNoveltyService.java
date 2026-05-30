package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.StandardNovelty;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-01-23 11:08
 * @Copyright © 河南交科院
 */
public interface StandardNoveltyService extends IService<StandardNovelty> {

    void updateBatchByCode(List<StandardNovelty> list);

    void delete();
}

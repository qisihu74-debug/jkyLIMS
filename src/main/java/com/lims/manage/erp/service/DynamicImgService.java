package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.DynamicImg;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2022-08-16 10:32
 * @Copyright © 河南交科院
 */
public interface DynamicImgService extends IService<DynamicImg> {
    void delete();
}

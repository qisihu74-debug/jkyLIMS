package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.result.Result;

/**
 * (Patent)表服务接口
 *
 * @author makejava
 * @since 2022-03-08 10:40:18
 */
public interface PatentService extends IService<Patent> {

    Result addPatent(Patent Patent);

    Result updPatent(Patent Patent);

    Result delPatent(String id);

}


package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.Mehord;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * (Mehord)表服务接口
 *
 * @author makejava
 * @since 2022-03-07 16:29:31
 */
public interface MehordService extends IService<Mehord> {

    Result addMethod(Mehord Mehord);
    Result updMethod(Mehord Mehord);
    Result delMethod(List<Long> idList);
}


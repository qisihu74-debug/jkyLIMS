package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.QsActiveEntity;
import com.lims.manage.erp.result.Result;

/**
 * @Author: DLC
 * @Date: 2024/7/10 16:23
 */
public interface ActiveService extends IService<QsActiveEntity> {

    /**
     * 创建内审活动
     *
     * @param qsActiveEntity
     * @return
     */
    Result addQsActiveData(QsActiveEntity qsActiveEntity);

    /**
     * 更新内审活动
     *
     * @param qsActiveEntity
     * @return
     */
    Result updateQsActiveData(QsActiveEntity qsActiveEntity);


    /**
     * 查询详情内审活动
     *
     * @param activeId
     * @return
     */
    Result queryDetailsQsActiveData(String activeId);

}

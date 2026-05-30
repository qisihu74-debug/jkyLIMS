package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.DataTransferRelation;

import java.util.Set;

/**
 * 数据流转关系业务层接口
 * @author: zhq
 * @date: 2024-06-13
 * @version: v1.0
 */
public interface DataTransferRelationService extends IService<DataTransferRelation> {

    /**
     * 添加数据流转关系
     * @param dataTransferRelation 数据流转关系
     */
    boolean addTransferRelation(DataTransferRelation dataTransferRelation);

    /**
     * 根据数据id获取和数据有关联的数据id列表
     * @param DataId 数据id
     * @return 数据id列表
     */
    Set<String> getDataIdSet(String DataId);
}

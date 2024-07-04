package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DataTransferRelation;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * 数据流转关系dao层接口
 * @author: zhq
 * @date: 2024-06-13
 * @version: v1.0
 */
public interface DataTransferRelationDao extends BaseMapper<DataTransferRelation> {

    /**
     * 获取流转关系条数
     * @param transferRelation 流转关系
     * @return 关系条数
     */
    Integer getTransferRelationCount(@Param("transferRelation") DataTransferRelation transferRelation);

    /**
     * 根据数据id获取和数据有关联的数据id列表
     * @param dataId 数据id
     * @return 数据id列表
     */
    Set<String> getDataIdSet(@Param("dataId") String dataId);
}

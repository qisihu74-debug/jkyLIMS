package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.DataTransferRecord;
import com.lims.manage.erp.entity.DataTransferRelation;
import com.lims.manage.erp.vo.DataTransferRecordVo;

import java.util.List;

/**
 * 数据流转记录业务层接口
 *
 * @author: zhq
 * @date: 2024-06-12
 * @version: v1.0
 */
public interface DataTransferRecordService extends IService<DataTransferRecord> {

    /**
     * 添加数据流转记录
     *
     * @param dataTransferRecord 数据信息
     * @return 添加结果
     */
    boolean addDataTransfer(DataTransferRecord dataTransferRecord);

    /**
     * 添加数据流转关系
     *
     * @param dataTransferRelation 数据流转关系
     */
    boolean addTransferRelation(DataTransferRelation dataTransferRelation);

    /**
     * 根据数据id获取数据的流转记录列表
     *
     * @param dataId 数据Id
     * @return 流转记录列表
     */
    List<DataTransferRecordVo> getTransferRecordList(String dataId);
}

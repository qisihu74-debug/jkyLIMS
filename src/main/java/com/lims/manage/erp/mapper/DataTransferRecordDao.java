package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DataTransferRecord;
import com.lims.manage.erp.vo.DataTransferRecordVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 数据流转记录dao层接口
 * @author: zhq
 * @date: 2024-06-12
 * @version: v1.0
 */
public interface DataTransferRecordDao extends BaseMapper<DataTransferRecord> {

    /**
     * 根据数据id获取数据的流转记录列表
     * @param dataIdSet 数据Id列表
     * @return 流转记录列表
     */
    List<DataTransferRecordVo> getTransferRecordList(@Param("dataIdSet") Set<String> dataIdSet);

}

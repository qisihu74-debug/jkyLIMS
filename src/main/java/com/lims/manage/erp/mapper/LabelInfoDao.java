package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.LabelInfo;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 标签信息dao层接口
 * @author: zhq
 * @date: 2023-01-05
 * @version: v1.0
 */
public interface LabelInfoDao extends BaseMapper<LabelInfo> {

    /**
     * 获取该标签有多少条记录涉及
     * @param labelId 标签id
     * @return Integer
     */
    Integer getLabelCount(@Param("labelId") String labelId);
}

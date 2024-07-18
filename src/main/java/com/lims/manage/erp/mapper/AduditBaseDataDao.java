package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.AduditBaseData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-07-05 15:11
 * @Copyright © 河南交科院
 */
public interface AduditBaseDataDao extends BaseMapper<AduditBaseData> {

    /**
     * 查询检测内容基本信息
     * @param type
     * @param dir
     * @param content
     * @param method
     * @param subject
     * @return
     */
    List<AduditBaseData> selectListBySub(@Param("type") String type, @Param("dir") String dir, @Param("content") String content,
                                         @Param("method") String method, @Param("subject") String subject, @Param("divideId") Integer divideId);
}

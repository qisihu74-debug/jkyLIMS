package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportRecordEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportRecordEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReportRecordEntity record);

    int insertSelective(ReportRecordEntity record);

    ReportRecordEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReportRecordEntity record);

    int updateByPrimaryKey(ReportRecordEntity record);

    /**
     * 待盖章、已盖章列表查询
     * @param search
     * @param type
     * @return
     */
    List<ReportRecordEntity> getSealList(@Param("type") String type, @Param("search") String search);
}
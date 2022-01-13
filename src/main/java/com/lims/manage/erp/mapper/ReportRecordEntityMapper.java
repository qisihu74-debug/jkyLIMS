package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportRecordEntity;

public interface ReportRecordEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReportRecordEntity record);

    int insertSelective(ReportRecordEntity record);

    ReportRecordEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReportRecordEntity record);

    int updateByPrimaryKey(ReportRecordEntity record);
}
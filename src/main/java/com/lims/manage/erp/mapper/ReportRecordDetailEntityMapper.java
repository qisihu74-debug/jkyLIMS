package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface ReportRecordDetailEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReportRecordDetailEntity record);

    int insertSelective(ReportRecordDetailEntity record);

    ReportRecordDetailEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReportRecordDetailEntity record);

    int updateByPrimaryKey(ReportRecordDetailEntity record);
}
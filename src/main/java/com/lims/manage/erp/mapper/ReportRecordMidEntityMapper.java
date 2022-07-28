package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportRecordMidEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface ReportRecordMidEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReportRecordMidEntity record);

    int insertSelective(ReportRecordMidEntity record);

    ReportRecordMidEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReportRecordMidEntity record);

    int updateByPrimaryKeyWithBLOBs(ReportRecordMidEntity record);

    int updateByPrimaryKey(ReportRecordMidEntity record);
}
package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ReportRecordDetailEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReportRecordDetailEntity record);

    int insertSelective(ReportRecordDetailEntity record);

    ReportRecordDetailEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReportRecordDetailEntity record);

    int updateByPrimaryKey(ReportRecordDetailEntity record);

    /**
     * 根据报告编号获取报下的详细信息
     * @param reportCode
     * @return
     */
    List<ReportRecordDetailEntity> getReportDetailByCode(String reportCode);
}
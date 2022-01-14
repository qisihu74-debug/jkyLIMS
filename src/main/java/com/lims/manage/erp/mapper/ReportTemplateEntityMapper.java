package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ReportTemplateEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ReportTemplateEntity record);

    int insertSelective(ReportTemplateEntity record);

    ReportTemplateEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ReportTemplateEntity record);

    int updateByPrimaryKey(ReportTemplateEntity record);

    List<ReportTemplateEntity> getReportTemplateList(String productId);
}
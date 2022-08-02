package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.vo.ReportProductRelVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    List<ReportTemplateEntity> getReportTemplateListOld(String productId);
    List<ReportTemplateEntity> getReportTemplateList(@Param("allReportId") List<Long> allReportId);
    List<ReportTemplateEntity> getReportTemplateList0706(@Param("entrustId") Long entrustId,@Param("sampleId") Integer sampleId);

    /**
     *
     * @param entrustId
     * @return
     */
    List<ReportProductRelVo> getSampleIdByEntrust(@Param("entrustId") Long entrustId,@Param("sampleIds") List<Integer> sampleIds);
}
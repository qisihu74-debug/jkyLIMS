package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportOriginalEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ReportOriginalEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReportOriginalEntity record);

    int insertSelective(ReportOriginalEntity record);

    ReportOriginalEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReportOriginalEntity record);

    int updateByPrimaryKey(ReportOriginalEntity record);

    /**
     * 查询报告列表
     * @param param
     * @return
     */
    List<ReportOriginalEntity> getReportList(ReportOriginalEntity param);

    /**
     * 批量删除报告模板
     * @param idList
     * @return
     */
    int deleteByIds(@Param("idList") List<Long> idList);
}
package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ReportRecordDetailEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReportRecordDetailEntity record);

    int insertSelective(ReportRecordDetailEntity record);

    ReportRecordDetailEntity selectByPrimaryKey(Long id);

    ReportRecordDetailEntity selectByRecordIdAndItemId(@Param("recordId") Long recordId,@Param("checkItemId") Integer checkItemId);

    int updateByPrimaryKeySelective(ReportRecordDetailEntity record);

    int updateByPrimaryKey(ReportRecordDetailEntity record);

    /**
     * 根据报告编号获取报下的详细信息
     *
     * @param reportCode
     * @return
     */
    List<ReportRecordDetailEntity> getReportDetailByCode(String reportCode);

    /**
     * 更新检测项数据
     *
     * @param record
     * @return
     */
    int updateByRecordIdSelective(ReportRecordDetailEntity record);

    /**
     * 根据recordId获取检测项信息
     *
     * @param recordId
     * @return
     */
    List<ReportRecordDetailEntity> getCheckInfoByRecordId(Long recordId);

    List<ReportRecordDetailEntity> getCheckInfoByRecordIdAndCheckId(Long recordId,List<Long> checkIds);

    /**
     * 查询已记录的检测项ID
     * @param recordId
     * @return
     */
    List<Long> getCheckItemIds(Long recordId,Long taskId);

    @Select("SELECT distinct \n" +
            "\trrd.check_item_name As name \n" +
            "FROM\n" +
            "\ttest_report_record rd\n" +
            "\tLEFT JOIN test_report_record_detail rrd ON rd.id = rrd.record_id \n" +
            "WHERE rrd.judge_result='合格'\n" +
            "\tAND rd.entrustment_id = #{entrustId}\n" +
            "\tAND rrd.check_item_id=#{checkItemId}")
    String getIdByItemId(@Param("checkItemId") Integer checkItemId,@Param("entrustId") Long entrustId);

    /**
     * 批量删除委托单已删除的检测项
     * @param list
     * @return
     */
    int deleteByEntrustIdandCheckItemId(@Param("list")List<ReportRecordDetailEntity> list);
}

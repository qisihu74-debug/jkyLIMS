package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Mapper
public interface ReportMapper {
    /**
     * 查询可制作报告列表
     *
     * @return
     */
    List<ReportListVo> getReportList();

    /**
     * 查询可制作报告列表--科室
     *
     * @return
     */
    List<ReportListVo> getReportList2(@Param("deptIds") List<Long> deptIds,@Param("taskCode") String taskCode);

    List<ReportListVo> reportDownloadList(@Param("deptIds") List<Long> deptIds,@Param("reportCode") String reportCode);

    /**
     * 查询下载报告列表--科室
     *
     * @return
     */
    List<ReportListVo> getReportList3(@Param("deptIds") List<Long> deptIds);

    /**
     * 查询委托单--报告制作详情
     *
     * @param id
     * @return
     */
    ReportDetailVo getReportDetail1(Long id);

    /**
     * 查询委托单--报告制作详情--科室
     * @param taskId
     * @param deptIds
     * @return
     */
    ReportDetailVo getReportDetail(Long taskId,List<Long> deptIds);

    /**
     * 根据委托单ID查询所有检测项
     * @param id
     * @return
     */
    ReportHistoryDetailVo getDetailCheckItem(Long id);

    ReportDetailVo getAllReportDetail(Long id);

    List<ReportListVo> getReportList_history(ReportListVo reportListVo);

    List<ReportListVo> reportDownloadListHistory(ReportListVo reportListVo);

    /**
     * 获取历史详情
     * @param id
     * @return
     */
    ReportDetailVo getReportDetailHistory(Long id);

    List<ReportSampleDetailVo> getReportHeadDetails(Long id);

    List<ReportCheckItemDetailVo> getReportCheckItemList(@Param("id") Long id,@Param("deptIds") List<Long> deptIds);

    /**
     * 查询判定依据
     * @param id
     * @return
     */
    List<String> getJudgeBasis(Long id);

    /**
     * 查询检测依据
     * @param id
     * @return
     */
    List<String> getCheckBasis(Long id);

    /**
     * 查询设备
     * @param id
     * @return
     */
    List<String> getEquipment(Long id);

    int updateReportUrl(Long id,String url,String code);

    /**
     * 更新报告上传的url存储地址
     * @param reportCode
     * @param url
     */
    @Update("update test_report_record set report_url=#{url},verifyer=#{verifyer},issuer=#{issuer}," +
            "verifyer_id=#{verifyerId},issuer_id=#{issuerId},report_complete_time=#{now},state='3' where entrustment_id=#{entrustId}")
    void updateUrl(@Param("entrustId") String reportCode, @Param("url") String url,
                   @Param("verifyer") String verifyer, @Param("issuer") String issuer,
                   @Param("verifyerId") Long verifyerId, @Param("issuerId") Long issuerId, @Param("now")Date now);

    /**
     * 根据报告编号获取信息
     * @param reportCode
     * @return
     */
    @Select("select entrustment_id from test_report_record where report_code=#{reportCode}")
    Long getMessageByCode(@Param("reportCode") String reportCode);

    /**
     * 获取url
     * @param entrustId
     * @return
     */
    @Select("select report_url from test_report_record where entrustment_id=#{entrustId}")
    String getUrlByEntrustId(@Param("entrustId") Long entrustId);

    @Select("select report_code,sample_name from test_report_record where entrustment_id=#{entrustId}")
    ReportRecordEntity getDetailByEntrustId(Long entrustId);
}
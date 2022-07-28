package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.SealEntity;
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
     * 查询委托下样品名字
     * @param entrustId
     * @return
     */
    List<String> getSampleNames(Long entrustId);

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
    ReportDetailVo getReportDetail0621(Long taskId,List<Long> deptIds);

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

    List<ReportCheckItemDetailVo> getReportCheckItemList(@Param("id") Long id,@Param("deptIds") List<Long> deptIds,@Param("taskId") Long taskId);

    /**
     * 根据recordId查询检测项信息
     * @param recordId
     * @return
     */
    List<ReportCheckItemDetailVo> getReportCheckItemListByRecordId(@Param("recordId") Long recordId,@Param("taskId") Long taskId);

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
            "verifyer_id=#{verifyerId},issuer_id=#{issuerId},report_complete_time=#{now},state='3'," +
            "applicant=#{applicant} where entrustment_id=#{entrustId} ")
    void updateUrl(@Param("entrustId") String reportCode, @Param("url") String url,
                   @Param("verifyer") String verifyer, @Param("issuer") String issuer,
                   @Param("verifyerId") Long verifyerId, @Param("issuerId") Long issuerId,
                   @Param("now")Date now,@Param("applicant") String applicant);

    @Update("update test_report_record set verifyer=#{verifyer},issuer=#{issuer}," +
            "verifyer_id=#{verifyerId},issuer_id=#{issuerId},combine_time=#{combineTime} where entrustment_id=#{entrustId}")
    void updateVerAndIss(@Param("entrustId") String reportCode, @Param("verifyer") String verifyer, @Param("issuer") String issuer,
                   @Param("verifyerId") Long verifyerId,@Param("combineTime") Date combineTime, @Param("issuerId") Long issuerId);

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

    ReportRecordEntity getDetailByEntrustId(@Param("entrustId") Long entrustId);
    ReportRecordEntity getDetailByEntrustIdZj(@Param("entrustId") Long entrustId);
    /**
     * 获取中间报告列表
     * @param deptIds
     * @param taskCode
     * @return
     */
    List<ReportListVo> getMiddleReportList(@Param("deptIds") List<Long> deptIds,@Param("state") Integer state,@Param("taskCode") String taskCode);

    /**
     * 查询可做中间报告的检测项详情
     * @param entrustId
     * @param taskFlowId
     * @return
     */
    ReportDetailVo getMiddleReportDetail(@Param("taskFlowId") Integer taskFlowId,@Param("entrustId") Long entrustId);

    @Select("select report_url from test_report_record where id=#{id}")
    String getUrlById(@Param("id") Long id);

    void updateCategory(List<SealEntity> list);

    @Select("select entrustment_id from test_report_record where id=#{id}")
    Long getEntrustIdById(@Param("id") Long id);

    List<ReportRecordEntity> historyList(@Param("reportCode") String reportCode, @Param("reportType") String reportType, @Param("sealType") String sealType,
                                         @Param("ids") List<Integer> ids,@Param("startDate") Date startDate,@Param("endDate") Date endDate);


    List<ReportRecordEntity> exportRecords(@Param("reportCode") String reportCode, @Param("reportType") String reportType, @Param("sealType") String sealType,
                                           @Param("ids") List<Integer> ids,@Param("startDate") Date startDate,@Param("endDate") Date endDate);

    @Select("update test_report_record set inspector=#{inspector} where report_code=#{reportCode}")
    int updateInspector(@Param("reportCode") String reportCode, @Param("inspector") String inspector);

    @Select("select report_url from test_report_record where entrust_id=#{entrustId}")
    String getUrlByZjEntrustId(@Param("entrustId") Long entrustId);

    @Select("select entrust_id from test_report_record where id=#{id}")
    Long getZjEntrustIdById(@Param("id") Long id);

    @Update("update test_report_record set verifyer=#{verifyer},issuer=#{issuer}," +
            "verifyer_id=#{verifyerId},issuer_id=#{issuerId},combine_time=#{combineTime} where entrust_id=#{entrustId}")
    void updateVerAndIssZj(@Param("entrustId") String reportCode, @Param("verifyer") String verifyer, @Param("issuer") String issuer,
                         @Param("verifyerId") Long verifyerId,@Param("combineTime") Date combineTime, @Param("issuerId") Long issuerId);

    @Update("update test_report_record set report_url=#{url},verifyer=#{verifyer},issuer=#{issuer}," +
            "verifyer_id=#{verifyerId},issuer_id=#{issuerId},report_complete_time=#{now},state='3'," +
            "applicant=#{applicant} where entrust_id=#{entrustId} ")
    void updateUrlZj(@Param("entrustId") String reportCode, @Param("url") String url,
                   @Param("verifyer") String verifyer, @Param("issuer") String issuer,
                   @Param("verifyerId") Long verifyerId, @Param("issuerId") Long issuerId,
                   @Param("now")Date now,@Param("applicant") String applicant);
}

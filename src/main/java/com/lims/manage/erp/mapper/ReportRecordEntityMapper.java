package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ApproveInfo;
import com.lims.manage.erp.entity.ReportEditReq;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.ReportDetailListParamVo;
import com.lims.manage.erp.vo.ReportDetailListVo;
import com.lims.manage.erp.vo.ReportNodeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Mapper
@Component
public interface ReportRecordEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReportRecordEntity record);

    int insertSelective(ReportRecordEntity record);

    ReportRecordEntity selectByPrimaryKey(Long id);

    ReportRecordEntity getByRecordId(Long recordId);

    int updateByPrimaryKeySelective(ReportRecordEntity record);

    int updateByPrimaryKey(ReportRecordEntity record);

    /**
     * 待盖章、已盖章列表查询
     * @param search
     * @return
     */
    List<ReportRecordEntity> getSealList(@Param("search") String search,
                                         @Param("reportType") String reportType,@Param("state") String state,@Param("reportTypeStatus")Integer reportTypeStatus,@Param("ids") List<Integer> ids);


    ReportRecordEntity getDetail(Long id);
    /**
     * 更新url
     * @param id
     * @param url
     */
    void updateImgByid(Long id, String url);

    /**
     * 获取模板url、印章url
     * @param reportCode
     * @return
     */
    ReportRecordEntity getUrlByCode(String reportCode);

    /**
     * 根据报告编号获取委托单id
     * @param reportCode
     * @return
     */
    Long getEntrustIdByCode(String reportCode);

    /**
     * 查询是否存在委托单信息
     * @param entrustId
     * @return
     */
    ReportRecordEntity selectByEntrustId(Long entrustId);

    /**
     * 获取最新报告制作数据
     * @param entrustId
     * @return
     */
    ReportRecordEntity getLatestReport(Long entrustId);

    ReportNodeVo getReportNodeByEntrustId(Long entrustId);

    List<ReportNodeVo> getReportNodesByEntrustId(Long entrustId);

    /**
     * 根据recordId查询委托单信息
     * @param recordId
     * @return
     */
    ReportRecordEntity selectByRecordId(Long recordId);

    /**
     * 查询是否存在委托单信息--根据任务ID
     * @param taskId
     * @return
     */
    ReportRecordEntity selectByTaskId(Long taskId);

    /**
     * 更新报告状态
     * @param record
     * @return
     */
    int updateByEntrustIdSelective(ReportRecordEntity record);

    /**
     * 更新报告状态
     * 根据委托单id 整体修改
     * @param record
     * @return
     */
    int updateByEntrustId(ReportRecordEntity record);


    /**
     * 最终报告待邮寄报告列表及已发出报告历史列表查询
     * @param search
     * @param reportType
     * @return
     */
    List<ReportRecordEntity> getSendList(@Param("search") String search, @Param("reportType") String reportType,@Param("type") String type,@Param("reportTypeStatus")Integer reportTypeStatus);
    List<ReportRecordEntity> getSendList0623(@Param("search") String search,
                                             @Param("reportType") String reportType,
                                             @Param("type") String type,
                                             @Param("category") String category,
                                             @Param("reportTypeStatus")Integer reportTypeStatus);

    /**
     * 中间报告待邮寄报告列表及已发出报告历史列表查询
     * @param search
     * @param reportType
     * @param type
     * @param category
     * @param reportTypeStatus
     * @return
     */
    List<ReportRecordEntity> getSendList20230131MidReport(@Param("search") String search,
                                             @Param("reportType") String reportType,
                                             @Param("type") String type,
                                             @Param("category") String category,
                                             @Param("reportTypeStatus")Integer reportTypeStatus);

    String isApprove(Long id);

    /**
     * 遍历报告信息 获取 委托id 和 状态
     * @return
     */
    List<ReportRecordEntity> getReportList();

    ReportRecordEntity getReportEntrust(Long entrustId);

    /**
     * 查询当前年 最大报告编号
     * @param year
     * @return
     */
    Integer getMaxCode(String year,String code);

    /**
     *
     * @param year
     * @param code
     * @param type
     * @return
     */
    Integer getOtherMaxCode(String year,String code,String type);

    /**
     * 获取委托编号类别
     * @param entrustId
     * @return
     */
    String getEntrustCategoryType(Long entrustId);
    /**
     * 查询当前年 最大报告编号--中间
     * @param year
     * @return
     */
    Integer getMaxCodeMid(String year,String code);

    /**
     *
     * @param year
     * @param code
     * @param type
     * @return
     */
    Integer getOtherMaxCodeMid(String year,String code,String type);

    /**
     * 根据委托单id查询所用报告模板名称
     * @param entrustId
     * @return
     */
    @Select("select template_name from test_report_record where entrustment_id = #{entrustId}")
    String getReportModelNameById(@Param("entrustId") Long entrustId);

    /**
     * 根据委托单id更新契约锁响应的docId和本业务状态
     * @param docId
     * @param entrustId
     * @param state
     */
    @Update("update test_report_record set qys_docment_id=#{docId},qys_state=#{state} where entrustment_id=#{entrustId}")
    void updateDocIdAndState(@Param("entrustId") Long entrustId, @Param("docId") Long docId, @Param("state") String state);

    /**
     * 根据委托单id更新契约锁响应的contractId和本业务状态
     * @param entrustId
     * @param contractId
     * @param state
     */
    @Update("update test_report_record set contract_id=#{contractId},qys_state=#{state} where entrustment_id=#{entrustId}")
    void updateContractIdAndState(@Param("entrustId") Long entrustId, @Param("contractId") Long contractId, @Param("state") String state);

    /**
     * 根据委托单id更新，报告签署url地址和状态
     * @param entrustId
     * @param signUrl
     * @param state
     */
    @Update("update test_report_record set sign_url=#{signUrl},qys_state=#{state},sealer=#{sealer},seal_time=#{sealTime} where entrustment_id=#{entrustId}")
    void updateUrlAndState(@Param("entrustId") Long entrustId, @Param("signUrl") String signUrl, @Param("state") String state,
                           @Param("sealer") String sealer, @Param("sealTime") Date sealTime);

    /**
     * 根据委托单id更新，报告签署url地址和状态
     * @param entrustId
     * @param signUrl
     * @param state
     */
    @Update("update test_report_record set sign_url=#{signUrl},qys_state=#{state},sealer=#{sealer},seal_time=#{sealTime} where entrust_id=#{entrustId}")
    void updateUrlAndStateZj(@Param("entrustId") Long entrustId, @Param("signUrl") String signUrl, @Param("state") String state,
                           @Param("sealer") String sealer, @Param("sealTime") Date sealTime);

    /**
     * 下载契约锁报告状态更新
     * @param enstustId
     * @param state
     */
    @Update("update test_report_record set qys_state=#{state} where entrustment_id=#{entrustId}")
    void updateState(@Param("entrustId") Long enstustId, @Param("state") String state);

    /**
     * 根据contractId更新状态
     * @param contractId
     * @param state
     */
    @Update("update test_report_record set qys_state=#{state},state='7' where contract_id=#{contractId}")
    void updateFileState(@Param("contractId") Long contractId, @Param("state") String state);

    /**
     * 根据合同id获取委托单id
     * @param contractId
     * @return
     */
    @Select("select entrustment_id from test_report_record where contract_id=#{contractId}")
    Long getEntrustIdByCid(@Param("contractId") Long contractId);

    @Select("select entrustment_id from test_report_record where contract_id=#{contractId}")
    List<Long> getEntrustIdsByCid(@Param("contractId") Long contractId);

    @Select("select entrust_id from test_report_record where contract_id=#{contractId}")
    List<Long> getzJEntrustIdsByCid(@Param("contractId") Long contractId);

    @Select("select id from test_report_record where contract_id=#{contractId}")
    Long getIdByCid(@Param("contractId") Long contractId);

    @Select("select id from test_report_record where contract_id=#{contractId}")
    List<Long> getIdsByCid(@Param("contractId") Long contractId);

    @Select("select qys_docment_id,contract_id,sign_url,qys_state from test_report_record where entrustment_id=#{entrustId}")
    List<ReportRecordEntity> selectMessageByEntrustId(@Param("entrustId") long entrustId);

    /**
     * 查询所有报告
     * @param entrustId
     * @return
     */
    List<String> getAllReportCode(Long entrustId);
    List<Long> getAllReportId(Long entrustId);
    List<Long> getAllMiddleReportId(Long entrustId);

    /**
     * 查询报告与检测项关系
     * @param entrustId
     * @return
     */
    List<LabelValueVo> getCheckReportRel(Long entrustId);

    /**
     * 统计报告的状态
     */
    Integer selectCount(Integer state);

    List<ReportDetailListVo> reportList(ReportDetailListParamVo paramVo);

    /**
     * 报告查询--不带任务单号
     * @param paramVo
     * @return
     */
    List<ReportDetailListVo> reportList0808(ReportDetailListParamVo paramVo);
    List<ReportDetailListVo> reportListMid0808(ReportDetailListParamVo paramVo);
    List<ReportDetailListVo> reportListTask0808(ReportDetailListParamVo paramVo);
    List<ReportDetailListVo> reportListTaskMid0808(ReportDetailListParamVo paramVo);

    @Update("update test_report_record set category = '电子章' where qys_docment_id = #{documentId}")
    void updateSeal(@Param("documentId") Long documentId);

    @Select("SELECT t1.id record_id FROM test_report_record t1 LEFT JOIN test_task t2 ON t1.entrustment_id = t2.entrustment_id where t2.id = #{taskId}")
    Long getRecordId(@Param("taskId") Long taskId);

    /**
     * 待发出报告列表
     * @param type
     * @param deptIds
     * @return
     */
    List<ReportRecordEntity> getSendListCount(@Param("type") String type, @Param("deptIds")List<Long> deptIds);

    /**
     * 待盖章、已盖章列表查询统计
     * @param ids
     * @return
     */
    List<ReportRecordEntity> getSealListCount(@Param("ids") List<Long> ids);

    @Select("select distinct entrustment_id from test_report_record where  entrustment_id=#{entrustmentId} and type=#{type}")
    Long checkExist(@Param("entrustmentId") Long entrustmentId,@Param("type") String type);

    ReportNodeVo getReportNodeByZjEntrustId(@Param("entrustId") Long entrustmentId);

    @Select("update test_report_record set qys_docment_id=#{docId},qys_state=#{state} where entrust_id=#{entrustId}")
    void updateDocIdAndStateZj(@Param("entrustId") Long entrustId, @Param("docId") Long docId, @Param("state") String state);

    @Select("select qys_docment_id,contract_id,sign_url,qys_state from test_report_record where entrust_id=#{entrustId}")
    List<ReportRecordEntity> selectMessageByZjEntrustId(@Param("entrustId") long entrustId);

    @Update("update test_report_record set contract_id=#{contractId},qys_state=#{state} where entrust_id=#{entrustId}")
    void updateContractIdAndStateZj(@Param("entrustId") Long entrustId, @Param("contractId") Long contractId, @Param("state") String state);

    ReportRecordEntity selectByEntrustIdZj(@Param("entrustId") Long entrustId);

    @Select("select entrust_id from test_report_record where id = #{id}")
    Long getZjEntrustIdById(@Param("id") Long id);

    @Select("select id from test_report_record where entrust_id = #{entrustId}")
    Long getIdByZjEntrustId(@Param("entrustId") Long entrustId);

    /**
     * 查询中间报告数量
     * @param entrustId
     * @return
     */
    Integer getMidReportNum(Long entrustId);

    @Select("select entrust_id from test_report_record where contract_id = #{contractId}")
    Long getEntrustByCid(@Param("contractId") Long contractId);

    @Select("select entrustment_id from test_report_record where id=#{id}")
    Long getTypeById(@Param("id") Long id);

    /**
     * 报告待邮寄报告列表及已发出报告历史列表查询
     * @param search
     * @param reportType
     * @param type
     * @param category
     * @param reportTypeStatus
     * @return
     */
    List<ReportRecordEntity> getSendList20230203Report(@Param("search") String search,
                                                          @Param("reportType") String reportType,
                                                          @Param("type") String type,
                                                          @Param("category") String category,
                                                          @Param("reportTypeStatus")Integer reportTypeStatus,
                                                          @Param("startTime")String startTime,
                                                          @Param("endTime")String endTime);

    @Select("select name from test_init_data where type=20")
    String getInitInfo();

    @Select("SELECT\n" +
            "\tproduct_excel_url AS producTexcelUrl,\n" +
            "\treport_edit_url AS reportEditUrl\n" +
            "FROM\n" +
            "\ttest_entrusted_sample_details_rel\n" +
            "WHERE\n" +
            "\tentrustment_id =#{entrustId}\n" +
            "AND sample_id =#{sampleId}")
    ReportEditReq getUrlByEntrustIdAndSampleId(@Param("entrustId") Long entrustId, @Param("sampleId") Integer sampleId);

    @Select("select type from test_report_record where report_code=#{reportCode}")
    String getTypeByCode(@Param("reportCode") String reportCode);

    @Select("select entrustment_id As entrustmentId,entrust_id As entrustId from test_report_record where report_code=#{reportCode}")
    ReportRecordEntity getEntrust(@Param("reportCode") String reportCode);

    @Update("update test_report_record set report_url=#{substring} where report_code=#{reportCode}")
    void updateUrlByCode(@Param("reportCode") String reportCode, @Param("substring") String substring);

    @Select("select max(end_time) from test_entrusted_sample_checkitem_rel where entrust_id=#{entrustId}")
    java.sql.Date getMaxTime(@Param("entrustId") Long entrustId);

    @Update("update test_report_record set report_complete_time=#{reportCompleteTime},required_completion_time=#{date},sample_name=#{sampleName},task_code=#{taskCode},task_id=#{taskId},combine_time=#{combineTime} where report_code=#{reportCode}")
    void updateTime(@Param("reportCode") String reportCode, @Param("reportCompleteTime") Date reportCompleteTime,
                    @Param("date") Date date,@Param("sampleName") String sampleName,@Param("taskId") Long taskId,@Param("taskCode") String taskCode,@Param("combineTime") Date combineTime);

    @Update("update test_report_record set category = '电子章', qys_docment_id = #{documentId} where report_code=#{reportCode}")
    void updateQysInfo(@Param("reportCode") String reportCode, @Param("documentId") Long documentId);

    @Update("<script>" +
            "UPDATE test_report_record SET contract_id=#{contractId} WHERE report_code IN" +
            " <foreach item='item' collection='list' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    void updateContractIdByCodes(@Param("list") Set<String> list, @Param("contractId") Long contractId);

    @Update("update test_report_record set sign_url=#{signUrl},qys_state=#{state},sealer=#{sealer},seal_time=#{sealTime} where contract_id=#{contractId}")
    void updateUrlAndStateByContractId(@Param("contractId") Long contractId, @Param("signUrl") String signUrl, @Param("state") String state,
                                       @Param("sealer") String sealer, @Param("sealTime") Date sealTime);

    @Select("SELECT distinct\n" +
            "\tt1.entrustment_no As entrustmentNo,\n" +
            "\tt1.entrust_company As entrustCompany,\n" +
            "\tt1.witness_uint As witnessUint,\n" +
            "\tt1.project_name As projectName,\n" +
            "\tt1.project_part As projectPart,\n" +
            "\tt2.inspector,\n" +
            "\tt2.recorder,\n" +
            "\tt2.reviewer,\n" +
            "\tt2.receiver,\n" +
            "\tt4.name As receiverName,\n" +
            "\tt3.report_code As reportCode\n" +
            "FROM\n" +
            "\ttest_entrusted_info t1\n" +
            "LEFT JOIN test_task t2 ON t1.id = t2.entrustment_id\n" +
            "LEFT JOIN test_report_record t3 ON t2.entrustment_id = t3.entrust_id\n" +
            "LEFT JOIN sys_user t4 ON t2.receiver=t4.user_id\n" +
            "WHERE\n" +
            "\tt3.report_code = #{reportCode}\n" +
            "UNION ALL\n" +
            "SELECT distinct\n" +
            "\tt1.entrustment_no,\n" +
            "\tt1.entrust_company,\n" +
            "\tt1.witness_uint,\n" +
            "\tt1.project_name,\n" +
            "\tt1.project_part,\n" +
            "\tt2.inspector,\n" +
            "\tt2.recorder,\n" +
            "\tt2.reviewer,\n" +
            "\tt2.receiver,\n" +
            "\tt4.name,\n" +
            "\tt3.report_code\n" +
            "FROM\n" +
            "\ttest_entrusted_info t1\n" +
            "LEFT JOIN test_task t2 ON t1.id = t2.entrustment_id\n" +
            "LEFT JOIN test_report_record t3 ON t2.entrustment_id = t3.entrustment_id\n" +
            "LEFT JOIN sys_user t4 ON t2.receiver=t4.user_id\n" +
            "WHERE\n" +
            "\tt3.report_code = #{reportCode}\n")
    List<ApproveInfo> approveInfo(@Param("reportCode") String reportCode);

    @Select("SELECT distinct\n" +
            "\tt1.entrustment_no As entrustmentNo,\n" +
            "\tt1.entrust_company As entrustCompany,\n" +
            "\tt1.witness_uint As witnessUint,\n" +
            "\tt1.project_name As projectName,\n" +
            "\tt1.project_part As projectPart,\n" +
            "\tt3.inspector,\n" +
            "\tt3.verifyer,\n" +
            "\tt3.issuer,\n" +
            "\tt4.name As receiverName,\n" +
            "\tt3.report_code As reportCode\n" +
            "FROM\n" +
            "\ttest_entrusted_info t1\n" +
            "LEFT JOIN test_task t2 ON t1.id = t2.entrustment_id\n" +
            "LEFT JOIN test_report_record t3 ON t2.entrustment_id = t3.entrust_id\n" +
            "LEFT JOIN sys_user t4 ON t2.receiver=t4.user_id\n" +
            "WHERE\n" +
            "\tt3.id = #{id}\n" +
            "UNION ALL\n" +
            "SELECT distinct\n" +
            "\tt1.entrustment_no,\n" +
            "\tt1.entrust_company,\n" +
            "\tt1.witness_uint,\n" +
            "\tt1.project_name,\n" +
            "\tt1.project_part,\n" +
            "\tt3.inspector,\n" +
            "\tt3.verifyer,\n" +
            "\tt3.issuer,\n" +
            "\tt4.name,\n" +
            "\tt3.report_code\n" +
            "FROM\n" +
            "\ttest_entrusted_info t1\n" +
            "LEFT JOIN test_task t2 ON t1.id = t2.entrustment_id\n" +
            "LEFT JOIN test_report_record t3 ON t2.entrustment_id = t3.entrustment_id\n" +
            "LEFT JOIN sys_user t4 ON t2.receiver=t4.user_id\n" +
            "WHERE\n" +
            "\tt3.id = #{id}\n")
    List<ApproveInfo> approveInfoById(@Param("id") Long id);

    @Select("select seal_type from test_report_record where id=#{id}")
    String getsealsById(@Param("id") Long id);

    @Update("update test_report_record set qys_state='1',state='6',qys_docment_id=null,contract_id=null,sign_url=null where id=#{id}")
    void sealRevoke(@Param("id") Long id);

    @Select("select operate_type from test_report_record where report_code=#{reportCode}")
    Integer getOperateTypeByCode(@Param("reportCode") String reportCode);

    @Select("select report_code from test_report_record where entrustment_id=#{entrustId}")
    String getCodeByEntrustId(@Param("entrustId") Long entrustId);

    @Select("select report_code from test_report_record where entrust_id=#{entrustId}")
    String getMCodeByEntrustId(@Param("entrustId") Long entrustId);

    List<String> getSealTypeByIds(@Param("ids") List<Long> ids);

    @Select("select id from test_report_record where entrustment_id=#{id}")
    Long getIdByFId(@Param("id") Long id);

    @Select("select id from test_report_record where entrust_id=#{id}")
    Long getIdByMId(@Param("id") Long id);

    @Select("\n" +
            "SELECT\n" +
            "\t\t\treport_code\n" +
            "\n" +
            "\t\tFROM\n" +
            "\t\t\ttest_report_record\n" +
            "\t\tWHERE\n" +
            "\t\t\treport_code IS NOT NULL\n" +
            "\t\tAND (state = 1 OR state =2)\n" +
            "\t\tAND report_type IS NOT NULL\n" +
            "ORDER BY  report_code")
    List<String> getAllUpdateCode();

    @Update("update test_report_record set verifyer_time=#{shTime},issuer_time=#{qfTime},verifyer=#{shr},verifyer_id=#{shrId},issuer=#{qhr},issuer_id=#{qhrId},state='8' where report_code = #{reportCode}")
    void updateShAndQfByReportCode(@Param("reportCode") String reportCode, @Param("shr") String shr, @Param("shrId") Long shrId,
                                   @Param("qhr") String qhr, @Param("qhrId") Long qhrId, @Param("shTime") Date shTime, @Param("qfTime") Date qfTime);

    @Select("SELECT DISTINCT\n" +
            "\tt3.url \n" +
            "FROM\n" +
            "\ttest_sample t1\n" +
            "\tLEFT JOIN product_report_original_rel t2 ON t1.product_id = t2.product_id\n" +
            "\tLEFT JOIN test_report_original_template t3 ON t2.report_original_id = t3.id \n" +
            "WHERE\n" +
            "\tt1.id= #{sampleId}")
    String getUrlBySampleId(@Param("sampleId") Integer sampleId);

    @Update("update test_report_record set state=#{state},qys_state='1' where report_code = #{reportCode}")
    void updateStateByCode(@Param("reportCode") String reportCode,@Param("state") String state);
}

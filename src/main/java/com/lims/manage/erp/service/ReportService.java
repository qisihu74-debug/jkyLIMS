package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.ConclusionEntity;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.QiYueSuoSeaLBean;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportResBean;
import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.entity.SealEntity;
import com.lims.manage.erp.entity.TestSampleMixInfoEntity;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.vo.ReportDetailListParamVo;
import com.lims.manage.erp.vo.ReportDetailVo;
import com.lims.manage.erp.vo.ReportHistoryDetailVo;
import com.lims.manage.erp.vo.ReportListVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import com.lims.manage.erp.vo.ReportProductRelVo;
import com.lims.manage.erp.vo.ReportSampleDetailVo;
import io.minio.MinioClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Administrator
 */
public interface ReportService {
    /**
     * 查询可制作报告列表
     *
     * @return
     */
    List<ReportListVo> getReportList();

    /**
     * 查询可制作报告列表--科室权限
     * @return
     */
//    List<ReportListVo> makeReport();
    PageInfo makeReport(Integer pageNum,Integer pageSize,String search);

    /**
     * 报告下载列表--科室权限
     * @return
     */
    PageInfo reportDownloadList(Integer pageNum,Integer pageSize,String search);

    /**
     * 提交审批
     * @param id
     * @param name 报告提交申请人
     * @return
     */
    Boolean getReportSubmit(Long id,String name);

    /**
     * 提交审批—— 二次
     * @return
     */
    Boolean getReportSubmit_two(ReportRecordEntity reportRecordEntity);

    /**
     * 查询历史记录
     * @param search
     * @return
     */
    PageInfo getReportList_history(String search,Integer pageNum,Integer pageSize);

    PageInfo reportDownloadListHistory(String search,Integer pageNum,Integer pageSize);

    /**
     * 获取报告详情
     * @param recordId
     * @return
     */
    ReportSampleDetailVo getReportList_history_details(Long recordId,Long taskId);

    /**
     * 查询委托单--报告制作详情
     *
     * @param id
     * @return
     */
    ReportDetailVo getReportDetail1(Long id);

    /**
     * 查询委托单--报告制作详情--科室
     *
     * @param taskId
     * @return
     */
    ReportDetailVo getReportDetail(Long taskId);

    ReportDetailVo getReportDetail0620(Long taskId);

    /**
     * 根据委托单ID查询所有检测项
     * @param id
     * @return
     */
    ReportHistoryDetailVo getDetailCheckItem(Long id);

    /**
     * 报告邮寄编辑数据回显
     * @param id
     * @return
     */
    ReportRecordEntity getDetail(Long id);

    /**
     * 报告邮寄编辑
     * @param reportRecordEntity
     * @return
     */
    Boolean saveMessage(ReportRecordEntity reportRecordEntity);

    Boolean preserve(ReportPreserveVo vo);

    /**
     * 保存中间报告
     * @param vo
     * @return
     */
    Boolean middleReportPreserve(ReportPreserveVo vo);

    /**
     * 中间报告修改保存接口
     * @param vo
     * @return
     */
    Boolean middleReportUpdate(ReportPreserveVo vo);

    /**
     * 待盖章和历史盖章列表查询
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo sealList(String search, Integer pageNum, Integer pageSize,String reportType,String state,Integer reportTypeStatus);



    /**
     * 获取要盖的印章
     * @param entrustId
     * @return
     */
    Boolean seal(Long entrustId,String title,String fileType);

    /**
     * 根据报告编号获取报告模板地址、印章地址
     *
     * @param reportCode
     * @return
     */
    ReportRecordEntity getUrlByCode(String reportCode);

    /**
     * 根据报告编号获取委托单id
     *
     * @param reportCode
     * @return
     */
    Long getEntrustIdByCode(String reportCode);

    /**
     * 查询报告的详细信息
     *
     * @param reportCode
     * @return
     */
    List<ReportRecordDetailEntity> getReportDetailByCode(String reportCode);

    /**
     * 查询产品下的报告
     *
     * @param productId
     * @return
     */
    List<ReportTemplateEntity> getReportTemplateListOld(String productId);
    List<ReportTemplateEntity> getReportTemplateList(Long id);
    List<ReportTemplateEntity> getMiddleReportTemplateList(Long id);

    /**
     * 查询每组样品的报告信息
     * @param id
     * @return
     */
    List<ReportProductRelVo> getReportTemplateList0706(Long id,Long recordId);

    /**
     * 查询存在委托单报告信息
     * @param entrustId
     * @return
     */
    ReportRecordEntity selectByEntrustId(Long entrustId);

    /**
     * 根据recordId获取检测项信息
     *
     * @param recordId
     * @return
     */
    List<ReportRecordDetailEntity> getCheckInfoByRecordId(Long recordId);

    /**
     * 待邮寄报告列表及已发出报告历史列表查询
     * @param search
     * @param reportType
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo getSendList(String search, String reportType, Integer pageNum, Integer pageSize,String type,Integer reportTypeStatus);
    PageInfo getSendList0623(String search, String reportType, Integer pageNum, Integer pageSize,String type,String category,Integer reportTypeStatus);

    Boolean isApprove(Long id);

    /**
     * 查询判定依据
     * @param id
     * @return
     */
    String getJudgeBasis(Long id);

    /**
     * 查询检测依据
     * @param id
     * @return
     */
    String getCheckBasis(Long id);

    /**
     * 查询设备
     * @param id
     * @return
     */
    String getEquipment(Long id);

    int updateReportUrl(Long id,String url,String code);

    /**
     * 根据检测项ID下载报告
     * @param client
     * @param code
     * @param id
     * @return
     * @throws Exception
     */
    String downLoadByCheckItemId(MinioClient client, String code, Long id) throws Exception;

    /**
     * 创建合同
     * @param reqBean
     * @return
     */
    QiYueSuoResponse createbycategory(QiYueSuoReqBean reqBean);

    /**
     * 合同报告签署url获取
     * @param reqBean
     * @return
     */
    QiYueSuoResponse signurl(QiYueSuoSeaLBean reqBean);

    /**
     * 下载契约锁报告文档
     * @param contractId
     * @param name
     * @param contact
     */
    byte[] downloadQysFile(Long entrustId, Long contractId, String name, String contact);

    /**
     * 契约锁报告签署完成时调用的回调函数
     * @param contractId
     */
    void callback(Long contractId);

    /**
     * 获取契约锁部门列表
     * @param tenantType
     * @param companyName
     * @return
     */
    QiYueSuoResponse deptList(String tenantType, String companyName);

    /**
     * 获取部门在契约锁的印章列表
     * @param category
     * @param companyName
     * @return
     */
    QiYueSuoResponse sealListOfQys(String category, String companyName, String sealType);

    /**
     * 报告生成--编辑按钮--科室
     * @param taskId
     * @return
     */
    Object getQuota(Long taskId);

    /**
     * 报告上传
     * @param reportCode
     * @param file
     * @param verifyer
     * @param issuer
     * @return
     */
    Boolean uploadReport(String reportCode, MultipartFile file, String verifyer, String issuer,Long verifyerId,
                         Long issuerId,String code,String conclusion,String additional,String mixInfo,String type,String inspector);

    /**
     * 根据委托单id查询报告编号和名称
     * @param entrustId
     * @return
     */
    ReportRecordEntity getDetailByEntrustId(Long entrustId);

    /**
     * 报告提交审批
     * @param client
     * @param list
     * @param id
     * @return
     */
    ReportResBean submitDownLoad(MinioClient client, List<ConclusionEntity> list, Long id);

    /**
     * 获取委托单下各个报告模板所需的检测结论和附加声明的文案
     * @param entrustId
     * @return
     */
    List<ConclusionEntity> getResut(Long entrustId,Integer reportType);

    /**
     * 查询配合比检测信息
     * @param entrustId
     * @return
     */
    TestSampleMixInfoEntity getMixSampleInfo(Long entrustId);

    /**
     * 配合比报告合并，下载，预览
     * @param client
     * @param list
     * @param id
     * @return
     */
    ReportResBean submitDownLoadMix(MinioClient client, List<ConclusionEntity> list, Long id,TestSampleMixInfoEntity mixInfoEntity);

    String reportUrl(Long entrustId);

    String insertPicToPdf(String url, Long entrustId,String inspector) throws Exception;

    /**
     * 报告查询列表
     * @param paramVo
     * @return
     */
    PageInfo reportList(ReportDetailListParamVo paramVo);

    /**
     * 中间报告列表
     * @param pageNum
     * @param pageSize
     * @param search
     * @return
     */
    PageInfo middleReportList(Integer pageNum,Integer pageSize,Integer state,String search);

    /**
     * 查询可制作中间报告的检测项详情
     * @param taskId
     * @return
     */
    ReportDetailVo getMiddleReportDetail(Integer taskFlowId,Long taskId);

    ReportDetailVo middleReportEdit(Integer taskFlowId,Long taskId,Long recordId);

    String getUrlById(Long id);

    Boolean category(SealEntity ids);

    /**
     * 撤回報告
     * @param recordId
     * @param taskId
     * @return
     */
    Boolean withdrawReport(Long recordId,Long taskId);

    Long getEntrustIdById(Long id);

    PageInfo<ReportRecordEntity> historyList(String reportCode, String reportType, String sealType, Integer pageNum, Integer pageSize,Long startDate,Long endDate);

    List<TestTeam> getSealer();

    byte[] exportRecords(String reportCode, String reportType, String sealType, Long startDate, Long endDate);

    List<String> inspectorList(String search);

    void updateInspector(String reportCode, String inspector);

    ReportRecordEntity getDetailByEntrustIdZj(Long entrustId);

}

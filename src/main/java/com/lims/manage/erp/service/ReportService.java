package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.ConclusionEntity;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.QiYueSuoSeaLBean;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.vo.*;
import io.minio.MinioClient;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

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
     * @param id
     * @return
     */
    ReportSampleDetailVo getReportList_history_details(Long id);

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

    /**
     * 保存信息
     *
     * @param vo
     * @return
     */
    Boolean preserve1(ReportPreserveVo vo);

    Boolean preserve(ReportPreserveVo vo);

    /**
     * 待盖章和历史盖章列表查询
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo sealList(String search, Integer pageNum, Integer pageSize,String reportType,String state);



    /**
     * 获取要盖的印章
     * @param entrustId
     * @return
     */
    Boolean seal(Long entrustId,String title,String fileType);

    /**
     * 报告预览
     *
     * @param reportCode
     * @param detailEntityList
     * @param object
     * @param sealUrls
     * @return
     */
    XWPFDocument preview(String reportCode,List<ReportRecordDetailEntity> detailEntityList, EntrustAddVo detail, InputStream object, String[] sealUrls);

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
    PageInfo getSendList(String search, String reportType, Integer pageNum, Integer pageSize,String type);

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
     * 报告下载
     * @return
     * @param client
     * @param code
     * @param id
     */
    String downLoad(MinioClient client, String code, Long id) throws Exception;

    /**
     * 下载报告--根据检测项ID存放数据
     * @param client
     * @param id
     * @return
     * @throws Exception
     */
    String downLoad2(MinioClient client, Long id) throws Exception;

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
                         Long issuerId,String code,String conclusion,String additional);

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
    String submitDownLoad(MinioClient client, List<ConclusionEntity> list, Long id);

    /**
     * 获取委托单下各个报告模板所需的检测结论和附加声明的文案
     * @param entrustId
     * @return
     */
    List<ConclusionEntity> getResut(Long entrustId);

    /**
     * 根据委托单id获取报告检测人、审核人、批准人
     * @param entrustId
     * @return
     */
    ReportRecordEntity getUserInfo(Long entrustId);
}

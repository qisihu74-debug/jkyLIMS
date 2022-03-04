package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.QiYueSuoSeaLBean;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.ReportDetailVo;
import com.lims.manage.erp.vo.ReportListVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import com.lims.manage.erp.vo.ReportSampleDetailVo;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    List<ReportListVo> makeReport();

    /**
     * 提交审批
     * @param id
     * @param name 报告提交申请人
     * @return
     */
    Boolean getReportSubmit(Long id,String name);

    /**
     * 查询历史记录
     * @param search
     * @return
     */
    List<ReportListVo> getReportList_history(String search);

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
     * @param id
     * @return
     */
    ReportDetailVo getReportDetail(Long taskId);

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
    Boolean preserve(ReportPreserveVo vo);

    /**
     * 待盖章和历史盖章列表查询
     *
     * @param type
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo sealList(String type, String search, Integer pageNum, Integer pageSize,String reportType,String state);



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
    List<ReportTemplateEntity> getReportTemplateList(String productId);

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
}

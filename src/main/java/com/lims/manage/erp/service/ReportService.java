package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.vo.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ReportService {
    /**
     * 查询可制作报告列表
     *
     * @return
     */
    List<ReportListVo> getReportList();

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
    ReportDetailVo getReportDetail(Long id);

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
    PageInfo sealList(String type, String search, Integer pageNum, Integer pageSize);



    /**
     * 获取要盖的印章
     *
     * @param list
     * @param id
     * @return
     */
    Boolean seal(List<String> list, Long id);

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
}

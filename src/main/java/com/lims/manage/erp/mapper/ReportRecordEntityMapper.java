package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportRecordEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface ReportRecordEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReportRecordEntity record);

    int insertSelective(ReportRecordEntity record);

    ReportRecordEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReportRecordEntity record);

    int updateByPrimaryKey(ReportRecordEntity record);

    /**
     * 待盖章、已盖章列表查询
     * @param search
     * @param type
     * @return
     */
    List<ReportRecordEntity> getSealList(@Param("type") String type, @Param("search") String search);


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
     * 更新报告状态
     * @param record
     * @return
     */
    int updateByEntrustIdSelective(ReportRecordEntity record);

    /**
     * 待邮寄报告列表及已发出报告历史列表查询
     * @param search
     * @param reportType
     * @return
     */
    List<ReportRecordEntity> getSendList(@Param("search") String search, @Param("reportType") String reportType,@Param("type") String type);

    String isApprove(Long id);

    /**
     * 遍历报告信息 获取 委托id 和 状态
     * @return
     */
    List<ReportRecordEntity> getReportList();
}
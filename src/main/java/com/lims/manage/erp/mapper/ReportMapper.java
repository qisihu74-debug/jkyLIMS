package com.lims.manage.erp.mapper;

import com.lims.manage.erp.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

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
    List<ReportListVo> getReportList2(@Param("deptIds") List<Long> deptIds);

    List<ReportListVo> reportDownloadList(@Param("deptIds") List<Long> deptIds);

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
}
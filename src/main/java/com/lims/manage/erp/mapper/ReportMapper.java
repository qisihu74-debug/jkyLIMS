package com.lims.manage.erp.mapper;

import com.lims.manage.erp.vo.ReportCheckItemDetailVo;
import com.lims.manage.erp.vo.ReportDetailVo;
import com.lims.manage.erp.vo.ReportListVo;
import com.lims.manage.erp.vo.ReportSampleDetailVo;
import org.apache.ibatis.annotations.Mapper;
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
     * 查询委托单--报告制作详情
     *
     * @param id
     * @return
     */
    ReportDetailVo getReportDetail(Long id);

    List<ReportListVo> getReportList_history(ReportListVo reportListVo);

    /**
     * 获取历史详情
     * @param id
     * @return
     */
    ReportDetailVo getReportDetailHistory(Long id);

    List<ReportSampleDetailVo> getReportHeadDetails(Long id);

    List<ReportCheckItemDetailVo> getReportCheckItemList(Long id);

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

    int updateReportUrl(Long id,String url);
}
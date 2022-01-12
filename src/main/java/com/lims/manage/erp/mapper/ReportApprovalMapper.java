package com.lims.manage.erp.mapper;

import com.lims.manage.erp.vo.ReportApprovalVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/1/11 16:57
 */
@Component
@Mapper
public interface ReportApprovalMapper {

    List<ReportApprovalVo> getReportApprovalList(@Param("search")String search);

    ReportApprovalVo getReportApprovalDetail(Long id);

    int updateReportApprovalDetail(ReportApprovalVo reportApprovalVo);

    String getUserName(Long userId);


}

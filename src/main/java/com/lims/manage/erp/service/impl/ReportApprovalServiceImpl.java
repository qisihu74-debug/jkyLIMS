package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.service.ReportApprovalService;
import com.lims.manage.erp.vo.ReportApprovalVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/1/11 16:56
 */
@Service
public class ReportApprovalServiceImpl implements ReportApprovalService {

    @Autowired
    ReportApprovalMapper reportApprovalMapper;


    @Override
    public List<ReportApprovalVo> getApplyforList(String search, Integer state) {

//        state 报告状态（默认是 0=未抢单 1=已抢单）
        if (state == null||state>2) {
            state = 0;
        } else {
            state = 1;
        }
        List<ReportApprovalVo> list = reportApprovalMapper.getReportApprovalList(search);
        List<ReportApprovalVo> returnData = new ArrayList<>();
        for (ReportApprovalVo reportApprovalVo : list) {
            if (state == 0) {
                if (reportApprovalVo.getVerifyer() == null) {
                    reportApprovalVo.setState(0);
                    returnData.add(reportApprovalVo);
                }
            }
            if (state == 1) {
                if (reportApprovalVo.getVerifyer() != null) {
                    reportApprovalVo.setState(1);
                    returnData.add(reportApprovalVo);
                }
            }
        }
        return returnData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyfor_monad(ReportApprovalVo reportApprovalVo) {

        reportApprovalVo.setVerifyTime(new Date());
       Integer status = reportApprovalMapper.updateReportApprovalDetail(reportApprovalVo);
       if(status==1){
           return true;
       }
        return false;
    }
}

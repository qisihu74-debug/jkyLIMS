package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.service.ReportApprovalService;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import com.lims.manage.erp.vo.ReportApprovalVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
        if (state == null || state > 2 || state == 0) {
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

        reportApprovalVo.setVerifyerTime(new Date());
        Integer status = reportApprovalMapper.updateReportApprovalDetail(reportApprovalVo);
        if (status == 1) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approval_data(ReportApprovalVo reportApprovalVo1) {
//        0是通过 1 是驳回
//         报告状态，0报告被驳回 1指标填写已完成，2指标填写未完成，3.审批已抢单，4.签发待抢单，5.签发已抢单，6已签发，7已盖章，8已邮寄
        Integer state = 0;
        ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
        if (reportApprovalVo1.getState() == 0) {
            //通过 4.签发待抢单
            state = 4;
            ReportApprovalVo data = reportApprovalMapper.getReportApprovalDetail(reportApprovalVo1.getId());
            reportApprovalVo.setVerifyerTime(new Date());
            reportApprovalVo.setVerifyer(data.getVerifyer());
        }
        if (reportApprovalVo1.getState() == 1) {
            // 驳回 对抢单人清空 抢单时间清空 状态改变 如果有备注 选填
            state = 1;
            reportApprovalVo.setVerifyerTime(null);
            reportApprovalVo.setVerifyer(null);
        }
        reportApprovalVo.setId(reportApprovalVo1.getId());
        reportApprovalVo.setReason(reportApprovalVo1.getReason());
        reportApprovalVo.setState(state);
        Integer status = reportApprovalMapper.updateReportApprovalDetail(reportApprovalVo);
        if (status == 1) {
            return true;
        }
        return false;
    }

    @Override
    public List<ReportApprovalVo> applyfor_history(String search) {

        return reportApprovalMapper.getReportApprovalHistory(search);
    }

    @Override
    public TaskDetailInfoVo getDetails(Long id) {
        /**
         * 获取任务单详情
         */
        TaskDetailInfoVo taskDetailInfoVo = reportApprovalMapper.getTaskDetail(id);
        if (taskDetailInfoVo == null) {
            return new TaskDetailInfoVo(id);
        }
        if (taskDetailInfoVo.getEntrustmentId() != null) {
            // 通过委托id 获取样品信息 及以下的 处理。
            List<SampleDetailVo> sampleDetailVoList = reportApprovalMapper.getSampleDetailLis(taskDetailInfoVo.getEntrustmentId());
            for (SampleDetailVo sampleDetailVo : sampleDetailVoList) {
                if (!sampleDetailVo.getCheckItemInfoList().isEmpty()) {
                    for (CheckItemInfoVo checkItemInfoVo : sampleDetailVo.getCheckItemInfoList()) {
                        if (!checkItemInfoVo.getTestInstrumentEntityList().isEmpty()) {
                            String InstrumentName = "";
                            for (TestInstrumentEntity testInstrumentEntity : checkItemInfoVo.getTestInstrumentEntityList()) {
                                InstrumentName += testInstrumentEntity.getName() + "、";
                            }
                            checkItemInfoVo.setIntrusmentName(InstrumentName);
                        }
                    }
                }
            }
            // 处理checkItemId 相同的话 进行保留其中一个。
            if (!sampleDetailVoList.isEmpty()) {
                HashMap<Integer, SampleDetailVo> map = new HashMap<>();
                for (SampleDetailVo data : sampleDetailVoList) {
                    for (CheckItemInfoVo checkItemInfoVo : data.getCheckItemInfoList()) {
                        map.put(checkItemInfoVo.getCheckItemId(), data);
                    }
                }
                Collection<SampleDetailVo> values = map.values();
                Iterator<SampleDetailVo> returnData = values.iterator();
                List<SampleDetailVo> list = new ArrayList<>();
                while (returnData.hasNext()) {
                    list.add(returnData.next());
                }
                taskDetailInfoVo.setSampleDetailList(list);
            }
        }
        return taskDetailInfoVo;
    }

    @Override
    public List<ReportApprovalVo> getVerify_list(String search, Integer state) {
        //        state 报告状态（默认是 0=未抢单 1=已抢单）
        if (state == null || state > 2 || state == 0) {
            //4.签发待抢单，
            state = 4;
        } else {
            //5.签发已抢单
            state = 5;
        }
        return reportApprovalMapper.getVerifyList(search, state);
    }

    @Override
    public Boolean verify_monad(ReportApprovalVo reportApprovalVo) {
        reportApprovalVo.setIssuerTime(new Date());
        Integer status = reportApprovalMapper.updateVerifyMonad(reportApprovalVo);
        if (status == 1) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean verify_data(ReportApprovalVo reportApprovalVo1) {
        //        0是通过 1 是驳回
        // 报告状态，0报告被驳回 1指标填写已完成，2指标填写未完成，3.审批已抢单，4.签发待抢单，5.签发已抢单，6已签发，7已盖章，8已邮寄
        Integer state = 0;
        ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
        reportApprovalVo.setSealType(reportApprovalVo1.getSealType());
        if (reportApprovalVo1.getState() == 0) {
            //通过6已签
            state = 6;
            reportApprovalVo.setIssuerTime(new Date());
            reportApprovalVo.setIssuer(reportApprovalVo1.getIssuer());
        }
        if (reportApprovalVo1.getState() == 1) {
            // 驳回 对报告签发人清空 签发抢单时间清空 状态改变 如果有备注 选填 清除信息 退回上一步
            state = 4;
            reportApprovalVo.setIssuerTime(null);
            reportApprovalVo.setIssuer(null);
            reportApprovalVo.setSealType(null);
        }
        reportApprovalVo.setId(reportApprovalVo1.getId());
        reportApprovalVo.setReason(reportApprovalVo1.getReason());
        reportApprovalVo.setState(state);
        Integer status = reportApprovalMapper.updateVerifyMonad(reportApprovalVo);
        if (status == 1) {
            return true;
        }
        return false;
    }

    @Override
    public List<ReportApprovalVo> verifyHistory(String search) {

        return reportApprovalMapper.getVerifyHistory(search);
    }
}

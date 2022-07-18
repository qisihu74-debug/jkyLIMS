package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TeamTreeStructureEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TeamMapper;
import com.lims.manage.erp.service.ReportApprovalService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: DLC
 * @Date: 2022/1/11 16:56
 */
@Service
public class ReportApprovalServiceImpl implements ReportApprovalService {

    @Autowired
    ReportApprovalMapper reportApprovalMapper;
    @Autowired
    EntrustEntityMapper entrustEntityMapper;
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    private TeamMapper teamMapper;

    @Override
    public PageInfo getApplyforList(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus) {
        PageHelper.startPage(pageNum, pageSize);
        Set<Long> ids = getNextIdsToTeam();
        List<ReportApprovalVo> list = reportApprovalMapper.getReportApprovalList(search,ids,reportTypeStatus);
        PageInfo<ReportApprovalVo> result = new PageInfo<>(list);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyfor_monad(ReportApprovalVo reportApprovalVo) {
        // 抢单不记录 抢单时间
        reportApprovalVo.setVerifyerTime(null);
        Integer status = reportApprovalMapper.updateReportApprovalDetail(reportApprovalVo);
        if (status == 1) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approval_data(ReportApprovalVo reportApprovalVo1) {
//       state 0是通过 1 是驳回
//         报告状态，0报告被驳回 1指标填写已完成，2指标填写未完成，3.审批已抢单，4.签发待抢单，5.签发已抢单，6已签发，7已盖章，8已邮寄
        Integer state = 0;
        ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
        if (reportApprovalVo1.getState() == 0) {
            //通过 4.签发待抢单
            state = 4;
            ReportApprovalVo data = reportApprovalMapper.getReportApprovalDetail(reportApprovalVo1.getId());
            reportApprovalVo.setVerifyerTime(new Date());
            reportApprovalVo.setVerifyer(data.getVerifyer());
            // 根据任务单主键 获取委托单主键 更改委托单状态
            EntrustAddVo entrustAddVo = reportApprovalMapper.getEntrustAddVoDetail(reportApprovalVo1.getId());
            if(entrustAddVo.getState()!=null&&entrustAddVo.getState()<8){
                taskMapper.updateEntrustById(entrustAddVo.getId(),8);
            }
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
    public Boolean approval_data_two(ReportApprovalVo reportApprovalVo1) {
        //       state 0是通过 1 是驳回
//         报告状态，0报告被驳回 1指标填写已完成，2指标填写未完成，3.审批已抢单，4.签发待抢单，5.签发已抢单，6已签发，7已盖章，8已邮寄
        Integer state = 0;
        // 动态修改数据表
        ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
        // 获取当前报告类型。
        ReportApprovalVo reportApprovalVo2 = reportApprovalMapper.getReportApprovalDetail(reportApprovalVo1.getId());
        // reportApprovalVo2.getReportTypeStatus()==1 处理最终报告
        if (reportApprovalVo1.getState() == 0 && reportApprovalVo2.getReportTypeStatus()==0) {
            //通过 4.签发待抢单
            state = 4;
            reportApprovalVo.setVerifyerTime(new Date());
            reportApprovalVo.setVerifyer(reportApprovalVo1.getVerifyer());
            // 根据任务单主键 获取委托单主键 更改委托单状态
            EntrustAddVo entrustAddVo = reportApprovalMapper.getEntrustAddVoDetail(reportApprovalVo1.getId());
            if(entrustAddVo.getState()!=null&&entrustAddVo.getState()<8){
                taskMapper.updateEntrustById(entrustAddVo.getId(),8);
            }
            reportApprovalVo.setState(state);
            reportApprovalVo.setId(reportApprovalVo1.getId());
            reportApprovalMapper.updateReportApprovalDetail(reportApprovalVo);
            return true;
        }
        if (reportApprovalVo1.getState() == 1 &&  reportApprovalVo2.getReportTypeStatus()==0 ) {
            state = 0;
            reportApprovalVo.setVerifyerTime(null);
            reportApprovalVo.setVerifyer(null);
            reportApprovalVo.setVerifyerId(null);
            reportApprovalVo.setState(state);
            reportApprovalVo.setId(reportApprovalVo1.getId());
            reportApprovalVo.setReason(reportApprovalVo1.getReason());

            // 当前委托单信息 返还至制作报告那一步
            EntrustAddVo entrustAddVo = reportApprovalMapper.getEntrustAddVoDetail(reportApprovalVo1.getId());
            // 驳回 state=0  test_report_record表修改到驳回状态 。
            reportApprovalVo.setEntrustmentId(entrustAddVo.getId());
            reportApprovalMapper.updateentrustAndApprovalMonad(reportApprovalVo);
            // 驳回操作 test_task 下 report_complete =2
            taskMapper.updateTestTaskReportComplete(entrustAddVo.getId());
            if(entrustAddVo.getState()!=null){
                taskMapper.updateEntrustById(entrustAddVo.getId(),7);
            }
            return true;
        }
        // reportApprovalVo2.getReportTypeStatus()==0 处理中间报告 不操作委托单id
        if (reportApprovalVo1.getState() == 0 && reportApprovalVo2.getReportTypeStatus()==1) {
            //通过 4.签发待抢单
            state = 4;
            reportApprovalVo.setVerifyerTime(new Date());
            reportApprovalVo.setVerifyer(reportApprovalVo1.getVerifyer());
            reportApprovalVo.setState(state);
            reportApprovalVo.setId(reportApprovalVo1.getId());
            reportApprovalMapper.updateReportApprovalDetail(reportApprovalVo);
            return true;
        }
        if (reportApprovalVo1.getState() == 1 &&  reportApprovalVo2.getReportTypeStatus()==1 ) {
            state = 0;
            reportApprovalVo.setVerifyerTime(null);
            reportApprovalVo.setVerifyer(null);
            reportApprovalVo.setVerifyerId(null);
            reportApprovalVo.setState(state);
            reportApprovalVo.setId(reportApprovalVo1.getId());
            reportApprovalVo.setReason(reportApprovalVo1.getReason());
            reportApprovalMapper.updateentrustAndApprovalMonad(reportApprovalVo);
            return true;
        }
        return false;
    }

    @Override
    public Boolean efficacyApprovalData(Long taskId, Long userId, String name,Integer state) {
        // 通过taskId
        ReportApprovalVo reportData = reportApprovalMapper.getReportApprovalDetail(taskId);
        if(state==1){
            if(userId.equals(reportData.getVerifyerId())){
                return true;
            }
            return false;
        }
        if(state==2){
            if(userId.equals(reportData.getIssuerId())){
                return true;
            }
            return false;
        }
        return null;
    }


    @Override
    public PageInfo applyfor_history(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus) {
        PageHelper.startPage(pageNum, pageSize);
        Set<Long> ids = getNextIdsToTeam();
        List<ReportApprovalVo> list = reportApprovalMapper.getReportApprovalHistory(search,ids,reportTypeStatus);
        PageInfo<ReportApprovalVo> result = new PageInfo<>(list);
        return result;
    }

    /**
     * 报告审批详情  展示检测项数据
     * @param id
     * @return
     */
    @Override
    public TaskDetailInfoVo getDetails(Long id) {
        // 查询报告单详情  要区分 中间报告 还是 最终报告。 reportTypeStatus
        ReportApprovalVo reportApprovalVo = reportApprovalMapper.getReportApprovalDetail(id);
        // reportTypeStatus!=null 并且是中间报告的话
        if(reportApprovalVo.getReportTypeStatus()!=null&&reportApprovalVo.getReportTypeStatus()==1){
            // 审批报告详情 处理中间报告方法
            TaskDetailInfoVo data = approvalMiddleMethod(id);
            return data;
        }
        // 处理最终报告详情。
        TaskDetailInfoVo data = approvalUltimatelyMethod(id);
        return  data;
    }


//    @Override
//    public List<ReportApprovalVo> getVerify_list(String search, Integer state) {
//        //        state 报告状态（默认是 0=未抢单 1=已抢单）
//        if (state == null || state > 2 || state == 0) {
//            //4.签发待抢单，
//            state = 4;
//        } else {
//            //5.签发已抢单
//            state = 5;
//        }
//        return reportApprovalMapper.getVerifyList(search, state);
//    }

    @Override
    public PageInfo getVerify_list(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportApprovalVo> list = reportApprovalMapper.getVerifyList(search,getNextIdsToTeam(),reportTypeStatus);
        PageInfo<ReportApprovalVo> result = new PageInfo<>(list);
        return result;
    }


    @Override
    public Boolean verify_monad(ReportApprovalVo reportApprovalVo) {
        // 签发抢单 不记录签发时间
        reportApprovalVo.setIssuerTime(null);
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
        // 处理印章数组 解析成 字符串,
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0;i<reportApprovalVo1.getSealTypeArray().length;i++){
            stringBuilder.append(reportApprovalVo1.getSealTypeArray()[i]);
            stringBuilder.append(",");
        }
        reportApprovalVo.setSealType(stringBuilder.deleteCharAt(stringBuilder.toString().length()-1).toString());
        if (reportApprovalVo1.getState() == 0) {
            //通过6已签
            state = 6;
            reportApprovalVo.setIssuerTime(new Date());
            reportApprovalVo.setIssuer(reportApprovalVo1.getIssuer());

            // 根据任务单主键 获取委托单主键 更改委托单状态
            EntrustAddVo entrustAddVo = reportApprovalMapper.getEntrustAddVoDetail(reportApprovalVo1.getId());
            if(entrustAddVo.getState()!=null&&entrustAddVo.getState()<9){
                taskMapper.updateEntrustById(entrustAddVo.getId(),9);
            }

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
    public Boolean verify_data_two(ReportApprovalVo reportApprovalVo1) {
        //      state  0是通过 1 是驳回
        // 报告状态，0报告被驳回 1指标填写已完成，2指标填写未完成，3.审批已抢单，4.签发待抢单，5.签发已抢单，6已签发，7已盖章，8已邮寄
        Integer state = 0;
        ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
        // 获取报告状态 是中间报告 还是 最终报告
        ReportApprovalVo reportApprovalVo2  = reportApprovalMapper.getReportApprovalDetail(reportApprovalVo1.getId());
        if (reportApprovalVo1.getState() == 0 && reportApprovalVo2.getReportTypeStatus()==0) {
            //通过6已签
            state = 6;
            reportApprovalVo.setIssuerTime(new Date());
            reportApprovalVo.setIssuer(reportApprovalVo1.getIssuer());
            // 印章数据 转成字符串
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0;i<reportApprovalVo1.getSealTypeArray().length;i++){
                stringBuilder.append(reportApprovalVo1.getSealTypeArray()[i]);
                stringBuilder.append(",");
            }
            reportApprovalVo.setSealType(stringBuilder.deleteCharAt(stringBuilder.toString().length()-1).toString());

            // 根据任务单主键 获取委托单主键 更改委托单状态
            EntrustAddVo entrustAddVo = reportApprovalMapper.getEntrustAddVoDetail(reportApprovalVo1.getId());
            if(entrustAddVo.getState()!=null&&entrustAddVo.getState()<9){
                taskMapper.updateEntrustById(entrustAddVo.getId(),9);
            }
            reportApprovalVo.setId(reportApprovalVo1.getId());
            reportApprovalVo.setReason(reportApprovalVo1.getReason());
            reportApprovalVo.setState(state);
            reportApprovalMapper.updateVerifyMonad(reportApprovalVo);
            return true;
        }
        if (reportApprovalVo1.getState() == 1 && reportApprovalVo2.getReportTypeStatus()==0) {
            // 驳回 对报告签发人清空 签发抢单时间清空 状态改变 如果有备注 选填 清除信息 退回上一步
            state = 0;
            // 签发清除
            reportApprovalVo.setIssuerTime(null);
            reportApprovalVo.setIssuer(null);
            reportApprovalVo.setSealType(null);
            reportApprovalVo.setVerifyerId(null);
            // 审批清除
            reportApprovalVo.setVerifyerTime(null);
            reportApprovalVo.setVerifyer(null);
            reportApprovalVo.setVerifyerId(null);
            reportApprovalVo.setId(reportApprovalVo1.getId());
            reportApprovalVo.setReason(reportApprovalVo1.getReason());
            reportApprovalVo.setState(state);
            // 当前委托单信息 返还至制作报告那一步
            EntrustAddVo entrustAddVo = reportApprovalMapper.getEntrustAddVoDetail(reportApprovalVo1.getId());
            reportApprovalVo.setEntrustmentId(entrustAddVo.getId());
            // 根据委托单id 进行全部修改
            reportApprovalMapper.updateentrustAndApprovalMonad(reportApprovalVo);
            // 驳回操作 test_task 下 report_complete =2
//            taskMapper.updateTestTaskReportComplete(entrustAddVo.getId());
            if(entrustAddVo.getState()!=null){
                taskMapper.updateEntrustById(entrustAddVo.getId(),7);
            }
            return true;
        }
        // 处理中间报告
        if (reportApprovalVo1.getState() == 0 && reportApprovalVo2.getReportTypeStatus()==1) {
            //通过6已签
            state = 6;
            reportApprovalVo.setIssuerTime(new Date());
            reportApprovalVo.setIssuer(reportApprovalVo1.getIssuer());
            // 印章数据 转成字符串
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0;i<reportApprovalVo1.getSealTypeArray().length;i++){
                stringBuilder.append(reportApprovalVo1.getSealTypeArray()[i]);
                stringBuilder.append(",");
            }
            reportApprovalVo.setSealType(stringBuilder.deleteCharAt(stringBuilder.toString().length()-1).toString());
            reportApprovalVo.setId(reportApprovalVo1.getId());
            reportApprovalVo.setReason(reportApprovalVo1.getReason());
            reportApprovalVo.setState(state);
            reportApprovalMapper.updateVerifyMonad(reportApprovalVo);
            return true;
        }
        if (reportApprovalVo1.getState() == 1 && reportApprovalVo2.getReportTypeStatus()==1) {
            // 驳回 对报告签发人清空 签发抢单时间清空 状态改变 如果有备注 选填 清除信息 退回上一步
            state = 0;
            // 签发清除
            reportApprovalVo.setIssuerTime(null);
            reportApprovalVo.setIssuer(null);
            reportApprovalVo.setSealType(null);
            reportApprovalVo.setVerifyerId(null);
            // 审批清除
            reportApprovalVo.setVerifyerTime(null);
            reportApprovalVo.setVerifyer(null);
            reportApprovalVo.setVerifyerId(null);
            reportApprovalVo.setId(reportApprovalVo1.getId());
            reportApprovalVo.setReason(reportApprovalVo1.getReason());
            reportApprovalVo.setState(state);
            //  修改中间报告单至驳回状态
            reportApprovalMapper.updateentrustAndApprovalMonad(reportApprovalVo);
            return true;
        }
        return false;
    }

    @Override
    public PageInfo verifyHistory(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportApprovalVo> list = reportApprovalMapper.getVerifyHistory(search,getNextIdsToTeam(),reportTypeStatus);
        PageInfo<ReportApprovalVo> result = new PageInfo<>(list);
        return result;
    }

    /**
     * 获取当前管理员下级科室下所有的人员id
     * @return
     */
    public Set<Long> getNextIdsToTeam(){
        //获取当前管理账户的id
        Long userId = ShiroUtils.getUserInfo().getUserId();
       /* //根据userId获取当前科室id
        Long teamId = teamMapper.getTeamIdByUid(userId);
        //获取下级科室id
        List<TeamTreeStructureEntity> list = teamMapper.getChirds(teamId);
        List<Long> newList = Lists.newArrayList();
        for (TeamTreeStructureEntity entity:list) {
            newList.add(entity.getId());
        }
        //获取下级科室下的所有人员ids（不包含本科室除自身外其它人员）
        List<Long> uids = teamMapper.getUsersByTeams(newList);
        //获取本科室的人员id
        List<Long> users = teamMapper.getUsersByTeamId(teamId);
        Set<Long> ids = new HashSet<>();
        uids.removeAll(users);
        ids.addAll(uids);
        ids.add(userId);*/
        Set<Long> ids = new HashSet<>();
        ids.add(userId);
        return ids;
    }

    /**
     * 审批报告详情 处理中间报告方法
     */
    public TaskDetailInfoVo approvalMiddleMethod(Long id){
    // 中间报告详情表 已经存储检测项 详情。 比对委托单检测项后 取值。
        List<CheckItemInfoVo> CheckItemList  = reportApprovalMapper.getCheckItemInfoVoList(id);
        Map<Integer,String> map = new HashMap<>();
        if(!CheckItemList.isEmpty()){
            for(int i=0; i<CheckItemList.size();i++){
                CheckItemInfoVo checkItemInfoVo  =  CheckItemList.get(i);
                map.put(checkItemInfoVo.getCheckItemId(),checkItemInfoVo.getCheckItemName());
            }
        }
        // 获取任务单详情。----中间报告
        TaskDetailInfoVo taskDetailInfoVo = reportApprovalMapper.getTaskDetailInterimReport(id);
        // 获取印章数据以 数组形式呈现
        String[] sealTypeArray = new String[3];
        // 1、优先考虑报告印章 呈现
        if(taskDetailInfoVo.getSealType()!=null&&!taskDetailInfoVo.getSealType().equals("")){
            sealTypeArray = taskDetailInfoVo.getSealType().split(",");
        }else {
            // 2、考虑委托单印章 呈现
            if(taskDetailInfoVo.getSealTypeTicket()!=null&&!taskDetailInfoVo.getSealTypeTicket().equals("")){
                sealTypeArray = taskDetailInfoVo.getSealTypeTicket().split(",");
            }
        }
        taskDetailInfoVo.setSealTypeArray(sealTypeArray);
        if (taskDetailInfoVo == null) {
            return new TaskDetailInfoVo(id);
        }
        // 样品展示  样品的检测项信息展示
        if (taskDetailInfoVo.getEntrustmentId() != null) {
            // 通过委托id 获取样品信息 及以下的 处理。
            List<SampleDetailVo> sampleDetailVoList = reportApprovalMapper.getSampleDetailList(taskDetailInfoVo.getEntrustmentId());
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
            //中间报告需要 比较委托单检测项 与报告单检测项。最终以报告单检测项为准。
            if(!sampleDetailVoList.isEmpty()){
                for(SampleDetailVo sampleDetailVo:sampleDetailVoList){
                    if(!sampleDetailVo.getCheckItemInfoList().isEmpty()){
                        Iterator<CheckItemInfoVo> it = sampleDetailVo.getCheckItemInfoList().iterator();
                        while (it.hasNext()){
                            CheckItemInfoVo checkItemInfoVo  = it.next();
                            if(map.get(checkItemInfoVo.getCheckItemId())==null){
                                it.remove();
                            }
                        }
                    }
                }
            }
            taskDetailInfoVo.setSampleDetailList(sampleDetailVoList);
        }
        return taskDetailInfoVo;
    }

    /**
     * 审批报告详情 处理 最终报告方法
     * @param id
     * @return
     */
    public TaskDetailInfoVo approvalUltimatelyMethod(Long id){
        /**
         * 获取任务单详情
         */
        TaskDetailInfoVo taskDetailInfoVo = reportApprovalMapper.getTaskDetail(id);
        // 获取印章数据以 数组形式呈现
        String[] sealTypeArray = new String[3];
        // 1、优先考虑报告印章 呈现
        if(taskDetailInfoVo.getSealType()!=null&&!taskDetailInfoVo.getSealType().equals("")){
            sealTypeArray = taskDetailInfoVo.getSealType().split(",");
        }else {
            // 2、考虑委托单印章 呈现
            if(taskDetailInfoVo.getSealTypeTicket()!=null&&!taskDetailInfoVo.getSealTypeTicket().equals("")){
                sealTypeArray = taskDetailInfoVo.getSealTypeTicket().split(",");
            }
        }
        // 判断印章数组内容
        if(StringUtils.isEmpty(sealTypeArray[0])){
            taskDetailInfoVo.setSealTypeArray(new String[0]);
        }
        else {
            taskDetailInfoVo.setSealTypeArray(sealTypeArray);
        }
        if (taskDetailInfoVo == null) {
            return new TaskDetailInfoVo(id);
        }
        // 样品展示  样品的检测项信息展示
        if (taskDetailInfoVo.getEntrustmentId() != null) {
            // 通过委托id 获取样品信息 及以下的 处理。
            List<SampleDetailVo> sampleDetailVoList = reportApprovalMapper.getSampleDetailList(taskDetailInfoVo.getEntrustmentId());
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
            taskDetailInfoVo.setSampleDetailList(sampleDetailVoList);
        }
        return taskDetailInfoVo;
    }
}

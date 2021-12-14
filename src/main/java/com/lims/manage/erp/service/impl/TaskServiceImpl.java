package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TaskTestTeamEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.vo.*;
import com.lims.manage.erp.vo.*;
import javafx.scene.control.TableRow;
import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;

    @Override
    public TaskDetailInfoVo getTaskDetailInfo(Long taskId) {
        return taskMapper.getTaskDetailInfo(taskId);
    }

    @Override
    public List<TaskListVo> getTaskList(TaskListParamVo paramVo) {
        return taskMapper.getTaskList(paramVo);
    }

    @Override
    public List<ReceiveSampleListVo> getSampleList(TaskListParamVo paramVo) {
        String receiveTime = paramVo.getReceiveTime();
        if (receiveTime != null) {
            String[] split = receiveTime.split("~");
            paramVo.setBeginDate(split[0]);
            paramVo.setEndDate(split[1]);
        }
        return taskMapper.getSampleList(paramVo);
    }

    @Override
    public int receiveSample(ReceiveSampleParamVo paramVo) {
        paramVo.setState(2);
        return taskMapper.updateSampler(paramVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postGrabASingle(TaskTestEntity taskTestEntity) {
        // 抢单
        taskTestEntity.setState(1);
        // 根据角色查询团队名
        if (taskTestEntity.getReceiver() != null) {
            // 任务编号 团队名称+编号=任务编号
            TaskTestTeamEntity dataTeam = taskMapper.selectTeamCode(Long.parseLong(taskTestEntity.getReceiver()));
            if (dataTeam == null) {
                return false;
            }
            String strDate = taskTestEntity.getCode();
            String str1 = strDate.substring(0, 4);
            String str2 = strDate.substring(strDate.length() - 3);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str1);
            stringBuilder.append("-");
            stringBuilder.append(str2);
            taskTestEntity.setTaskCode(dataTeam.getCode() + stringBuilder);
            taskTestEntity.setTeamId(String.valueOf(dataTeam.getId()));
            // 抢单时间
            java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
            taskTestEntity.setReceiveTime(currentDate);
            taskMapper.updateTestTask(taskTestEntity);
            return true;
        }
        return false;
    }

    @Override
    public List<LabelValueTeamVo> getTeamUserName(Long UserLong) {
        TaskTestTeamEntity dataTeam = taskMapper.selectTeamCode(UserLong);
        if (dataTeam != null) {
            return taskMapper.selectTeamList(dataTeam.getId());
        }
        return null;
    }

    @Override
    public Boolean getJudgmentTaskList(Long id) {
        if (taskMapper.getJudgmentTaskList(id) == 0) {
            return true;
        }
        return false;
    }

    @SneakyThrows
    @Override
    public XWPFDocument downloadEntrust(TaskDetailInfoVo taskDetailInfoVo, InputStream object) {
        XWPFDocument doc = null;
        try {
            doc = new XWPFDocument(object);
            List<XWPFTable> tables = doc.getTables();
            List<XWPFTableRow> rows;
            XWPFTable table = tables.get(0);
            //表格属性
            CTTblPr pr = table.getCTTbl().getTblPr();
            //表头部分

            // 遍历 样品数据
            List<SampleDetailVo> sampleDetailList = taskDetailInfoVo.getSampleDetailList();
            if(sampleDetailList.size()>1){
                for(int i =1;i<sampleDetailList.size();i++){
                }
            }
            //获取表格对应的行
            rows = table.getRows();
            //                表格逐行赋值
            StringBuilder stringBuilder = new StringBuilder();
            for(int i =0;i<sampleDetailList.size();i++){
                SampleDetailVo sampleDetailVo = sampleDetailList.get(i);
                // 补充表格数据 样品名称
                rows.get(i+1).getTableCells().get(0).setText( sampleDetailVo.getSampleName());
                // 规格/等级
                rows.get(i+1).getTableCells().get(1).setText( sampleDetailVo.getSpecs());
                // 批号/编号
                rows.get(i+1).getTableCells().get(2).setText( sampleDetailVo.getBatchNumber());
                // 样品数量
                rows.get(i+1).getTableCells().get(3).setText( sampleDetailVo.getGeneration());
                // 样品产地
                rows.get(i+1).getTableCells().get(4).setText( sampleDetailVo.getSampleOrigin());
                //样品编号
                rows.get(i+1).getTableCells().get(5).setText( sampleDetailVo.getSampleCode());
                // 备注
                rows.get(i+1).getTableCells().get(6).setText( sampleDetailVo.getRemark());
                for(CheckItemInfoVo checkItemInfoVo:sampleDetailVo.getCheckItemInfoList()){
                    stringBuilder.append(checkItemInfoVo.getCheckItemName()+"("+checkItemInfoVo.getStandardName()+")"+",");
                }
            }
            // 提供资料
            rows.get(6).getTableCells().get(0).setText(taskDetailInfoVo.getPresentInformation());
            // 取样方式
            rows.get(7).getTableCells().get(1).setText(taskDetailInfoVo.getSamplingMethod());
            // 检验目的
            rows.get(7).getTableCells().get(3).setText(taskDetailInfoVo.getCheckPurpose());
            // 产品标准
            rows.get(7).getTableCells().get(5).setText("待补充");
            // 检测项目及检验依据
            rows.get(8).getTableCells().get(1).setText(stringBuilder.toString());
            // 要求检验完成日期
            rows.get(9).getTableCells().get(1).setText(taskDetailInfoVo.getRequiredCompletionTime());
            // 本单产值
            rows.get(9).getTableCells().get(3).setText("待补充");
            return doc;
        }catch (Exception e){
            System.out.println("设置委托单信息到模板异常:"+e);
        }
        return doc;
    }
}
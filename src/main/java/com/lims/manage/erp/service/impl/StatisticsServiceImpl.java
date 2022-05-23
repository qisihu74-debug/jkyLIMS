package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.mapper.StatisticsMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.service.StatisticsService;
import com.lims.manage.erp.vo.*;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private StatisticsMapper statisticsMapper;
    @Autowired
    private TaskMapper taskMapper;


    @Override
    public PageInfo taskQuery(TaskStatsVo taskStatsVo) {
        // 获取任务集合。
        PageHelper.startPage(taskStatsVo.getPageNum(), taskStatsVo.getPageSize());
        List<TaskStatsVo> list = statisticsMapper.selectTaskQuery(taskStatsVo);
        for (TaskStatsVo data : list) {
            // 通过任务单id 和 委托信息 获取样品名 和 所属的检测项价格及检测项状态（试验已开始，原始记录已上传，已复核）；
            List<SampleEntity> sampleList = statisticsMapper.selectSampleEntityList(data.getTaskId(), data.getEntrustmentId());
            Set<String> set = new HashSet<>();
            Integer testPrice = 0;
            for (SampleEntity sampleEntity : sampleList) {
                set.add(sampleEntity.getSampleName());
                for (SampleItemEntity sampleItemInstrumentEntity : sampleEntity.getSampleCheckItem()) {
                    // 进行强转 子类 继承 父类信息。
                    SampleItemInstrumentEntity sampleItemInstrumentEntity1 = (SampleItemInstrumentEntity) sampleItemInstrumentEntity;
                    // times 次数 * 单价 UnitPrice = 此次检测价格。
                    if (sampleItemInstrumentEntity1.getTimes() != null && sampleItemInstrumentEntity1.getUnitPrice() != null) {
                        testPrice += sampleItemInstrumentEntity1.getTimes() * sampleItemInstrumentEntity1.getUnitPrice();
                    }
                }
            }
            data.setCost(testPrice.toString());
            data.setSampleName(set.toString());
            // 任务单 state = 6.原始记录已复核， 其余都未复核
            data.setTaskStatus(data.getState() != null && data.getState() == 6 ? "完成复核" : "未完成复核");
        }
        PageInfo<TaskStatsVo> result = new PageInfo<>(list);
        return result;
    }

    @Override
    public TaskStatsVo TaskDetails(Long taskId) {
        TaskStatsVo taskStatsVo = new TaskStatsVo();
        List<TaskStatsItemVo> itemList = new ArrayList<>();
        // 查询任务单详情 判断 进度
        TaskTestEntity taskTestEntity = taskMapper.getTaskOrders(taskId);
        taskStatsVo.setTaskProgress(taskTestEntity.getState()>1?"已领取":"未领取");
        TaskDetailInfoVo list = taskMapper.getTaskDetailInfo(taskId);
        taskStatsVo.setTaskCode(list.getTaskCode());
        taskStatsVo.setTaskId(list.getTaskId());
        if (!list.getSampleDetailList().isEmpty()) {
            for (SampleDetailVo sampleDetailVo : list.getSampleDetailList()) {
                if (!sampleDetailVo.getCheckItemInfoList().isEmpty()) {
                   for(CheckItemInfoVo checkItemInfoVo :sampleDetailVo.getCheckItemInfoList())
                   {
                       TaskStatsItemVo taskStatsItemVo = new TaskStatsItemVo();
                       taskStatsItemVo .setCheckItemName(checkItemInfoVo.getCheckItemName());
                       taskStatsItemVo.setItemId(checkItemInfoVo.getItemId());
                       // 判断 试验开始
                       taskStatsItemVo.setTestBegun(checkItemInfoVo.getState()>0?true:false);
                       // 原始记录上传
                       taskStatsItemVo.setUploadRecords(checkItemInfoVo.getOriginUrl()!=null?true:false);
                       // 已复核
                       taskStatsItemVo.setReview(checkItemInfoVo.getState()>=3?true:false);
                       itemList.add(taskStatsItemVo);
                   }
                }
            }
        }
        taskStatsVo.setList(itemList);
        return taskStatsVo;
    }

    @Override
    public InputStream exportPersonDetails(PageInfo list) throws IOException {
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
//建立新的sheet对象（excel的表单）
        HSSFSheet sheet=wb.createSheet("sheet0");
//在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1=sheet.createRow(0);
        //创建单元格并设置单元格内容
        row1.createCell(0).setCellValue("任务单编号");
        row1.createCell(1).setCellValue("要求完成日期");
        row1.createCell(2).setCellValue("实际完成日期");
        row1.createCell(3).setCellValue("费用");
        row1.createCell(4).setCellValue("报告编号");
        row1.createCell(5).setCellValue("报告类型");
        row1.createCell(6).setCellValue("样品名称");
        row1.createCell(7).setCellValue("状态");
        for(int i=0;i<list.getList().size();i++){
            TaskStatsVo personVo = (TaskStatsVo) list.getList().get(i);
            //在sheet里创建第二行
            HSSFRow row3=sheet.createRow(i+1);
            row3.createCell(0).setCellValue(personVo.getTaskCode());
            row3.createCell(1).setCellValue(personVo.getRequestDate());
            row3.createCell(2).setCellValue(personVo.getFinishDate());
            row3.createCell(3).setCellValue(personVo.getCost());
            row3.createCell(4).setCellValue(personVo.getReportCode());
            row3.createCell(5).setCellValue(personVo.getReportType());
            row3.createCell(6).setCellValue(personVo.getSampleName());
            row3.createCell(7).setCellValue(personVo.getTaskStatus());
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    public PageInfo areaStatistics(StatisticsParamVo paramVo) {
        PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
        List<AreaStatisticsResultVo> list = statisticsMapper.areaStatistics(paramVo);
        PageInfo<AreaStatisticsResultVo> result = new PageInfo<>(list);
        return result;
    }
}

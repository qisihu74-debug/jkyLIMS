package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TaskTestTeamEntity;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestDetectionDao;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private SampleEntityMapper sampleEntityMapper;
    @Autowired
    private TestDetectionDao testDetectionDao;

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
            if (sampleDetailList.size() > 1) {
                for (int i = 1; i < sampleDetailList.size(); i++) {
                }
            }
            //获取表格对应的行
            rows = table.getRows();
            //                表格逐行赋值
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < sampleDetailList.size(); i++) {
                SampleDetailVo sampleDetailVo = sampleDetailList.get(i);
                // 补充表格数据 样品名称
                rows.get(i + 1).getTableCells().get(0).setText(sampleDetailVo.getSampleName());
                // 规格/等级
                rows.get(i + 1).getTableCells().get(1).setText(sampleDetailVo.getSpecs());
                // 批号/编号
                rows.get(i + 1).getTableCells().get(2).setText(sampleDetailVo.getBatchNumber());
                // 样品数量
                rows.get(i + 1).getTableCells().get(3).setText(sampleDetailVo.getGeneration());
                // 样品产地
                rows.get(i + 1).getTableCells().get(4).setText(sampleDetailVo.getSampleOrigin());
                //样品编号
                rows.get(i + 1).getTableCells().get(5).setText(sampleDetailVo.getSampleCode());
                // 备注
                rows.get(i + 1).getTableCells().get(6).setText(sampleDetailVo.getRemark());
                for (CheckItemInfoVo checkItemInfoVo : sampleDetailVo.getCheckItemInfoList()) {
                    stringBuilder.append(checkItemInfoVo.getCheckItemName() + "(" + checkItemInfoVo.getStandardName() + ")" + ",");
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
        } catch (Exception e) {
            System.out.println("设置委托单信息到模板异常:" + e);
        }
        return doc;
    }

    @Override
    public OriginalRecordDataVo getOriginalData(Long taskId, Integer sampleId, Integer checkItemId) {
        //生成记录编号
        String recordNumber = "JL-C2105-108-04";
        //获取委托单信息
        EntrustEntity entrustBaseInfo = taskMapper.getEntrustBaseInfo(taskId);
        //获取样品信息
        TemplateSampleVo sampleVo = sampleEntityMapper.getOriginalSampleInfo(sampleId);
        //获取检测依据
        log.debug("执行上一行完成---------------");
        String checkBasis = taskMapper.getCheckBasis(checkItemId, entrustBaseInfo.getId(), sampleId);
        log.debug("执行下一行完成--------------");
        //获取判定依据
        List<String> judgeBasisList = taskMapper.getJudgeBasis(sampleId, entrustBaseInfo.getId());
        StringBuilder judgeBasis = new StringBuilder("");
        if (judgeBasisList != null && !judgeBasisList.isEmpty()) {
            for (int i = 0; i < judgeBasisList.size(); i++) {
                judgeBasis.append(judgeBasisList.get(i) + "\n");
            }
        }
        OriginalRecordDataVo result = new OriginalRecordDataVo(recordNumber, entrustBaseInfo, sampleVo, checkBasis, judgeBasis.toString());
        return result;
    }

    @Override
    public String getOriginalTemplate(Integer checkItemId) {
        return taskMapper.getOriginalTemplate(checkItemId);
    }

    @Override
    public int uploadOriginalRecord(OriginalRecordParamVo paramVo, MultipartFile file) {
        //获取委托单信息
        EntrustEntity entrustBaseInfo = taskMapper.getEntrustBaseInfo(paramVo.getTaskId());
        String upload = "";
        String fileUrlStr = "";
        if (file != null) {
            String name = file.getOriginalFilename();
            String[] strings = name.split("\\.");
            upload = MinIoUtil.upload("upload-original-record", file, entrustBaseInfo.getId() + "-" + paramVo.getSampleId() + "-" + paramVo.getCheckItemId() + "." + strings[strings.length - 1]);
            fileUrlStr = entrustBaseInfo.getId() + "-" + paramVo.getSampleId() + paramVo.getCheckItemId() + "." + strings[strings.length - 1];
        }

        return taskMapper.updateOriginalFile(upload, entrustBaseInfo.getId(), paramVo.getSampleId(), paramVo.getCheckItemId(), fileUrlStr);
    }

    @Override
    public ReviewVo getReviewInfo(Integer itemId) {
        return taskMapper.getReviewInfo(itemId);
    }

    @Override
    public int passorno(Integer itemId, Integer state, String opinion) {
        // 驳回=4，通过=3，撤回=1
        if (state != null) {
            if (state == 1) {
                SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
                if (sampleItemInstrumentEntity2.getOriginUrl() != null && !sampleItemInstrumentEntity2.getOriginUrl().isEmpty()) {
                    // 去清除 MinIo 桶数据。
                    try {
                        MinIoUtil.deleteFile("upload-original-record", sampleItemInstrumentEntity2.getFileUrlStr());
                    } catch (Exception e) {
                        logger.info("修改委托下清除 MinIo 桶数据 出错");
                    }
                }
                SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
                // 待检状态 =0
                sampleItemInstrumentEntity.setState(0);
                // 检测项 开始时间更新
                sampleItemInstrumentEntity.setStartTime(null);
                sampleItemInstrumentEntity.setItemId(itemId);
                sampleItemInstrumentEntity.setOpinion(opinion);
                sampleItemInstrumentEntity.setOriginUrl(null);
                sampleItemInstrumentEntity.setFileUrlStr(null);
                testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
                // 删除设备仪器
                testDetectionDao.deleteInstrument(itemId);
                return 1;
            }
        }
        return taskMapper.updateState(itemId, state, opinion);
    }

    @Override
    public PersonInfoVo getPersonInfo(Long taskId) {
        return taskMapper.getPersonInfo(taskId);
    }

    @Override
    public int updatePersonInfo(PersonInfoVo vo) {
        return taskMapper.updatePersonInfo(vo);
    }
}
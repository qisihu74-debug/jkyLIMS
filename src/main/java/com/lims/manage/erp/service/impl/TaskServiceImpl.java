package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.config.PoiConfig;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

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
    @Autowired
    private ReportRecordEntityMapper reportRecordEntityMapper;
    @Autowired
    private EntrustEntityMapper entrustEntityMapper;
    @Autowired
    private TeamMapper teamMapper;

    @Override
    public TaskDetailInfoVo getTaskDetailInfo(Long taskId) {
        // 处理 委托单的文件链接
        TaskDetailInfoVo taskDetailInfoVo = taskMapper.getTaskDetailInfo(taskId);
        if (taskDetailInfoVo.getFileUrl() != null) {
            String[] array = taskDetailInfoVo.getFileUrl().split(",");
            taskDetailInfoVo.setArray(array);
        }
        // 获取文件附件
        Long entrustId = taskMapper.getEntrustIdByTaskId(taskId);
        List<String> strings = entrustEntityMapper.getSampleStandard(entrustId);
        StringBuilder stringBuilder = new StringBuilder();
        if (strings != null && !strings.isEmpty()) {
            for (String str : strings) {
                stringBuilder.append(str);
                stringBuilder.append(",");
            }
            taskDetailInfoVo.setJudgmentBasis(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
        }

        return taskDetailInfoVo;
    }

    @Override
    public TaskDetailInfoVo getTaskDetailInfoTwo(Long taskId, String[] deptIds) {
        TaskListParamVo paramVo = new TaskListParamVo();
        paramVo.setTaskId(taskId);
        if (deptIds != null) {
            // 根据部门id 遍历包含下级部门信息
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < deptIds.length; i++) {
                ids.add(Long.valueOf(deptIds[i]));
            }
            paramVo.setDeptIds(ids);
        } else {
            // 查询任务单 所属部门id
            Long deptId = taskMapper.getTaskDept(taskId);
            if (deptId == null) {
                paramVo.setDeptIds(null);
            } else {
                List<Long> ids = new ArrayList<>();
                ids.add(deptId);
                paramVo.setDeptIds(ids);
            }
        }
        // 处理 委托单的文件链接
        TaskDetailInfoVo taskDetailInfoVo = taskMapper.getTaskDetailInfoTwo(paramVo);
        if (taskDetailInfoVo.getFileUrl() != null) {
            String[] array = taskDetailInfoVo.getFileUrl().split(",");
            taskDetailInfoVo.setArray(array);
        }
        // 获取文件附件
        Long entrustId = taskMapper.getEntrustIdByTaskId(taskId);
        List<String> strings = entrustEntityMapper.getSampleStandard(entrustId);
        StringBuilder stringBuilder = new StringBuilder();
        if (strings != null && !strings.isEmpty()) {
            for (String str : strings) {
                stringBuilder.append(str);
                stringBuilder.append(",");
            }
            taskDetailInfoVo.setJudgmentBasis(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
        }
        // 根据itemId 查询设备仪器信息集合
        if (taskDetailInfoVo.getSampleDetailList() != null && !taskDetailInfoVo.getSampleDetailList().isEmpty()) {
            for (SampleDetailVo sampleDetailVo : taskDetailInfoVo.getSampleDetailList()) {
                if (sampleDetailVo.getCheckItemInfoList() != null && !sampleDetailVo.getCheckItemInfoList().isEmpty()) {
                    for (CheckItemInfoVo checkItemInfoVo : sampleDetailVo.getCheckItemInfoList()) {
                        List<TestInstrumentEntity> instrumentEntityList = taskMapper.getInstrumentEntityList(checkItemInfoVo.getItemId());
//                        List<Integer> result = Lists.newArrayList();
                        // 设置数组 存放
                        int[] arrayInt = new int[instrumentEntityList.size()];
                        if (instrumentEntityList != null && !instrumentEntityList.isEmpty()) {
                            checkItemInfoVo.setTestInstrumentEntityList(instrumentEntityList);
                            for (int i = 0; i < instrumentEntityList.size(); i++) {
                                arrayInt[i] = instrumentEntityList.get(0).getId();
                            }
                            checkItemInfoVo.setTestInstrumentEntityArray(arrayInt);
                        } else {
                            checkItemInfoVo.setTestInstrumentEntityList(instrumentEntityList);
                            checkItemInfoVo.setTestInstrumentEntityArray(arrayInt);
                        }
                    }
                }
            }
        }


        return taskDetailInfoVo;
    }

    @Override
    public TaskDetailInfoVo getTaskTestDetails(Long taskId, String[] deptIds) {
        TaskListParamVo paramVo = new TaskListParamVo();
        paramVo.setTaskId(taskId);
        if (deptIds != null) {
            // 根据部门id 遍历包含下级部门信息
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < deptIds.length; i++) {
                ids.add(Long.valueOf(deptIds[i]));
            }
            paramVo.setDeptIds(ids);
        } else {
            // 查询任务单 所属部门id
            Long deptId = taskMapper.getTaskDept(taskId);
            if (deptId == null) {
                paramVo.setDeptIds(null);
            } else {
                List<Long> ids = new ArrayList<>();
                ids.add(deptId);
                paramVo.setDeptIds(ids);
            }
        }
        // 处理 委托单的文件链接
        TaskDetailInfoVo taskDetailInfoVo = taskMapper.getTaskDetailInfoTwo(paramVo);
        if (taskDetailInfoVo.getFileUrl() != null) {
            String[] array = taskDetailInfoVo.getFileUrl().split(",");
            taskDetailInfoVo.setArray(array);
        }
        // 遍历数据 处理检测项下 检测项价格单位为空不展示。
        if (taskDetailInfoVo.getSampleDetailList() != null && !taskDetailInfoVo.getSampleDetailList().isEmpty()) {

            for (SampleDetailVo sampleDetailVo : taskDetailInfoVo.getSampleDetailList()) {
                if (sampleDetailVo.getCheckItemInfoList() != null && !sampleDetailVo.getCheckItemInfoList().isEmpty()) {
                    Iterator<CheckItemInfoVo> it = sampleDetailVo.getCheckItemInfoList().iterator();
                    while (it.hasNext()) {
                        CheckItemInfoVo judgmentBasisVo = it.next();
                        if (judgmentBasisVo.getCheckPrice() == null) {
                            it.remove();
                        }
                    }
                }
            }
        }
        // 获取文件附件
        Long entrustId = taskMapper.getEntrustIdByTaskId(taskId);
        List<String> strings = entrustEntityMapper.getSampleStandard(entrustId);
        StringBuilder stringBuilder = new StringBuilder();
        if (strings != null && !strings.isEmpty()) {
            for (String str : strings) {
                stringBuilder.append(str);
                stringBuilder.append(",");
            }
            taskDetailInfoVo.setJudgmentBasis(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
        }
        // 根据itemId 查询设备仪器信息集合
        if (taskDetailInfoVo.getSampleDetailList() != null && !taskDetailInfoVo.getSampleDetailList().isEmpty()) {
            for (SampleDetailVo sampleDetailVo : taskDetailInfoVo.getSampleDetailList()) {
                if (sampleDetailVo.getCheckItemInfoList() != null && !sampleDetailVo.getCheckItemInfoList().isEmpty()) {
                    for (CheckItemInfoVo checkItemInfoVo : sampleDetailVo.getCheckItemInfoList()) {
                        List<TestInstrumentEntity> instrumentEntityList = taskMapper.getInstrumentEntityList(checkItemInfoVo.getItemId());
//                        List<Integer> result = Lists.newArrayList();
                        // 设置数组 存放
                        int[] arrayInt = new int[instrumentEntityList.size()];
                        if (instrumentEntityList != null && !instrumentEntityList.isEmpty()) {
                            checkItemInfoVo.setTestInstrumentEntityList(instrumentEntityList);
                            for (int i = 0; i < instrumentEntityList.size(); i++) {
                                arrayInt[i] = instrumentEntityList.get(0).getId();
                            }
                            checkItemInfoVo.setTestInstrumentEntityArray(arrayInt);
                        } else {
                            checkItemInfoVo.setTestInstrumentEntityList(instrumentEntityList);
                            checkItemInfoVo.setTestInstrumentEntityArray(arrayInt);
                        }
                    }
                }
            }
        }


        return taskDetailInfoVo;
    }

    @Override
    public List<TaskListVo> getTaskList(TaskListParamVo paramVo) {
        return taskMapper.getTaskList(paramVo);
    }

    @Override
    public String getDeptIds(Long userId) {

        // 根据人员id 返回团队id集合
        List<Long> deptIds = teamMapper.getUserTeamIds(userId);
        if (deptIds != null && !deptIds.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Long detId : deptIds) {
                stringBuilder.append(detId);
                stringBuilder.append(",");
            }
            return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
        }
        return null;
    }

    /**
     * 查询任务列表列表 并设置
     *
     * @param paramVo
     * @param deptIds
     * @return
     */
    @Override
    public PageInfo getTaskListTwo(TaskListParamVo paramVo, String[] deptIds) {

        if (deptIds != null && deptIds.length >= 1) {
            // 根据部门id 遍历包含下级部门信息
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < deptIds.length; i++) {
                ids.add(Long.valueOf(deptIds[i]));
            }
            paramVo.setDeptIds(ids);
        } else {
            paramVo.setDeptIds(null);
        }
        List<TaskListVo> dataList = new ArrayList<>();
        if (paramVo.getState() != null && paramVo.getState() != 1) {
            PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
            dataList = taskMapper.getTaskListTwo(paramVo);
        }
        if (paramVo.getState() == 1) {
            PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
            dataList = taskMapper.getTaskListTwoGreater(paramVo);
        }
//        if (dataList != null && !dataList.isEmpty()) {
//            for (TaskListVo data : dataList) {
//                if (data.getInspector() != null) {
//                    String[] strings2 = data.getInspector().split(",");
//                    StringBuilder stringBuilder = new StringBuilder();
//                    for (int i = 0; i < strings2.length; i++) {
//                        String[] strings3 = strings2[i].split("&");
//                        stringBuilder.append(strings3[0]);
//                        stringBuilder.append(",");
//                    }
//                    data.setInspector(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
//                }
//            }
//        }
        PageInfo<TaskListVo> result = new PageInfo<>(dataList);
        return result;
    }

    @Override
    public PageInfo getSampleList(TaskListParamVo paramVo) {
        //查询用户团队及子团队ID
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        //根据团队信息查询样品信息
        String receiveTime = paramVo.getReceiveTime();
        if (receiveTime != null) {
            String[] split = receiveTime.split("~");
            paramVo.setBeginDate(split[0]);
            paramVo.setEndDate(split[1]);
        }
        if (userTeamIds.size() > 0) {
            paramVo.setDeptIds(userTeamIds);
        } else {
            paramVo.setDeptIds(null);
            return null;
        }

        PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
        List<ReceiveSampleListVo> dataList = taskMapper.getSampleList(paramVo);
        //TODO gjl添加样品状态
        EntrustServiceImpl service = new EntrustServiceImpl();
        for (ReceiveSampleListVo sampleListVo : dataList) {
            List<SamplePrivateInfoVo> sampleList = sampleListVo.getSampleList();
            for (SamplePrivateInfoVo samplePrivateInfoVo : sampleList) {
                String state = service.findStateBySampleId(samplePrivateInfoVo.getId(), entrustEntityMapper, taskMapper);
                samplePrivateInfoVo.setState(state);
            }
        }
        PageInfo<ReceiveSampleListVo> result = new PageInfo<>(dataList);
        return result;
    }

    @Override
    public int receiveSample(ReceiveSampleParamVo paramVo) {
        paramVo.setState(2);
        // 根据任务单主键 获取委托单主键
        EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(paramVo.getTaskId());
        if (entrustEntity != null) {
            taskMapper.updateEntrustById(entrustEntity.getId(), 2);
        }
        return taskMapper.updateSampler(paramVo);
    }

    @Override
    public Boolean isIntendedEffectReceive(Long taskId, String sampler) {
        List<String> strings = teamMapper.getTaskIdUserName(taskId);
        if (strings != null && strings.size() > 0) {
            for (String userName : strings) {
                if (userName.equals(sampler)) {
                    // 匹配成功
                    return true;
                }
            }
            // 领样人不属于此任务单下团队成员
            return false;
        }
        logger.error(taskId + "\t任务单下部门成员为空");
        return false;
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
    public Boolean postGrabASingleTwo(TaskTestEntity taskTestEntity) {
        // 抢单
        taskTestEntity.setState(1);
        // 抢单时间
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        taskTestEntity.setReceiveTime(currentDate);
        taskMapper.updateTestTask(taskTestEntity);
        return true;
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
    public TeamVo getTeamUserNameTwo(Long UserLong) {
        TeamVo teamVo = new TeamVo();
        // 返回团队id集合 根据人员id
        List<TeamTreeStructureEntity> dataDepts = taskMapper.getTeamDeptVo(UserLong);
        if (dataDepts != null && !dataDepts.isEmpty()) {
            Set<Long> deptIds = new HashSet<>();
            for (TeamTreeStructureEntity data : dataDepts) {
                deptIds.add(data.getId());
                if (data.getSId() != null) {
                    deptIds.add(data.getSId());
                }
            }
            // 团队id集合 返回人员信息
            List<LabelValueVo> teamVos = taskMapper.getMemberInformation(deptIds);
            teamVo.setTeamVo(teamVos);
        }
        // 复核人集合

        teamVo.setReviewVo(null);
        // 审批人集合
        List<LabelValueVo> ApproverVo = taskMapper.getRoleInformation(Const.approverStr);
        teamVo.setApproverVo(ApproverVo);
        // 签发人集合
        List<LabelValueVo> SignerVo = taskMapper.getRoleInformation(Const.signerStr);
        teamVo.setSignerVo(SignerVo);
        // 获取科室下人员信息 （一个科室下）

        return teamVo;
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
            // 第一个整体表格 设置表格
            XWPFTable table = tables.get(0);
            // 第二个整体表格
            XWPFTable table1 = tables.get(1);
            List<XWPFTableRow> rows1;
            rows1 = table1.getRows();
            // 第三个表格 设置印章类型
            XWPFTable table2 = tables.get(2);
            List<XWPFTableRow> rows2;
            rows2 = table2.getRows();
            //表格属性
            CTTblPr pr = table.getCTTbl().getTblPr();
            //表头部分
            //解析替换文本段落对象
            Map<String, String> testMap = new HashMap<String, String>();
            // 下单日期
            testMap.put("date", taskDetailInfoVo.getOrderTime());
            // 编号
            testMap.put("number", taskDetailInfoVo.getTaskCode());
            PoiConfig.changeText(doc, testMap);

            // 获取委托单印章
            if (taskDetailInfoVo.getSealType() != null) {
                String[] sealTypes = taskDetailInfoVo.getSealType().split(",");
                // 任务单表格数据
                List<String> totalData = new ArrayList<>();
                totalData.add("综合甲级");
                totalData.add("CMA");
                totalData.add("CNAS");
                // 勾选数据
                String check = "☑";
                // 未勾选数据
                String untick = "□";
                // map 数据 委托单盖章数据
                HashMap<String, Integer> map = new HashMap<>();
                //   HashMap 去重利器
                StringBuilder sealType = new StringBuilder();
                for (int i = 0; i < sealTypes.length; i++) {
                    for (String totalData1 : totalData) {
                        // 盖章勾选
                        if (sealTypes[i].equals(totalData1)) {
                            sealType.append(check);
                            sealType.append(sealTypes[i]);
                            map.put(sealTypes[i], 1);
                        }
                    }
                }
                // 不勾选的数据查找
                for (String rowData1 : totalData) {
                    if (map.get(rowData1) == null) {
                        sealType.append(untick);
                        sealType.append(rowData1);
                    }
                }
                rows2.get(14).getTableCells().get(1).setText(sealType.toString());
            }

            // 遍历 样品数据
            List<SampleDetailVo> sampleDetailList = taskDetailInfoVo.getSampleDetailList();
            if (taskDetailInfoVo.getSampleDetailList() == null || taskDetailInfoVo.getSampleDetailList().isEmpty()) {
                return doc;
            }
            //获取表格对应的行
            rows = table.getRows();
            // 判断表格行数 >5 sampleDetailList.size()
            // 补充增加表格数量。
            if (sampleDetailList.size() > 5) {
                // 3.25 测试表格插入数据
                int addRows = sampleDetailList.size() - 5;
                // 表格插入
                XWPFDocument doc1 = new XWPFDocument();
                XWPFTable newTable = doc1.createTable(addRows, 7);  //2行7格
                // 创建表格后直接进行存放 后续多余数据
                List<XWPFTableRow> dataTable = newTable.getRows();
                int j = 0;
                for (int i = 5; i < sampleDetailList.size(); i++) {
                    SampleDetailVo sampleDetailVo = sampleDetailList.get(i);
                    // 补充表格数据 样品名称
                    dataTable.get(j).getTableCells().get(0).setText(sampleDetailVo.getSampleName());
                    // 规格/等级
                    dataTable.get(j).getTableCells().get(1).setText(sampleDetailVo.getSpecs());
                    // 批号/编号
                    dataTable.get(j).getTableCells().get(2).setText(sampleDetailVo.getBatchNumber());
                    // 样品数量
                    dataTable.get(j).getTableCells().get(3).setText(sampleDetailVo.getGeneration());
                    // 样品产地
                    dataTable.get(j).getTableCells().get(4).setText(sampleDetailVo.getSampleOrigin());
                    //样品编号
                    dataTable.get(j).getTableCells().get(5).setText(sampleDetailVo.getSampleCode());
                    // 备注
                    dataTable.get(j).getTableCells().get(6).setText(sampleDetailVo.getRemark());
                    table.addRow(dataTable.get(j));
                    j++;
                }
//                XWPFTableRow rows25 = table25.getRow(0);
//                rows25.getCell(0).setText("sampleDetailVo.getSampleName()");
//                table.addRow(table25.getRow(0));
                rows = table.getRows();


            }
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
            rows1.get(0).getTableCells().get(0).setText(taskDetailInfoVo.getPresentInformation());
            // 取样方式
            rows1.get(1).getTableCells().get(1).setText(taskDetailInfoVo.getSamplingMethod());
            // 检验目的
            rows1.get(1).getTableCells().get(3).setText(taskDetailInfoVo.getCheckPurpose());
            // 产品标准
            rows1.get(1).getTableCells().get(5).setText(taskDetailInfoVo.getJudgmentBasis());
            // 检测项目及检验依据
            rows1.get(2).getTableCells().get(1).setText(stringBuilder.toString());
            // 要求检验完成日期
            rows1.get(3).getTableCells().get(1).setText(taskDetailInfoVo.getRequiredCompletionTime());
            // 本单产值
            rows1.get(3).getTableCells().get(3).setText(taskDetailInfoVo.getCost());

            return doc;
        } catch (
                Exception e) {
            System.out.println("设置委托单信息到模板异常:" + e);
        }
        return doc;
    }

    @Override
    public OriginalRecordDataVo getOriginalData(Long taskId, Integer sampleId, Integer checkItemId, Integer idItem) {
        //生成记录编号
        String recordNumber = "JL-C2105-108-04";
        //获取委托单信息
        EntrustEntity entrustBaseInfo = taskMapper.getEntrustBaseInfo(taskId);
        //获取样品信息
        TemplateSampleVo sampleVo = sampleEntityMapper.getOriginalSampleInfo(sampleId);
        // 得到样品信息数据; 分割。
        sampleVo.setSampleName(sampleVo.getSampleName() + ";");
        sampleVo.setSampleNumber(sampleVo.getSampleNumber() + ";");
        sampleVo.setSampleQuantity(sampleVo.getSampleQuantity() + ";");
        sampleVo.setSampleDesc(sampleVo.getSampleDesc() + ";");
        sampleVo.setSampleTime(sampleVo.getSampleTime() + ";");
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
        // 检测项 开始检测日期。

        // 所使用的设备仪器。
        List<TestInstrumentEntity> InstrumentEntityList = taskMapper.getInstrumentEntityList(idItem);
        if (InstrumentEntityList != null && !InstrumentEntityList.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (TestInstrumentEntity testInstrumentEntity : InstrumentEntityList) {
                stringBuilder.append(testInstrumentEntity.getCode());
                stringBuilder.append(",");
            }
            result.setEquipment(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
        }

        return result;
    }

    @Override
    public String getOriginalTemplate(Integer checkItemId) {
        return taskMapper.getOriginalTemplate(checkItemId);
    }

    @Override
    public String getOriginalTemplateUrl(Integer checkItemId) {
        return taskMapper.getOriginalTemplateUrl(checkItemId);
    }

    @Override
    public int uploadOriginalRecord(OriginalRecordParamVo paramVo, MultipartFile file) {
        //获取委托单信息
        EntrustEntity entrustBaseInfo = taskMapper.getEntrustBaseInfo(paramVo.getTaskId());
        // 获取检测项详细信息 判断文件是否上传
        SampleItemInstrumentEntity sampleItemInstrumentEntity = testDetectionDao.getTestEntrustedSampleCheckitemRelDetailIf(entrustBaseInfo.getId(), paramVo.getSampleId(), paramVo.getCheckItemId());
        if (sampleItemInstrumentEntity.getOriginUrl() != null) {
            // 文件已经存在
            return 2;
        }
        String upload = "";
        String fileUrlStr = "";
        // 原始名称
        String fileName = "";
        if (file != null) {
            String name = file.getOriginalFilename();
            String[] strings = name.split("\\.");
            upload = MinIoUtil.upload("upload-original-record", file, entrustBaseInfo.getId() + "-" + paramVo.getSampleId() + "-" + paramVo.getCheckItemId() + "." + strings[strings.length - 1]);
            fileUrlStr = entrustBaseInfo.getId() + "-" + paramVo.getSampleId() + "-" + paramVo.getCheckItemId() + "." + strings[strings.length - 1];
            fileName = strings[0] + "." + strings[strings.length - 1];
        }
        // 根据任务单主键 获取委托单主键
        if (entrustBaseInfo != null) {
            if (entrustBaseInfo.getState() < 5) {
                taskMapper.updateEntrustById(entrustBaseInfo.getId(), 5);
            }
        }
        return taskMapper.updateOriginalFile(upload, entrustBaseInfo.getId(), paramVo.getSampleId(), paramVo.getCheckItemId(), fileUrlStr, fileName);
    }

    /**
     * 检测项复核数据
     *
     * @param itemId
     * @return
     */
    @Override
    public ReviewVo getReviewInfo(Integer itemId) {
        // 根据检测项主键 获取委托单主键
        return taskMapper.getReviewInfo(itemId);
    }

    @Override
    public String passorno(Integer itemId, Integer state, String opinion) {
        // 驳回=4，通过=3，撤回=1
        if (state != null) {
            if (state == 1) {
                SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
                // 检测项 撤回时 考虑 （盖章的话） test_report_record state = '7'
                // 通过委托单id 获取报告test_report_record state 状态
                ReportRecordEntity reportRecordEntity = reportRecordEntityMapper.selectByEntrustId(sampleItemInstrumentEntity2.getEntrustId());
                if (reportRecordEntity != null && reportRecordEntity.getState() != null) {
                    if (Integer.parseInt(reportRecordEntity.getState()) == 7 && Integer.parseInt(reportRecordEntity.getState()) >= 7) {
                        return "撤回失败！此报告已经盖章";
                    }
                }

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
                sampleItemInstrumentEntity.setEndTime(null);
                testDetectionDao.updateTaskPassorno(sampleItemInstrumentEntity);
                // 删除设备仪器
                testDetectionDao.deleteInstrument(itemId);
                return "撤回成功，检测项回到初始状态";
            }
            if (state == 3) {
                // 检测项复核通过
                taskMapper.updateState(itemId, 3, opinion);
                SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
                if (sampleItemInstrumentEntity2 != null && sampleItemInstrumentEntity2.getEntrustId() != null) {
                    EntrustAddVo entrustBaseInfo = entrustEntityMapper.selectByKeyId(sampleItemInstrumentEntity2.getEntrustId());
                    if (entrustBaseInfo.getState() != null && entrustBaseInfo.getState() < 6) {
                        taskMapper.updateEntrustById(entrustBaseInfo.getId(), 6);
                    }
                }
                // 通过委托单id 和部门ID为条件  遍历（判断每个状态 state = 3 复核通过。 改变任务单 6 否则 任务单还是为试验完成）
                List<Integer> states = testDetectionDao.getSampleCheckitemRelDetailState(sampleItemInstrumentEntity2.getEntrustId(), sampleItemInstrumentEntity2.getDeptId());
                for (Integer stateItem : states) {
                    if (stateItem != 3) {
                        return "当前任务单下检测项未全部复核成功";
                    }
                }
                // 修改test_task state 状态 为6：
                Long testTaskId = taskMapper.getTestTaskId(sampleItemInstrumentEntity2.getEntrustId(), sampleItemInstrumentEntity2.getDeptId());
                TaskTestEntity taskTestEntity = new TaskTestEntity();
                taskTestEntity.setId(testTaskId);
                taskTestEntity.setState(6);
                taskMapper.updateTestTask(taskTestEntity);
                return "任务单复核成功";
            }
        }
        // state = 4 检测项状态驳回
        int status = taskMapper.updateState(itemId, state, opinion);
        if (status > 0) {
            return "成功";
        }
        return "失败";
    }

    @Override
    public String passorno_delete(Integer itemId) {
        SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
        // 检测项 撤回时 考虑 （盖章的话） test_report_record state = '7'
        // 通过委托单id 获取报告test_report_record state 状态
        ReportRecordEntity reportRecordEntity = reportRecordEntityMapper.selectByEntrustId(sampleItemInstrumentEntity2.getEntrustId());
        if (reportRecordEntity != null && reportRecordEntity.getState() != null) {
            if (Integer.parseInt(reportRecordEntity.getState()) == 7 && Integer.parseInt(reportRecordEntity.getState()) >= 7) {
                return "撤回失败！此报告已经盖章";
            }
        }

        if (sampleItemInstrumentEntity2.getOriginUrl() != null && !sampleItemInstrumentEntity2.getOriginUrl().isEmpty()) {
            // 去清除 MinIo 桶数据。
            try {
                MinIoUtil.deleteFile("upload-original-record", sampleItemInstrumentEntity2.getFileUrlStr());
            } catch (Exception e) {
                logger.info("修改委托下清除 MinIo 桶数据 出错");
            }
        }
        SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
        sampleItemInstrumentEntity.setStartTime(null);
        sampleItemInstrumentEntity.setItemId(itemId);
        sampleItemInstrumentEntity.setOriginUrl(null);
        sampleItemInstrumentEntity.setFileUrlStr(null);
        sampleItemInstrumentEntity.setEndTime(null);
        sampleItemInstrumentEntity.setItemFileName(null);
        testDetectionDao.updateTaskPassorno(sampleItemInstrumentEntity);
        return "成功";
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
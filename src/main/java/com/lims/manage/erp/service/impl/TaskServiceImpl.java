package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.config.PoiConfig;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.EntrustFileTableDao;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.SysRoleDao;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TeamMapper;
import com.lims.manage.erp.mapper.TestDetectionDao;
import com.lims.manage.erp.mapper.TestEntrustedTaskRelDao;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.mapper.TestSampleEntityMapper;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ConvertUtil;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.ExcelReplaceUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.ExcelInsertVo;
import com.lims.manage.erp.vo.LabelValueTeamVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.OriginalRecordDataVo;
import com.lims.manage.erp.vo.OriginalRecordParamVo;
import com.lims.manage.erp.vo.PagingToolVo;
import com.lims.manage.erp.vo.PersonInfoVo;
import com.lims.manage.erp.vo.ReceiveSampleListVo;
import com.lims.manage.erp.vo.ReceiveSampleParamVo;
import com.lims.manage.erp.vo.ReviewVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.SamplePrivateInfoVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import com.lims.manage.erp.vo.TaskListParamVo;
import com.lims.manage.erp.vo.TaskListVo;
import com.lims.manage.erp.vo.TaskStatsItemVo;
import com.lims.manage.erp.vo.TaskStatsVo;
import com.lims.manage.erp.vo.TeamVo;
import com.lims.manage.erp.vo.TemplateSampleVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class TaskServiceImpl<labelValueVos> implements TaskService {
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
    @Autowired
    private TestSampleEntityMapper testSampleEntityMapper;
    @Autowired
    private EntrustFileTableDao entrustFileTableDao;
    @Autowired
    private TestEntrustedTaskRelDao testEntrustedTaskRelDao;
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private TestProductItemDao testProductItemDao;
    @Autowired
    private SysRoleDao sysRoleDao;

    @Override
    public TaskDetailInfoVo getTaskDetailInfo(Long taskId) {
        // 处理 委托单的文件链接
        TaskDetailInfoVo taskDetailInfoVo = taskMapper.getTaskDetailInfo(taskId);
/*        if (taskDetailInfoVo.getFileUrl() != null) {
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
        }*/

        return taskDetailInfoVo;
    }

    @Override
    public TaskDetailInfoVo getTaskDetailInfoTwo(Long taskId, String[] deptIds) {
        TaskListParamVo paramVo = new TaskListParamVo();
        paramVo.setTaskId(taskId);
        // 处理 委托单的文件链接
        PageHelper.clearPage();
        TaskDetailInfoVo taskDetailInfoVo = new TaskDetailInfoVo();
        // 通过任务单id 查询检测项为空集合 调用其他方法操作
        List<CheckItemInfoVo> list = taskMapper.getEntrustItemVos(taskId);
        if(CollectionUtils.isEmpty(list)){
            // 任务单不包含检测项 进行查询
            taskDetailInfoVo = taskMapper.getTaskDetailInfoThree(paramVo);
        }else {
            taskDetailInfoVo = taskMapper.getTaskDetailInfoTwo(paramVo);
        }
        //TODO dlc 补充任务单价格
        if(StringUtils.isEmpty(taskDetailInfoVo.getCost())){
            taskDetailInfoVo.setCost("--");
        }
        // 获取文件附件
        Long entrustId = taskMapper.getEntrustIdByTaskId(taskId);
        /**
         * 委托单文件file 处理
         * 通过委托单id 查询相应附件集合
         */
        List<EntrustFileTableEntity> fileList = entrustFileTableDao.getEntrustFileTableEntityList(entrustId);
        if(CollectionUtils.isEmpty(fileList)){
            // 返回空集合
            List<EntrustFileTableEntity> fileListNull = new ArrayList<>();
            taskDetailInfoVo.setFileArrays(fileListNull);
        }
        else {
            taskDetailInfoVo.setFileArrays(fileList);
        }
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
                        if(org.apache.commons.lang.StringUtils.isNotEmpty(checkItemInfoVo.getOriginUrl())){
                            String[] split = checkItemInfoVo.getOriginUrl().split("\\?");
                            String[] strings1 = split[0].split("\\/");
                            String fileName = strings1[4];
                            String[] names = fileName.split("\\.");
                            if("pdf".equals(names[1]) || "xls".equals(names[1]) || "xlsx".equals(names[1])){
                                checkItemInfoVo.setSuffixType("1");
                            }
                            else if("jpeg".equals(names[1]) || "png".equals(names[1]) || "jpg".equals(names[1])){
                                checkItemInfoVo.setSuffixType("2");
                            }
                        }

                        List<TestInstrumentEntity> instrumentEntityList = taskMapper.getInstrumentEntityList(checkItemInfoVo.getItemId());
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
                //设置原材样品信息
                taskDetailInfoVo.setNodeSample(testSampleEntityMapper.selectByPid(sampleDetailVo.getId()));
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
        TaskDetailInfoVo taskDetailInfoVo = new TaskDetailInfoVo();
        // 通过任务单id 查询检测项为空集合 调用其他方法操作
        List<CheckItemInfoVo> list = taskMapper.getEntrustItemVos(taskId);
        if(CollectionUtils.isEmpty(list)){
            // 任务单不包含检测项 进行查询
            taskDetailInfoVo = taskMapper.getTaskDetailInfoThree(paramVo);
        }else {
            taskDetailInfoVo = taskMapper.getTaskDetailInfoTwo(paramVo);
        }
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
     * 查询检测列表 并设置
     *
     * @param paramVo
     * @param deptIds
     * @return
     */
    @Override
    public PagingToolVo getTaskListTwo(TaskListParamVo paramVo, String[] deptIds) {

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
        List<TaskListVo> personList = new ArrayList<>();
//        PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
        PageHelper.clearPage();
        personList = taskMapper.getTaskListContainsSample(paramVo);
        // 补充样品名称信息
        if(!CollectionUtils.isEmpty(personList)){
            for(TaskListVo taskListVo :personList)
            {
                StringBuilder stringBuilder = new StringBuilder();
                if(!CollectionUtils.isEmpty(taskListVo.getSampleList())){
                    for(SamplePrivateInfoVo samplePrivateInfoVo:taskListVo.getSampleList()){
                        stringBuilder.append(samplePrivateInfoVo.getAliasName());
                        stringBuilder.append("、");
                    }
                    taskListVo.setSampleName(stringBuilder.deleteCharAt(stringBuilder.length()-1).toString());
                }
            }
        }
        // 手动分页
        Integer pageNum =  paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        PagingToolVo pagingVo = new PagingToolVo();
        if(pageNum>0&&pageSize>0) {
            // 总条数
            pagingVo.setTotal(personList.size());
            // 开始
            pagingVo.setPageNum(pageNum);
            // 页码
            pagingVo.setPageSize(pageSize);
            // 当前页展示数量
            Integer size = personList.size() - (pageNum*pageSize); // 实际返回页码展示数量
            if(size>0){
                pagingVo.setSize(pageSize);
            }
            else {
                size =pageSize -( pageNum*pageSize - personList.size());
                pagingVo.setSize(size>0?size:0);
            }
            // 总页数
            pagingVo.setPages(personList.size() / pageSize);
            // 开始行数
            pagingVo.setStartRow(personList.size() / pageSize / pageNum);
            // 结束行数
            pagingVo.setEndRow(personList.size() / pageSize);
            List<TaskListVo> subList = Lists.newArrayList();
            if (!CollectionUtils.isEmpty(personList)) {
                try {
                    if (personList.size() > 10 && personList.size() / 10 >= pageNum) {
                        subList = personList.subList((pageNum - 1) * pageSize, pageNum * pageSize);
                    } else {
                        subList = personList.subList((pageNum - 1) * pageSize, personList.size());
                    }
                } catch (IndexOutOfBoundsException e) {
                    subList = personList;
                } catch (IllegalArgumentException e) {
                    subList = personList.subList(0, personList.size());
                } finally {
                    // 返回数据
                    pagingVo.setList(subList);
                }
            }
            // 分页后 逻辑处理
            methodManualPages((List<TaskListVo>) pagingVo.getList());
            return pagingVo;
        }
        else {
            if (!CollectionUtils.isEmpty(personList)) {
                pagingVo.setList(personList);
            }
            // 分页后 逻辑处理
            methodManualPages((List<TaskListVo>) pagingVo.getList());
            return pagingVo;
        }

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
            List<SamplePrivateInfoVo> nodeSampleList = Lists.newArrayList();
            for (SamplePrivateInfoVo samplePrivateInfoVo : sampleList) {
                String state = service.findStateBySampleId(samplePrivateInfoVo.getId(), entrustEntityMapper, taskMapper);
                samplePrivateInfoVo.setState(state);
                //TODO PSH查询子原材样品信息
                List<SamplePrivateInfoVo> nodeSampleList1 = taskMapper.getNodeSampleList(samplePrivateInfoVo.getId());
                if(!CollectionUtils.isEmpty(nodeSampleList1)){
                    nodeSampleList.addAll(nodeSampleList1);
                }
            }
            sampleList.addAll(nodeSampleList);
        }
        PageInfo<ReceiveSampleListVo> result = new PageInfo<>(dataList);
        return result;
    }

    @Transactional
    @Override
    public int receiveSample(ReceiveSampleParamVo paramVo) {
        paramVo.setState(2);
        // 根据任务单主键 获取委托单主键
        EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(paramVo.getTaskId());
        if (entrustEntity != null) {
            //更新任务单状态为已领样
            taskMapper.updateEntrustById(paramVo.getTaskId(), 2);
        }
        //更新样品状态为领样
//        sampleEntityMapper.updateSampleState(paramVo.getSampleId(),1);
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
        /*// 抢单
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
        }*/
        return false;
    }

    @Override
    public Boolean postGrabASingleTwo(TaskTestEntity taskTestEntity) {
        // state=1 （领取任务单 并领样）。
        taskTestEntity.setState(1);
        // 根据任务单主键 获取委托单主键
        EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
        if (entrustEntity != null) {
            //更新任务单状态为已领样
            taskMapper.updateEntrustById(taskTestEntity.getId(), 2);
        }
        // 抢单时间
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        taskTestEntity.setReceiveTime(currentDate);
        // 通过任务单id 获取详情 任务单下单时间 = 领样时间
        TaskDetailInfoVo taskDetails = taskMapper.getTaskDetailInfo(taskTestEntity.getId());
        // 字符串转日期
        if(!StringUtils.isEmpty(taskDetails.getOrderTime())){
            try {
                Date yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd").parse(taskDetails.getOrderTime());
                // 领样时间
                taskTestEntity.setSampleReceivingTime(yyyy_MM_dd);
            }
            catch (Exception e){
                e.fillInStackTrace();
            }
        }
        //记录日志
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append(" 任务id"+taskTestEntity.getId());
        stringBuilder1.append(" 任务编号:"+taskTestEntity.getCode());
        stringBuilder1.append(" 检测人:"+taskTestEntity.getInspector());
        stringBuilder1.append(" 接收人:"+taskTestEntity.getReceiver());
        stringBuilder1.append(" 复核人:"+taskTestEntity.getReviewer());
        stringBuilder1.append(" 报告制作人:"+taskTestEntity.getReportProducer());
        stringBuilder1.append(" 记录人:"+taskTestEntity.getRecorder());
        stringBuilder1.append(" 样品状态描述:"+taskTestEntity.getSampleStateDescription());
        stringBuilder1.append(" 领样人:"+taskTestEntity.getSampler());
        stringBuilder1.append(" 见习生：实习的新手:"+taskTestEntity.getProbationer());
        stringBuilder1.append(" 实习生:"+taskTestEntity.getInterns());
        stringBuilder1.append(" 辅助人员:"+taskTestEntity.getAuxiliaryPersonnel());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "任务单领取\n\t"+stringBuilder1.toString(), Const.TASK_GET, true);
        taskMapper.updateTestTask(taskTestEntity);
        // 获取任务单id集合 进行更新样品领样状态
        List<Long> taskIds = new ArrayList<>();
        taskIds.add(taskTestEntity.getId());
        List<TaskTestEntity> taskTestEntities = new ArrayList<>();
        taskTestEntities.add(taskTestEntity);
        // 根据任务单id 更新样品状态 = 1
        updateSampleStateMethod(taskIds,taskTestEntities);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchPostGrabASingle(List<TaskTestEntity> taskTestEntitys) {
        for(TaskTestEntity taskTestEntity :taskTestEntitys){
            // 通过任务单id 获取详情 任务单下单时间 = 领样时间
            TaskDetailInfoVo taskDetails = taskMapper.getTaskDetailInfo(taskTestEntity.getId());
            // 字符串转日期
            if(!StringUtils.isEmpty(taskDetails.getOrderTime())){
                try {
                    Date yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd").parse(taskDetails.getOrderTime());
                    // 领样时间
                    taskTestEntity.setSampleReceivingTime(yyyy_MM_dd);
                }
                catch (Exception e){
                    e.fillInStackTrace();
                }
            }
        }
        if(!CollectionUtils.isEmpty(taskTestEntitys)){
            //记录日志
            StringBuilder stringBuilder1 = new StringBuilder();
            for(TaskTestEntity taskTestEntity  : taskTestEntitys){
                stringBuilder1.append(" 任务id"+taskTestEntity.getId());
                stringBuilder1.append(" 任务编号:"+taskTestEntity.getCode());
                stringBuilder1.append(" 检测人:"+taskTestEntity.getInspector());
                stringBuilder1.append(" 接单人:"+taskTestEntity.getReceiver());
                stringBuilder1.append(" 复核人:"+taskTestEntity.getReviewer());
                stringBuilder1.append(" 报告制作人:"+taskTestEntity.getReportProducer());
                stringBuilder1.append(" 记录人:"+taskTestEntity.getRecorder());
                stringBuilder1.append(" 样品状态描述:"+taskTestEntity.getSampleStateDescription());
                stringBuilder1.append(" 领样人:"+taskTestEntity.getSampler());
                stringBuilder1.append(" 见习生：实习的新手:"+taskTestEntity.getProbationer());
                stringBuilder1.append(" 实习生:"+taskTestEntity.getInterns());
                stringBuilder1.append(" 辅助人员:"+taskTestEntity.getAuxiliaryPersonnel());
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "任务单领取\n\t"+stringBuilder1.toString(), Const.TASK_GET, true);
        }
        int i = taskMapper.batchUpdateTestTask(taskTestEntitys);
       if(i==0){
           logger.error("批量修改任务单信息失败！");
       }
        // 获取任务单id集合 进行更新样品领样状态
        List<Long> taskIds = new ArrayList<>();
       for(TaskTestEntity taskTestEntity :taskTestEntitys){
            // 根据任务单主键 获取委托单主键
            EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
           taskIds.add(taskTestEntity.getId());
            if (entrustEntity != null) {
                //更新任务单状态为已领样
                taskMapper.updateEntrustById(taskTestEntity.getId(), 2);
            }
           taskIds.add(taskTestEntity.getId());
        }
       // 根据任务单id 更新样品状态 = 1
        updateSampleStateMethod(taskIds,taskTestEntitys);
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
/*        // 返回团队id集合 根据人员id
        List<TeamTreeStructureEntity> dataDepts = taskMapper.getTeamDeptVo(UserLong);
        List<TeamTreeStructureEntity> dataDepts = new ArrayList<>();
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
        }*/

        /*// 返回团队所有人员信息。*/
        // 获取当前用户所在科室id
        Long department = teamMapper.getTeamIdByUid(UserLong);
        // 获取顶级部门 为空则是当前部门
        Long topDepartment = this.getTopDepartment(department);
        if(StringUtils.isEmpty(topDepartment)){
            topDepartment = department;
        }
        // 获取团队下所有子集团队下技术人员集合
        List<TestTeam> testTeamList = teamMapper.getIdsByTeamId(topDepartment);
        List<LabelValueVo> teamVos = new ArrayList<>();
        // testTeamList =null
        if(CollectionUtils.isEmpty(testTeamList)){
            // 团队id集合 返回人员信息
            Set<Long> deptIds = new HashSet<>();
            deptIds.add(topDepartment);
            List<LabelValueVo> teamVos0 = taskMapper.getMemberInformation(deptIds);
            teamVo.setTeamVo(teamVos0);
        } else {
            for(TestTeam testTeam:testTeamList)
            {
                if(!StringUtils.isEmpty(testTeam)){
                    LabelValueVo labelValueVo = new LabelValueVo();
                    labelValueVo.setLabel(testTeam.getName());
                    labelValueVo.setValue(testTeam.getUserId());
                    teamVos.add(labelValueVo);
                }
            }
            teamVo.setTeamVo(teamVos);
        }
        // 复核人集合

        teamVo.setReviewVo(null);
        // 审批人集合
        List<LabelValueVo> ApproverVo = taskMapper.getRoleInformation(Const.approverLongUserId);
        teamVo.setApproverVo(ApproverVo);
        // 签发人集合
        List<LabelValueVo> SignerVo = taskMapper.getRoleInformation(Const.AuthorizedSignatory);
        teamVo.setSignerVo(SignerVo);
        // 获取科室下人员信息 （一个科室下）
        // 查询用户列表
        List<LabelValueTeamVo> userList = teamMapper.selectUserList();
        teamVo.setUserVo(userList);

        return teamVo;
    }

    /**
     * 返回 团队姓名 通过委托单id下样品名称是否匹配进行过滤
     * @param UserLong
     * @param entrustId
     * @return
     */
    @Override
    public TeamVo getEntrustTeamUserName(Long UserLong,Long entrustId) {
        TeamVo teamVo = new TeamVo();
        // 获取当前用户所在科室id
        Long department = teamMapper.getTeamIdByUid(UserLong);
        // 获取顶级部门 为空则是当前部门
        Long topDepartment = this.getTopDepartment(department);
        if (StringUtils.isEmpty(topDepartment)) {
            topDepartment = department;
        }
        // 获取团队下所有子集团队下技术人员集合
        List<TestTeam> testTeamList = teamMapper.getByTeamId(topDepartment);
        List<LabelValueVo> teamVos = new ArrayList<>();
        // 查询当前登录人 是否为 授权角色
        Boolean flag = false;
        List<SysRoleEntity> roleList = sysRoleDao.selectSysRoleByUserId(UserLong);
        // 用户集合 = 根据角色 == 88 带出用户信息
        Set<Long> userIds = new HashSet<>();
        if (CollectionUtil.isNotEmpty(roleList)) {
            for (SysRoleEntity sysRoleEntity : roleList) {
                if (sysRoleEntity.getRoleId() == 88) {
                    flag = true;
                }
            }
        }
        // 根据 角色Id 带出 用户信息列表
        if (flag) {
            // TODO:10月30日 暂定写死 = 88L
            List<LabelValueVo> userList = sysRoleDao.selectSysyRoleName(88L);
            if (CollectionUtil.isNotEmpty(userList)) {
                for (LabelValueVo labelValueVo : userList) {
                    userIds.add(labelValueVo.getValue());
                }
            }
        }
        // 去重利器
        HashMap<Long,LabelValueVo> map = new HashMap<>();
        // testTeamList =null
        if (CollectionUtils.isEmpty(testTeamList)) {
            // 团队id集合 返回人员信息
            Set<Long> deptIds = new HashSet<>();
            deptIds.add(topDepartment);
            if (CollectionUtil.isNotEmpty(userIds)) {
                List<LabelValueVo> teamVos0 = taskMapper.getMemberInformationConcat(deptIds, userIds);
                teamVo.setTeamVo(teamVos0);
            } else {
                List<LabelValueVo> teamVos0 = taskMapper.getMemberInformationConcat(deptIds, null);
                teamVo.setTeamVo(teamVos0);
            }
        } else {
            if(CollectionUtil.isNotEmpty(userIds)){
                List<LabelValueVo> teamVos1 = taskMapper.getMemberInformationConcat1(userIds);
                if(CollectionUtil.isNotEmpty(teamVos1)){
                    for (LabelValueVo labelValueVo : teamVos1) {
                        map.put(labelValueVo.getValue(),labelValueVo);
                    }
                }
            }
            for (TestTeam testTeam : testTeamList) {
                if (!StringUtils.isEmpty(testTeam)) {
                    LabelValueVo labelValueVo = new LabelValueVo();
                    labelValueVo.setLabel(testTeam.getName());
                    labelValueVo.setValue(testTeam.getUserId());
                    labelValueVo.setText(testTeam.getText());
                    map.put(labelValueVo.getValue(),labelValueVo);
                }
            }
            for(Long key : map.keySet()){
                LabelValueVo labelValueVo = map.get(key);
                teamVos.add(labelValueVo);
            }
            teamVo.setTeamVo(teamVos);
        }
        // 通过委托单获取样品信息
        // 2、 展示每组下样品列表
        List<SampleEntity> sampleList = sampleEntityMapper.selectSampleListGroup(entrustId);
        if (CollectionUtil.isNotEmpty(sampleList)) {
            List<Integer> productList = new ArrayList<>();
            for (SampleEntity sampleEntity : sampleList) {
                productList.add(sampleEntity.getProductId());
            }
            // 通过产品id 获取对应的所属userIds
            List<Long> userList = sampleEntityMapper.selectProductIdsTechnicist(productList);
            if(CollectionUtils.isEmpty(userList)){
                teamVo.setTeamVo(new ArrayList<>());
            }
            // 参与比较
            Map<Long,String> userMap = new HashMap<>();
            if (CollectionUtil.isNotEmpty(teamVo.getTeamVo())) {
                for (Long userId : userList) {
                    for (LabelValueVo labelValueVo : teamVo.getTeamVo()) {
                        if (labelValueVo.getValue().equals(userId)) {
                            userMap.put(userId, "录入人员");
                        }
                    }
                }
                // 迭代数据
                List<LabelValueVo> testUsers = new ArrayList<>();
                for (LabelValueVo valueVo : teamVo.getTeamVo()) {
                    if (userMap.get(valueVo.getValue()) != null) {
                        testUsers.add(valueVo);
                    }
                }
                teamVo.setTeamVo(testUsers);
            }
        }
        return teamVo;
    }

    @Override
    public Boolean getJudgmentTaskList(Long id) {
        if (taskMapper.getJudgmentTaskList(id) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> getSampleOutward(Long taskId) {
        return taskMapper.getSampleOutward(taskId);
    }

    @SneakyThrows
    @Override
    public XWPFDocument downloadEntrust(TaskDetailInfoVo taskDetailInfoVo, InputStream object,Boolean status) {
        XWPFDocument doc = null;
        doc = new XWPFDocument(object);
        List<XWPFTable> tables = doc.getTables();
        //                原材表格逐行赋值
        StringBuilder stringBuilder = new StringBuilder();
        // 检测项信息集合
        Map<String,CheckItemInfoVo> checkItemInfoVoMap = new HashMap<>();
        // 通过任务单 获取对应的任务单中信息。
        TaskListVo taskListVo = taskMapper.selectTaskListDetails(taskDetailInfoVo.getTaskId());
        Integer cost = 0;
        for (int j = 0; j < tables.size(); j++) {
            List<XWPFTableRow> rows = tables.get(j).getRows();
            //表头部分
            Map<String, String> testMap = new HashMap<String, String>();
            // 下单日期
            testMap.put("date", taskDetailInfoVo.getOrderTime());
            // 编号
            testMap.put("number", taskDetailInfoVo.getTaskCode());
            // 发布人
            if(taskListVo.getRecorder()!=null){
                testMap.put("recorder", taskListVo.getRecorder());
            }
            else{
                testMap.put("recorder", "--");
            }
            // 领样人
            if(taskListVo.getSampler()!=null){
                testMap.put("sampler", taskListVo.getSampler().split("&")[0]);
            }else {
                testMap.put("sampler", "--");
            }
            // 领样日期
           if(taskListVo.getSampleReceivingTime()!=null){

               String sampleReceivingTime = DateUtil.longToString(taskListVo.getSampleReceivingTime().getTime()/1000,"yyyy-MM-dd");
               testMap.put("sampleReceivingTime", sampleReceivingTime);
           }else {
               testMap.put("sampleReceivingTime", "--");
           }
            // 样品描述
           if(taskListVo.getSampleStateDescription()!=null){
               testMap.put("sampleStateDescription", taskListVo.getSampleStateDescription());
           }
           else{
               testMap.put("sampleStateDescription", "--");

           }
            //遍历表格,并替换模板
            PoiConfig.eachTable(rows, testMap);
            if (j == 0) {
                //设置样品信息
                List<SampleDetailVo> sampleEntityList = Lists.newArrayList();
                // 遍历 原材样品数据
                List<SampleDetailVo> samples = taskDetailInfoVo.getSampleDetailList();
                // 处理为配合比。
                List<TestSampleEntity> nodeSample = taskDetailInfoVo.getNodeSample();
                if (nodeSample != null && nodeSample.size() > 0) {
                    for (TestSampleEntity node : nodeSample) {
                        SampleEntity entity = new SampleEntity(node);
                        SampleDetailVo sampleDetailVo = new SampleDetailVo();
                        sampleDetailVo.setSampleName(entity.getAliasName());
                        sampleDetailVo.setAliasName(entity.getAliasName());
                        sampleDetailVo.setSpecs(entity.getSpecs());
                        sampleDetailVo.setBatchNumber(entity.getBatchNumber());
                        sampleDetailVo.setSampleQuantity(entity.getSampleQuantity());
                        sampleDetailVo.setGeneration(entity.getGeneration());
                        sampleDetailVo.setSampleOrigin(entity.getSampleOrigin());
                        sampleDetailVo.setSampleRemark(entity.getSampleRemark());
                        sampleDetailVo.setSampleCode(entity.getSampleCode());
                        // 生产厂家/样品产地
                        sampleDetailVo.setManufacturer(entity.getManufacturer());
                        sampleEntityList.add(sampleDetailVo);
                    }
                    samples.addAll(sampleEntityList);
                }
                if (taskDetailInfoVo.getSampleDetailList() == null || taskDetailInfoVo.getSampleDetailList().isEmpty()) {
                    return doc;
                }
                if (samples.size() > 5) {
                    if(status == true){
                        AsposeUtil.addRows(tables.get(0), 4, samples.size() - 6);
                    }else{
                        AsposeUtil.addRows(tables.get(0), 4, samples.size() - 5);
                    }
                    //遍历表格插入数据
                    XWPFTable table1 = tables.get(j);
                    List<XWPFTableRow> rows1 = table1.getRows();
                    for (int i = 1; i < rows1.size(); i++) {
                        List<XWPFTableCell> cells = rows1.get(i).getTableCells();
                        for (int j1 = 0; j1 < cells.size(); j1++) {
                            XWPFTableCell cell = cells.get(j1);

                            // 设置水平居中,需要ooxml-schemas包支持
                            CTTc cttc = cell.getCTTc();
                            CTTcPr ctPr = cttc.addNewTcPr();
                            ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
                            cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
                        }
                    }
                }

                for (int i = 0; i < samples.size(); i++) {
                    SampleDetailVo sampleDetailVo = samples.get(i);
                    // 补充表格数据 样品名称
                    rows.get(i + 2).getTableCells().get(0).setText(sampleDetailVo.getAliasName());
                    // 规格/等级
                    rows.get(i + 2).getTableCells().get(1).setText(StringUtils.isEmpty(sampleDetailVo.getSpecs()) ? "——" : sampleDetailVo.getSpecs());
                    // 批号/编号
                    rows.get(i + 2).getTableCells().get(2).setText(StringUtils.isEmpty(sampleDetailVo.getBatchNumber()) ? "——" : sampleDetailVo.getBatchNumber());
                    // 样品数量
                    rows.get(i + 2).getTableCells().get(3).setText(sampleDetailVo.getSampleQuantity());
                    // 样品产地
                    rows.get(i + 2).getTableCells().get(4).setText("——");
                    //样品编号
                    rows.get(i + 2).getTableCells().get(5).setText(sampleDetailVo.getSampleCode());
                    // 备注
                    rows.get(i + 2).getTableCells().get(6).setText(StringUtils.isEmpty(sampleDetailVo.getSampleRemark()) ? "——" : sampleDetailVo.getSampleRemark());
                    //6月22日 (多组样品有相同的检测项无法预览任务单；产品标准、检测项都要去重展示；没有价格的子检测项目不展示) 废弃
                    //9月2日  检测项名称一致和标准规范一致。进行去重。
                    if(!StringUtils.isEmpty(sampleDetailVo.getCheckItemInfoList())){
                        for(CheckItemInfoVo checkItemInfoVo :sampleDetailVo.getCheckItemInfoList()){
                            checkItemInfoVoMap.put(checkItemInfoVo.getCheckItemName()+String.valueOf(checkItemInfoVo.getStandardId()),
                                    checkItemInfoVo);
                        }
                    }
                }
            }
            // 提供资料
            if (j == 1 && status == false) {
                rows.get(0).getTableCells().get(1).setText(taskDetailInfoVo.getPresentInformation());
                // 取样方式
                rows.get(1).getTableCells().get(1).setText(taskDetailInfoVo.getSamplingMethod());
                // 检验目的
                rows.get(1).getTableCells().get(3).setText(taskDetailInfoVo.getCheckPurpose());
                // 产品标准 去重
                if (!StringUtils.isEmpty(taskDetailInfoVo.getJudgmentBasis())) {
                    String[] arrays = taskDetailInfoVo.getJudgmentBasis().split(",");
                    Set<String> set = new HashSet<>();
                    for (int i = 0; i < arrays.length; i++) {
                        set.add(arrays[i]);
                    }
                    StringBuilder stringBuilder1 = new StringBuilder();
                    for(int x=0; x<set.toArray().length; x++){
                        stringBuilder1.append(set.toArray()[x]);
                        stringBuilder1.append(",");
                    }
                    if(stringBuilder1.length()>2){
                        rows.get(1).getTableCells().get(5).setText(stringBuilder1.deleteCharAt(stringBuilder1.length()-1).toString());
                    }
                }
                // 检测项目及检验依据
                // 6月22日 (多组样品有相同的检测项无法预览任务单；产品标准、检测项都要去重展示；没有价格的子检测项目不展示) 废弃
                // 9月2日  检测项名称一致和标准规范一致。进行去重。
                for (String key : checkItemInfoVoMap.keySet()) {
                    CheckItemInfoVo checkItemInfoVo = checkItemInfoVoMap.get(key);
                    String name = checkItemInfoVo.getCheckItemName();
                    stringBuilder.append(name);
                    if (!StringUtils.isEmpty(checkItemInfoVo.getStandardName())) {
                        stringBuilder.append("（");
                        String s = checkItemInfoVo.getStandardName();
                        String aa = s.split("《")[0];
                        stringBuilder.append(aa);
                        stringBuilder.append("）");
                    }
                    stringBuilder.append("，");
                    // 获取样品的检测项信息
                    checkItemInfoVo.setTimes(checkItemInfoVo.getTimes() != null ? checkItemInfoVo.getTimes() : 0);
                    checkItemInfoVo.setCheckPrice(checkItemInfoVo.getCheckPrice() != null ? checkItemInfoVo.getCheckPrice() : "0");
                    cost += (checkItemInfoVo.getTimes() * Integer.parseInt(checkItemInfoVo.getCheckPrice()));
                }
                String substring = "";
                if(stringBuilder.length()>1){
                    substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                }
                rows.get(2).getTableCells().get(1).setText(substring);
                // 要求检验完成日期
                rows.get(3).getTableCells().get(1).setText(taskDetailInfoVo.getRequiredCompletionTime());
                // 本单产值 6月17日 任务单 test_task 字段 task_price 为准
                if (!StringUtils.isEmpty(taskDetailInfoVo.getCost())) {
                    rows.get(3).getTableCells().get(3).setText(String.valueOf(taskDetailInfoVo.getCost()));
                } else {
                    rows.get(3).getTableCells().get(3).setText(String.valueOf("--"));
                }
                // 检测项目处理 add增加表格。
                // 判断表格 是否大于3
                if(checkItemInfoVoMap.size()>3){
                    AsposeUtil.addRows(tables.get(j), 11, checkItemInfoVoMap.size() - 3);
                    //遍历表格插入数据
                    XWPFTable table1 = tables.get(j);
                    List<XWPFTableRow> rows1 = table1.getRows();
                    for (int i = 1; i < rows1.size(); i++) {
                        List<XWPFTableCell> cells = rows1.get(i).getTableCells();
                        for (int j1 = 0; j1 < cells.size(); j1++) {
                            XWPFTableCell cell = cells.get(j1);

                            // 设置水平居中,需要ooxml-schemas包支持
                            CTTc cttc = cell.getCTTc();
                            CTTcPr ctPr = cttc.addNewTcPr();
                            ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
                            cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
                        }
                    }
                }
                // 塞入数据
                int serialNumber = 9;
                for (String key : checkItemInfoVoMap.keySet()) {
                    CheckItemInfoVo checkItemInfoVo = checkItemInfoVoMap.get(key);
                    rows.get(serialNumber).getTableCells().get(1).setText(checkItemInfoVo.getCheckItemName());
                    serialNumber+=1;
                }
            }
            // 数据：处理 2023年07月01日发布 第二页
            if (j == 1 && status==true) {
                rows.get(0).getTableCells().get(1).setText(taskDetailInfoVo.getPresentInformation());
                // 取样方式
                rows.get(1).getTableCells().get(1).setText(taskDetailInfoVo.getSamplingMethod());
                // 检验目的
                rows.get(1).getTableCells().get(3).setText(taskDetailInfoVo.getCheckPurpose());
                // 产品标准 去重
                if (!StringUtils.isEmpty(taskDetailInfoVo.getJudgmentBasis())) {
                    String[] arrays = taskDetailInfoVo.getJudgmentBasis().split(",");
                    Set<String> set = new HashSet<>();
                    for (int i = 0; i < arrays.length; i++) {
                        set.add(arrays[i]);
                    }
                    StringBuilder stringBuilder1 = new StringBuilder();
                    for(int x=0; x<set.toArray().length; x++){
                        stringBuilder1.append(set.toArray()[x]);
                        stringBuilder1.append(",");
                    }
                    if(stringBuilder1.length()>2){
                        rows.get(1).getTableCells().get(5).setText(stringBuilder1.deleteCharAt(stringBuilder1.length()-1).toString());
                    }
                }
                // 检测项目及检验依据
                // 6月22日 (多组样品有相同的检测项无法预览任务单；产品标准、检测项都要去重展示；没有价格的子检测项目不展示) 废弃
                // 9月2日  检测项名称一致和标准规范一致。进行去重。
                for (String key : checkItemInfoVoMap.keySet()) {
                    CheckItemInfoVo checkItemInfoVo = checkItemInfoVoMap.get(key);
                    String name = checkItemInfoVo.getCheckItemName();
                    stringBuilder.append(name);
                    if (!StringUtils.isEmpty(checkItemInfoVo.getStandardName())) {
                        stringBuilder.append("（");
                        String s = checkItemInfoVo.getStandardName();
                        String aa = s.split("《")[0];
                        stringBuilder.append(aa);
                        stringBuilder.append("）");
                    }
                    stringBuilder.append("，");
                    // 获取样品的检测项信息
                    checkItemInfoVo.setTimes(checkItemInfoVo.getTimes() != null ? checkItemInfoVo.getTimes() : 0);
                    checkItemInfoVo.setCheckPrice(checkItemInfoVo.getCheckPrice() != null ? checkItemInfoVo.getCheckPrice() : "0");
                    cost += (checkItemInfoVo.getTimes() * Integer.parseInt(checkItemInfoVo.getCheckPrice()));
                }
                String substring = "";
                if(stringBuilder.length()>1){
                    substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                }
                rows.get(2).getTableCells().get(1).setText(substring);
                // 要求检验完成日期
                rows.get(3).getTableCells().get(1).setText(taskDetailInfoVo.getRequiredCompletionTime());
                // 本单产值 6月17日 任务单 test_task 字段 task_price 为准
                if (!StringUtils.isEmpty(taskDetailInfoVo.getCost())) {
                    rows.get(3).getTableCells().get(3).setText(String.valueOf(taskDetailInfoVo.getCost()));
                } else {
                    rows.get(3).getTableCells().get(3).setText(String.valueOf("--"));
                }
            }
            // 数据：处理 2023年07月01日发布 第三页
            if (j == 2 && status==true) {
                // 检测项目处理 add增加表格。
                // 判断表格 是否大于3
                if(checkItemInfoVoMap.size()>5){
                    AsposeUtil.addRows(tables.get(j), 8, checkItemInfoVoMap.size() - 5);
                    //遍历表格插入数据
                    XWPFTable table1 = tables.get(j);
                    List<XWPFTableRow> rows1 = table1.getRows();
                    for (int i = 1; i < rows1.size(); i++) {
                        List<XWPFTableCell> cells = rows1.get(i).getTableCells();
                        for (int j1 = 0; j1 < cells.size(); j1++) {
                            XWPFTableCell cell = cells.get(j1);

                            // 设置水平居中,需要ooxml-schemas包支持
                            CTTc cttc = cell.getCTTc();
                            CTTcPr ctPr = cttc.addNewTcPr();
                            ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
                            cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
                        }
                    }
                }
                // 塞入数据
                int serialNumber = 5;
                for (String key : checkItemInfoVoMap.keySet()) {
                    CheckItemInfoVo checkItemInfoVo = checkItemInfoVoMap.get(key);
                    rows.get(serialNumber).getTableCells().get(1).setText(checkItemInfoVo.getCheckItemName());
                    serialNumber+=1;
                }
            }
        }
        return doc;
    }

    @Override
    public OriginalRecordDataVo getOriginalData(Long taskId, Integer sampleId, Integer checkItemId, Integer idItem) {
        //获取委托单信息
        EntrustEntity entrustBaseInfo = taskMapper.getEntrustBaseInfo(taskId);
        //工程名称及工程部位信息去掉不展示
        entrustBaseInfo.setProjectName("/");
        entrustBaseInfo.setProjectPart("/");
        //生成记录编号
        String recordNumber = "JL-"+entrustBaseInfo.getTaskCode();
        //获取样品信息
        TemplateSampleVo sampleVo = sampleEntityMapper.getOriginalSampleInfo(sampleId);
        // 样品来样时间  = 委托时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 签收时间 =委托单受理日期
        sampleVo.setSampleTime(sdf.format(entrustBaseInfo.getAcceptanceDate()));
        // 得到样品信息数据; 分割。
        sampleVo.setSampleName(!StringUtils.isEmpty(sampleVo.getSampleName())?sampleVo.getSampleName() + "；":"/；");
        sampleVo.setSampleNumber(!StringUtils.isEmpty(sampleVo.getSampleNumber())?sampleVo.getSampleNumber() + "；":"/；");
        sampleVo.setSampleQuantity(!StringUtils.isEmpty(sampleVo.getSampleQuantity())?sampleVo.getSampleQuantity() + "；":"/；");
        // 样品描述
        if(!StringUtils.isEmpty(sampleVo.getOutwardDescribe())){
            sampleVo.setSampleDesc(sampleVo.getOutwardDescribe()+"；");
        }
        else {
            sampleVo.setSampleDesc("/；");
        }
        // 规格/等级
        if(!StringUtils.isEmpty(sampleVo.getSpecs())){
            sampleVo.setSpecs(sampleVo.getSpecs()+"；");
        }
        else {
            sampleVo.setSpecs("/；");
        }

        //获取检测依据
        log.debug("执行上一行完成---------------");
        String checkBasis = taskMapper.getCheckBasis(checkItemId, entrustBaseInfo.getId(), sampleId);
        log.debug("执行下一行完成--------------");
        //获取判定依据
        List<String> judgeBasisList = taskMapper.getJudgeBasis(sampleId, entrustBaseInfo.getId());
        StringBuilder judgeBasis = new StringBuilder("");
        if (judgeBasisList != null && !judgeBasisList.isEmpty()) {
            for (int i = 0; i < judgeBasisList.size(); i++) {
                judgeBasis.append(judgeBasisList.get(i));
                if(i != judgeBasisList.size()-1 ){
                    judgeBasis.append("\n");
                }
            }
        }
        StringBuilder sampleTime = new StringBuilder(sampleVo.getSampleTime() + ";");

        //补充原材信息
        if(sampleVo.getSampleType().contains("配合比")){
            List<SampleEntity> sampleEntities = sampleEntityMapper.selectByPid(sampleId);
            for (SampleEntity sampleEntity : sampleEntities) {
                sampleTime.append("样品名称：");
                sampleTime.append(sampleEntity.getAliasName()== null ? "——" :sampleEntity.getAliasName());
                sampleTime.append("；");
                sampleTime.append("样品编号：");
                sampleTime.append(sampleEntity.getSampleCode()== null ? "——" :sampleEntity.getSampleCode());
                sampleTime.append("；");
                sampleTime.append("样品数量：");
                sampleTime.append(sampleEntity.getSampleQuantity()== null ? "——": sampleEntity.getSampleQuantity());
                sampleTime.append("；");
                sampleTime.append("样品描述：");
                sampleTime.append(sampleEntity.getOutwardDescribe()== null ? "——": sampleEntity.getOutwardDescribe());
                sampleTime.append("；");
                sampleTime.append("来样时间：");
                sampleTime.append(sampleEntity.getSpecs()== null ? "——": sampleEntity.getSpecs());
                sampleTime.append("；");
                sampleTime.append("规格尺寸：");
                // 签收时间 = 委托单受理日期
                sampleEntity.setReceivedDate(sdf.format(entrustBaseInfo.getAcceptanceDate()));
                sampleTime.append(sampleEntity.getReceivedDate());
            }
        }
        sampleVo.setSampleTime(sampleTime.toString());
        OriginalRecordDataVo result = new OriginalRecordDataVo(recordNumber, entrustBaseInfo, sampleVo, checkBasis, judgeBasis.toString());
        // 检测项 开始检测日期。

        // 所使用的设备仪器。
        List<TestInstrumentEntity> instrumentEntityList = taskMapper.getInstrumentEntityList(idItem);
        if (instrumentEntityList != null && !instrumentEntityList.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < instrumentEntityList.size(); i++) {
                stringBuilder.append(instrumentEntityList.get(i).getModel());
                stringBuilder.append(instrumentEntityList.get(i).getName());
                stringBuilder.append("（");
                stringBuilder.append(instrumentEntityList.get(i).getCode());
                stringBuilder.append("）");
                if(i != instrumentEntityList.size()-1){
                    stringBuilder.append("、");
                }
            }
            result.setEquipment(stringBuilder.toString());
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
    @Transactional(rollbackFor = Exception.class)
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
            if(!StringUtils.isEmpty(upload)){
                String[] arrays = upload.split("\\?");
                upload = arrays[0];
            }
            fileUrlStr = entrustBaseInfo.getId() + "-" + paramVo.getSampleId() + "-" + paramVo.getCheckItemId() + "." + strings[strings.length - 1];
            fileName = strings[0] + "." + strings[strings.length - 1];
        }
        // 根据任务单主键 获取委托单主键
        if (entrustBaseInfo != null) {
            if (entrustBaseInfo.getState() < 5) {
                taskMapper.updateEntrustById(entrustBaseInfo.getId(), 5);
            }
        }
        //记录日志
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append(" 检测项id"+sampleItemInstrumentEntity.getItemId());
        stringBuilder1.append(" 检测项附件名:"+fileUrlStr + ":" + fileName);
        stringBuilder1.append(" 附件链接:"+ upload );
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "上传附件\n\t"+stringBuilder1.toString(), Const.TASK_GET, true);
        return taskMapper.updateOriginalFile(upload, entrustBaseInfo.getId(), paramVo.getSampleId(), paramVo.getCheckItemId(), fileUrlStr, fileName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadingBatch(List<Integer> ids, MultipartFile file) {
        List<SampleItemInstrumentEntity> entityList = new ArrayList<>();
        for (Integer id : ids) {
            SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
            String upload = "";
            String fileUrlStr = "";
            // 原始名称
            String fileName = "";
            long fileUrlLongId = GenID.getID();
            if (file != null) {
                String name = file.getOriginalFilename();
                String[] strings = name.split("\\.");
                upload = MinIoUtil.upload("upload-original-record", file, fileUrlLongId + "." + strings[strings.length - 1]);
                if(!StringUtils.isEmpty(upload)){
                    String[] arrays = upload.split("\\?");
                    upload = arrays[0];
                }
                fileUrlStr = fileUrlLongId + "." + strings[strings.length - 1];
                fileName = strings[0] + "." + strings[strings.length - 1];
            }
            sampleItemInstrumentEntity.setItemId(id);
            sampleItemInstrumentEntity.setOriginUrl(upload);
            sampleItemInstrumentEntity.setFileUrlStr(fileUrlStr);
            sampleItemInstrumentEntity.setItemFileName(fileName);
            entityList.add(sampleItemInstrumentEntity);
        }

        if(!CollectionUtils.isEmpty(entityList)){
            //记录日志
            StringBuilder stringBuilder1 = new StringBuilder();
            for(SampleItemInstrumentEntity sampleItemInstrumentEntity : entityList){
                stringBuilder1.append(" 检测项id"+sampleItemInstrumentEntity.getItemId());
                stringBuilder1.append(" 检测项附件名:"+sampleItemInstrumentEntity.getFileUrlStr() + ":" + sampleItemInstrumentEntity.getItemFileName());
                stringBuilder1.append(" 附件链接:"+ sampleItemInstrumentEntity.getOriginUrl() );
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "上传附件\n\t"+stringBuilder1.toString(), Const.TASK_GET, true);
        }
        taskMapper.updateTestEntrustedSampleCheckitemRel(entityList);
        return true;
    }

    @Override
    public Boolean effectDataSet(List<Integer> ids) {
        List<SampleItemInstrumentEntity> list = testDetectionDao.getTestEntrustedSampleCheckitemRelDetailList(ids);
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : list) {
            if (sampleItemInstrumentEntity.getOriginUrl() != null) {
                return false;
            }
        }
        return true;
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
    @Transactional(rollbackFor = Exception.class)
    public String passorno(Integer itemId, Integer state, String opinion, Long userId) {
        // 驳回=4，通过=3，撤回=1
        if (state != null) {
            if (state == 1) {
                SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
                if(sampleItemInstrumentEntity2.getState() == 3){
                    return "撤回失败！当前检测项 已经复核通过";
                }
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
                // 撤回后保留设备仪器
                //testDetectionDao.deleteInstrument(itemId);
                //记录日志
                StringBuilder stringBuilder1 = new StringBuilder();
                stringBuilder1.append(" 检测项id"+sampleItemInstrumentEntity.getItemId());
                stringBuilder1.append(" 检测项附件清除:");
                stringBuilder1.append(" 检测项开始时间清除:");
                stringBuilder1.append(" 检测项结束时间清除:");
                stringBuilder1.append(" 检测项描述信息:"+sampleItemInstrumentEntity.getOpinion());
                //stringBuilder1.append(" 删除设备仪器记录:");
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-驳回\n\t"+stringBuilder1.toString(), Const.TASK_TEST, true);
                return "撤回成功，检测项回到初始状态";
            }
            if (state == 3) {
                // 检测项复核通过
                taskMapper.updateState(itemId, 3, opinion,userId);
                //记录日志
                StringBuilder stringBuilder1 = new StringBuilder();
                stringBuilder1.append(" 检测项id"+itemId);
                stringBuilder1.append(" 检测项描述信息:"+opinion);
                stringBuilder1.append(" 检测项状态: " + state);
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-复核通过\n\t"+stringBuilder1.toString(), Const.TASK_TEST, true);
                SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
                if (sampleItemInstrumentEntity2 != null && sampleItemInstrumentEntity2.getEntrustId() != null) {
                    EntrustAddVo entrustBaseInfo = entrustEntityMapper.selectByKeyId(sampleItemInstrumentEntity2.getEntrustId());
                    if (entrustBaseInfo.getState() != null && entrustBaseInfo.getState() < 6) {
                        taskMapper.updateEntrustById(entrustBaseInfo.getId(), 6);
                    }
                }
                // 通过委托单id 和部门ID为条件  遍历（判断每个状态 state = 3 复核通过。 改变任务单 6 否则 任务单还是为试验完成） 去除 检测项单价为空
                List<Integer> states = testDetectionDao.getSampleCheckitemRelDetailState(sampleItemInstrumentEntity2.getEntrustId(), sampleItemInstrumentEntity2.getDeptId());
                for (Integer stateItem : states) {
                    if (stateItem != 3) {
                        return "当前任务单下检测项未全部复核成功";
                    }
                }
                Long testTaskId = sampleItemInstrumentEntity2.getTaskId();
                TaskTestEntity taskTestEntity = new TaskTestEntity();
                taskTestEntity.setId(testTaskId);
                taskTestEntity.setState(6);
                // 任务单 复核成功 记录复核时间。
                taskTestEntity.setReviewTime(new Date(System.currentTimeMillis()));
                //记录日志
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(" 任务单id"+testTaskId);
                stringBuilder2.append("  任务单复核时间 :"+new Timestamp(taskTestEntity.getReviewTime().getTime()));
                stringBuilder2.append(" 任务单状态: " + state);
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-任务单复核成功\n\t"+stringBuilder2.toString(), Const.TASK_TEST, true);
                taskMapper.updateTestTask(taskTestEntity);
                return "任务单复核成功";
            }
        }
        //记录日志
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(" 检测项id"+itemId);
        stringBuilder2.append(" 检测项描述信息:"+opinion);
        stringBuilder2.append(" 检测项状态: " + state);
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-检测项状态驳回\n\t"+stringBuilder2.toString(), Const.TASK_TEST, true);
        // state = 4 检测项状态驳回
        int status = taskMapper.updateState(itemId, state, opinion,null);
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
        if(!StringUtils.isEmpty(sampleItemInstrumentEntity2.getStartTime())){
            sampleItemInstrumentEntity.setStartTime(sampleItemInstrumentEntity2.getStartTime());
        }
        sampleItemInstrumentEntity.setItemId(itemId);
        sampleItemInstrumentEntity.setOriginUrl(null);
        sampleItemInstrumentEntity.setFileUrlStr(null);
        if(!StringUtils.isEmpty(sampleItemInstrumentEntity2.getEndTime())){
            sampleItemInstrumentEntity.setEndTime(sampleItemInstrumentEntity2.getEndTime());
        }
        sampleItemInstrumentEntity.setItemFileName(null);
        //记录日志
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(" 检测项id"+itemId);
        stringBuilder2.append(" 检测项附件清除:"+sampleItemInstrumentEntity2.getOriginUrl());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-删除附件\n\t"+stringBuilder2.toString(), Const.TASK_TEST, true);
        testDetectionDao.updateTaskPassorno(sampleItemInstrumentEntity);
        return "成功";
    }

    @Override
    public PersonInfoVo getPersonInfo(Long taskId) {
        return taskMapper.getPersonInfo(taskId);
    }

    @Override
    public int updatePersonInfo(PersonInfoVo vo) {
        //记录日志
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append(" 任务id"+vo.getTaskId());
        stringBuilder1.append(" 检测人:"+vo.getInspector());
        stringBuilder1.append(" 记录人:"+vo.getRecorder());
        stringBuilder1.append(" 复核人:"+vo.getReviewer());
        stringBuilder1.append(" 报告制作人:"+vo.getReportProducer());
        stringBuilder1.append(" 记录人:"+vo.getRecorder());
        stringBuilder1.append(" 样品状态描述:"+vo.getSampleStateDescription());
        stringBuilder1.append(" 领样人:"+vo.getSampler());
        stringBuilder1.append(" 见习生：实习的新手:"+vo.getProbationer());
        stringBuilder1.append(" 实习生:"+vo.getInterns());
        stringBuilder1.append(" 辅助人员:"+vo.getAuxiliaryPersonnel());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "任务单修改\n\t"+stringBuilder1.toString(), Const.TASK_GET, true);
        return taskMapper.updatePersonInfo(vo);
    }

    /**
     * 扩展模板样品行列
     *
     * @param table            原始表格
     * @param rows             原始表格行数
     * @param sampleDetailList 待处理数据
     * @param modelSampleRows  需要新增行
     * @param columns          列数
     */
    public List<XWPFTableRow> extendTable(XWPFTable table, List<XWPFTableRow> rows, List<SampleDetailVo> sampleDetailList,
                                          int modelSampleRows, int columns) {
        //获取表格对应的行
        rows = table.getRows();
        // 判断表格行数 >5 sampleDetailList.size()
        // 补充增加表格数量。
        if (sampleDetailList.size() > modelSampleRows) {
            // 3.25 测试表格插入数据
            int addRows = sampleDetailList.size() - modelSampleRows;
            // 表格插入
            XWPFDocument doc1 = new XWPFDocument();
            XWPFTable newTable = doc1.createTable(addRows, columns);  //2行7格
            // 创建表格后直接进行存放 后续多余数据
            List<XWPFTableRow> dataTable = newTable.getRows();
            int j = 0;
            for (int i = modelSampleRows; i < sampleDetailList.size(); i++) {
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
            rows = table.getRows();
        }
        return rows;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String batchReview(TaskStatsVo taskStatsVo) {
        // 批量操作。
        List<TaskStatsItemVo> list = new ArrayList<>();
        // 检测项id
        Integer itemId =0;
        for(int i=0;i<taskStatsVo.getIntegers().length;i++){
            TaskStatsItemVo taskStatsItemVo = new TaskStatsItemVo();
            taskStatsItemVo.setItemId(taskStatsVo.getIntegers()[i]);
            taskStatsItemVo.setState(taskStatsVo.getState());
            taskStatsItemVo.setRemark(taskStatsVo.getRemark()!=null?taskStatsVo.getRemark():"--");
            list.add(taskStatsItemVo);
            itemId = taskStatsVo.getIntegers()[i];
        }
        if(!CollectionUtils.isEmpty(list)){
            //记录日志
            StringBuilder stringBuilder1 = new StringBuilder();
            for(TaskStatsItemVo taskStatsItemVo :list){
                stringBuilder1.append(" 检测项id"+taskStatsItemVo.getItemId());
                stringBuilder1.append(" 检测项描述信息:"+taskStatsItemVo.getRemark());
                stringBuilder1.append(" 检测项状态: " + taskStatsItemVo.getState());
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-复核\n\t"+stringBuilder1.toString(), Const.TASK_TEST, true);
        }
        taskMapper.batchReview(list);
        // 通过任务单 获取检测项状态 =3 通过状态
        if(taskStatsVo.getState()==3){
            SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
            List<Integer> states = taskMapper.selectCheckItemState(taskStatsVo.getTaskId(),sampleItemInstrumentEntity2.getDeptId());
            for (Integer stateItem : states) {
                if (stateItem != 3) {
                    return "当前任务单下检测项未全部复核成功";
                }
            }
            // 修改test_task state 状态 为6：
            TaskTestEntity taskTestEntity = new TaskTestEntity();
            taskTestEntity.setId(taskStatsVo.getTaskId());
            taskTestEntity.setState(6);
            // 任务单 复核成功 记录复核时间。
            taskTestEntity.setReviewTime(new Date(System.currentTimeMillis()));
            //记录日志
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(" 任务单id"+taskStatsVo.getTaskId());
            stringBuilder2.append("  任务单复核时间 :"+new Timestamp(taskTestEntity.getReviewTime().getTime()));
            stringBuilder2.append(" 任务单状态: " + taskTestEntity.getState());
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-任务单复核成功\n\t"+stringBuilder2.toString(), Const.TASK_TEST, true);
            taskMapper.updateTestTask(taskTestEntity);
            return "任务单复核成功";
        }
        return "成功";
    }

    @Override
    public ZipOutputStream packagingWorkbookZip(List<TaskIdEntity> dataEntitys, HttpServletResponse response,Long taskId) throws IOException {
        // 通过输入参数 返回 对应的处理成功的EXCEL数据。
        ServletOutputStream outputStream = response.getOutputStream();
        ZipOutputStream out = new ZipOutputStream(outputStream);

        // 批量获取 检测项id（有可能对应多个模板） 再进行填充。
        // 通过检测项id 获取 相应的 id关联信息。
        for(int i=0; i<dataEntitys.size(); i++ ){
            TaskIdEntity data = dataEntitys.get(i);
            // 有序信息。
            OriginalRecordDataVo originalData = getOriginalData(data.getTaskId(), data.getSampleId(),data.getCheckItemId(), data.getIdItem());
            Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
            result.put("result", originalData);

            // 根据单个Workbook 进行处理打包。
            HttpServletResponse response1 = null;
            // 原始记录模板 比对信息
            try {
                // 链接 get minIo 检查是否存在
                String[] split = data.getFileUrl().split("/");
                String[] split1 = split[4].split("\\?");
                XLSTransformer transformer = new XLSTransformer();
                InputStream fileStream = MinIoUtil.getFileStream("file-resources", split1[0]);
                Workbook workbook = methodPlugTheData(data.getFileUrl(),result, null,dataEntitys.get(0).getTaskId());
                /**
                 * TODD:7月5日 原始记录命名规则
                 * 任务单号+模板名称，如果有重复的，后面加序号
                 */
                SampleServiceImpl.DealWithZip(workbook, data.getTaskCode()+data.getOriginalName()+(i+1)+".xls", out);
            }
            catch (Exception e){
                log.info("输出异常\t"+e);
            }

        }
        // 关闭输入流
        out.closeEntry();
        if (out != null) {
            out.flush();
            out.close();
        }
        return out;
    }

    /**
     * 查询任务列表
     * @param paramVo
     * @param deptIds
     * @return
     */
    @Override
    public PagingToolVo getTaskList(TaskListParamVo paramVo, String[] deptIds) {
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
        List<TaskListVo> personList = new ArrayList<>();
        if (paramVo.getState() != null && paramVo.getState() != 1) {
//            PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
            PageHelper.clearPage();
            personList = taskMapper.getTaskListContainsSample(paramVo);
        }
        else {
            PageHelper.clearPage();
            personList = taskMapper.getTaskListTwoGreater(paramVo);
        }
        // 手动分页
       Integer pageNum =  paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        PagingToolVo pagingVo = new PagingToolVo();
        if(pageNum>0&&pageSize>0) {
            // 总条数
            pagingVo.setTotal(personList.size());
            // 开始
            pagingVo.setPageNum(pageNum);
            // 页码
            pagingVo.setPageSize(pageSize);
            // 当前页展示数量
            Integer size =  personList.size() - (pageNum*pageSize); // 实际返回页码展示数量
            if(size>0){
                pagingVo.setSize(pageSize);
            }
            else {
                size =pageSize -( pageNum*pageSize - personList.size());
                pagingVo.setSize(size>0?size:0);
            }
            // 总页数
            pagingVo.setPages(personList.size() / pageSize);
            // 开始行数
            pagingVo.setStartRow(personList.size() / pageSize / pageNum);
            // 结束行数
            pagingVo.setEndRow(personList.size() / pageSize);
            List<TaskListVo> subList = Lists.newArrayList();
            if (!CollectionUtils.isEmpty(personList)) {
                try {
                    if (personList.size() > 10 && personList.size() / 10 >= pageNum) {
                        subList = personList.subList((pageNum - 1) * pageSize, pageNum * pageSize);
                    } else {
                        subList = personList.subList((pageNum - 1) * pageSize, personList.size());
                    }
                } catch (IndexOutOfBoundsException e) {
                    subList = personList;
                } catch (IllegalArgumentException e) {
                    subList = personList.subList(0, personList.size());
                } finally {
                    // 返回数据
                    pagingVo.setList(subList);
                }
            }
            // 分页后 逻辑处理
            methodManualPages((List<TaskListVo>) pagingVo.getList());
            return pagingVo;
        } else {
            if (!CollectionUtils.isEmpty(personList)) {
                pagingVo.setList(personList);
            }
            // 分页后 逻辑处理
            methodManualPages((List<TaskListVo>) pagingVo.getList());
            return pagingVo;
        }

    }

    /**
     * 根据任务单id 判断 委托状态等于 144 返回true
     * @param id
     * @return
     */
    @Override
    public Boolean judgeTaskStatus(Long id) {
        // 委托单 == 144 || 任务单单自身 == 144 ：结果返回 = true
        Integer state = taskMapper.getJudgmentTaskList(id);
        if(state!=null && state==144){
            return true;
        }
      return  taskMapper.judgeTaskStatus(id);
    }

    @Override
    public List<LabelValueVo> getDeviceUser(Long userId) {
        // 获取当前用户所在科室id
        Long department = teamMapper.getTeamIdByUid(userId);
        // 获取顶级部门 为空则是当前部门
        Long topDepartment = this.getTopDepartment(department);
        if(StringUtils.isEmpty(topDepartment)){
            topDepartment = department;
        }
        // 获取团队下所有子集团队下技术人员集合
        List<TestTeam> testTeamList = teamMapper.getIdsByTeamId(topDepartment);
        List<LabelValueVo> teamVos = Lists.newArrayList();
        if(CollectionUtils.isEmpty(testTeamList)){
            // 团队id集合 返回人员信息
            Set<Long> deptIds = new HashSet<>();
            deptIds.add(topDepartment);
            List<LabelValueVo> teamVos0 = taskMapper.getMemberInformation(deptIds);
            return teamVos0;
        } else {
            for(TestTeam testTeam:testTeamList) {
                if(!StringUtils.isEmpty(testTeam)){
                    LabelValueVo labelValueVo = new LabelValueVo();
                    labelValueVo.setLabel(testTeam.getName());
                    labelValueVo.setValue(testTeam.getUserId());
                    teamVos.add(labelValueVo);
                }
            }
            return teamVos;
        }
    }

    @Override
    public Long getEntrustIdByTaskId(Long taskId) {
        return taskMapper.getEntrustIdByTaskId(taskId);
    }
    /**
     * 如果原始记录文件不为空 塞数据
     */
    public Workbook methodPlugTheData(String originalTemplate,Map<String, OriginalRecordDataVo> result,HttpServletResponse response,Long taskId) throws InvalidFormatException {
        String[] split = originalTemplate.split("/");
        String[] split1 = split[4].split("\\?");
        XLSTransformer transformer = new XLSTransformer();
        InputStream fileStream = MinIoUtil.getFileStream("file-resources", split1[0]);
        org.apache.poi.ss.usermodel.Workbook workbook = null;
        workbook = transformer.transformXLS(fileStream, result);
        //处理原始记录下载，单位名称问题
        java.sql.Date date = entrustEntityMapper.getEntrustDateByTaskId(taskId);
        String dayString = DateUtil.getDayString(date.getTime());
        if (Integer.parseInt(dayString) >= 20230313){
            //河南交科院检验检测认证有限公司
            Sheet sheet = workbook.getSheetAt(0);
            //确定要处理的行
            int rowStart = sheet.getFirstRowNum();
            int rowEnd = sheet.getLastRowNum();
            Boolean flag = false;
            for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
                Row r = sheet.getRow(rowNum);
                if (r == null) {
                    continue;
                }
                int lastColumn = r.getLastCellNum();
                for (int cn = 0; cn < lastColumn; cn++) {
                    Cell c = r.getCell(cn, Row.RETURN_BLANK_AS_NULL);
                    if (c != null){
                        String cellValue = c.getStringCellValue();
                        if ("检测单位名称：河南省公路工程试验检测中心有限公司".equals(cellValue)){
                            c.setCellValue("检测单位名称：河南交科院检验检测认证有限公司");
                            flag = true;
                            break;
                        }
                    }
                }
                if (flag){
                    break;
                }
            }
        }
        return workbook;
    }

    /**
     * 手动分页后 针对分页后数据 进行业务处理
     * @param dataList
     */
    public void methodManualPages(List<TaskListVo> dataList){
        if(!CollectionUtils.isEmpty(dataList)){
            // 处理任务单 与信息。
            //TODO gjl添加样品状态
            EntrustServiceImpl service = new EntrustServiceImpl();
            for (TaskListVo sampleListVo : dataList) {
                //TODO dlc 补充任务单价格
                if(StringUtils.isEmpty(sampleListVo.getCost())){
                    sampleListVo.setCost("--");
                }
                List<SamplePrivateInfoVo> sampleList = sampleListVo.getSampleList();
                List<SamplePrivateInfoVo> nodeSampleList = Lists.newArrayList();
                //外观描述
                StringBuilder outward = new StringBuilder();
                if(!CollectionUtils.isEmpty(sampleList)) {
                    int i = 0;
                    for (SamplePrivateInfoVo samplePrivateInfoVo : sampleList) {
                        String state = service.findStateBySampleId(samplePrivateInfoVo.getId(), entrustEntityMapper, taskMapper);
                        samplePrivateInfoVo.setState(state);
                        //TODO PSH查询子原材样品信息
                        List<SamplePrivateInfoVo> nodeSampleList1 = taskMapper.getNodeSampleList(samplePrivateInfoVo.getId());
                        if (!CollectionUtils.isEmpty(nodeSampleList1)) {
                            nodeSampleList.addAll(nodeSampleList1);
                        }
                        if(!StringUtils.isEmpty(samplePrivateInfoVo.getOutward())){
                            outward.append(samplePrivateInfoVo.getOutward());
                        }
                        if(i != sampleList.size()-1){
                            outward.append("/");
                        }
                        i++;
                    }
                    sampleList.addAll(nodeSampleList);
                }
                //将多组样品放到领取任务中；
                sampleListVo.setOutward(outward.toString());
                //增加关联委托单信息
                StringBuilder correlationTask = new StringBuilder();
                List<String> correlationTaskList = taskMapper.getCorrelationTask(sampleListVo.getTaskId());
                if(CollectionUtils.isEmpty(correlationTaskList)){
                    correlationTask.append("——");
                }else{
                    for (int i = 0; i < correlationTaskList.size(); i++) {
                        correlationTask.append(correlationTaskList.get(i));
                        if(i!=correlationTaskList.size()-1){
                            correlationTask.append("\n");
                        }
                    }
                }
                sampleListVo.setCorrelationTaskCode(correlationTask.toString());
                // 通过任务单id 补充 任务流转日期信息
                List<TestEntrustedTaskRelEntity> TaskList = Lists.newArrayList();
                TaskList = testEntrustedTaskRelDao.getTaskList(sampleListVo.getTaskId(),sampleListVo.getEntrustmentId());
                if(!CollectionUtils.isEmpty(TaskList)){
                    StringBuilder stringBuilder = new StringBuilder();
                    StringBuilder remark = new StringBuilder();
                    for(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity:TaskList){
                        stringBuilder.append(DateUtil.formatDate(testEntrustedTaskRelEntity.getTaskFlowDate()));
                        stringBuilder.append("、");
                        if(StringUtils.isEmpty(testEntrustedTaskRelEntity.getRemark())){
                            remark.append("(");
                            remark.append("——");
                            remark.append(")");
                            remark.append("、");
                        }else {
                            remark.append("(");
                            remark.append(testEntrustedTaskRelEntity.getRemark());
                            remark.append(")");
                            remark.append("、");
                        }
                    }
                    if(stringBuilder.length()>1){
                        sampleListVo.setTaskFlowDate(stringBuilder.toString()+
                                remark.deleteCharAt(remark.length()-1).toString());
                    }
                    else{
                        sampleListVo.setTaskFlowDate("——");
                    }
                }
                else {
                    sampleListVo.setTaskFlowDate("——");
                }
            }
        }
    }

    /**
     * 获取顶级部门id
     * @return
     */
    public Long getTopDepartment(Long id){
        Long topId = null;
        List<TreeEntity> treeList = com.google.api.client.util.Lists.newArrayList();
        List<TestTeam> list = teamMapper.getTopDepartment(id);
        if (list.size() == 0){
            return id;
        }
        if (list.size() == 1){
            topId = Long.valueOf(list.get(0).getId()+"");
        }else {
            for (TestTeam team:list) {
                TreeEntity entity = new TreeEntity();
                entity.setId(team.getId()+"");
                entity.setPid(team.getPid()+"");
                treeList.add(entity);
            }
            //获取最顶级部门id
            List<TreeEntity> treeData = ConvertUtil.list2TreeList(treeList,"id","pid","children");
            topId = Long.valueOf(treeData.get(0).getId());
        }
        return topId;
    }

    /**
     *  返回原始记录
     * List 检测项主键
     * CheckReview 类型（中间复核 或 最终复核）
     * @return
     */
    @Override
    public  XSSFWorkbook getOriginalRecordAttachment(ExcelInsertVo excelInsertVo) throws IOException {
        Integer[] ids =new Integer[excelInsertVo.getList().size()];
        for(int i = 0; i<excelInsertVo.getList().size(); i++){
            ids[i] = excelInsertVo.getList().get(i);
        }
        // 通过检测项主键 获取样品生成附件是否存在。
        String productExcelUrl = testProductItemDao.getProductExcelUrl(excelInsertVo.getList().get(0));
        InputStream fileStream = null;
        // 获取公网 附件
        try {
            fileStream = FileAndFolderUtil.getInputStream(productExcelUrl);
        } catch (Exception e) {
            logger.info("样品附件 " + productExcelUrl + e);
        }
        if(fileStream == null){
            return null;
        }
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        Map<String, String> mapSheet = new HashMap<>();
        // 循环遍历所有工作表
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            // 获取第i个工作表
            XSSFSheet sheet = wb.getSheetAt(i);
            if (sheet != null) {
                // 获取工作表的名称
                String sheetName = sheet.getSheetName();
                mapSheet.put(sheetName, sheetName);
            }
        }
        // 查询检测项对应的 sheet下标
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(ids);
        // 获取 sheetName
        Map<String, Object> map = new HashMap<>();
        // 根据key 保证 sheet不重复使用。
        Map<String, String> keyMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(sheetItems)) {
            for (ExcelInsertVo excelInsertVo1 : sheetItems) {
                // 获取sheetIndex工作表
                XSSFSheet sheet = wb.getSheetAt(excelInsertVo1.getSheetIndex());
                if (sheet != null) {
                    //获取工作表的名称
                    String sheetName = sheet.getSheetName();
                    if (keyMap.get(sheetName) == null) {
                        keyMap.put(sheetName, sheetName);
                    }
                }
            }
        }
        for (String key : keyMap.keySet()) {
            XSSFSheet sheet = wb.getSheet(key);
            if (sheet != null) {
                // 设置全部可读
                wb.getSheet(key).setVerticallyCenter(true);
                map.put(key, 0);
            }
        }
        // sheetName 不包含 则清除
        ExcelReplaceUtil.removeSheetName(map,wb);
        fileStream.close();
        return wb;
    }

    @Override
    public Boolean getVerifyReportState(Long taskId) {

        List<Integer> list = taskMapper.getVerifyReportState(taskId);
        if(CollectionUtils.isEmpty(list)){
            return false;
        }
        Boolean status = true;
        for(Integer state : list){
            if(state<7){
                status = false;
            }
        }
        return status;
    }

    @Override
    public XWPFDocument downloadEntrustNew(TaskDetailInfoVo taskDetailInfoVo, InputStream object) throws IOException {
        XWPFDocument doc = null;
        doc = new XWPFDocument(object);
        List<XWPFTable> tables = doc.getTables();
        //                原材表格逐行赋值
        StringBuilder stringBuilder = new StringBuilder();
        // 检测项信息集合
        Map<String,CheckItemInfoVo> checkItemInfoVoMap = new HashMap<>();
        // 通过任务单 获取对应的任务单中信息。
        TaskListVo taskListVo = taskMapper.selectTaskListDetails(taskDetailInfoVo.getTaskId());
        Integer cost = 0;
        for (int j = 0; j < tables.size(); j++) {
            List<XWPFTableRow> rows = tables.get(j).getRows();
            //表头部分
            Map<String, String> testMap = new HashMap<String, String>();
            // 下单日期
            testMap.put("date", taskDetailInfoVo.getOrderTime());
            // 编号
            testMap.put("number", taskDetailInfoVo.getTaskCode());
            // 发布人
            if(taskListVo.getRecorder()!=null){
                testMap.put("recorder", taskListVo.getRecorder());
            }
            else{
                testMap.put("recorder", "--");
            }
            // 领样人
            if(taskListVo.getSampler()!=null){
                testMap.put("sampler", taskListVo.getSampler().split("&")[0]);
            }else {
                testMap.put("sampler", "--");
            }
            // 领样日期
            if(taskListVo.getSampleReceivingTime()!=null){
                String sampleReceivingTime = DateUtil.longToString(taskListVo.getSampleReceivingTime().getTime()/1000,"yyyy-MM-dd");
                testMap.put("sampleReceivingTime", sampleReceivingTime);
            }else {
                testMap.put("sampleReceivingTime", "--");
            }
            // 样品描述
            if(taskListVo.getSampleStateDescription()!=null){
                testMap.put("sampleStateDescription", taskListVo.getSampleStateDescription());
            }
            else{
                testMap.put("sampleStateDescription", "--");
            }
            //遍历表格,并替换模板
            PoiConfig.eachTable(rows, testMap);
            if (j == 0) {
                //设置样品信息
                List<SampleDetailVo> sampleEntityList = Lists.newArrayList();
                // 遍历 原材样品数据
                List<SampleDetailVo> samples = taskDetailInfoVo.getSampleDetailList();
                // 处理为配合比。
                List<TestSampleEntity> nodeSample = taskDetailInfoVo.getNodeSample();
                if (nodeSample != null && nodeSample.size() > 0) {
                    for (TestSampleEntity node : nodeSample) {
                        SampleEntity entity = new SampleEntity(node);
                        SampleDetailVo sampleDetailVo = new SampleDetailVo();
                        sampleDetailVo.setSampleName(entity.getAliasName());
                        sampleDetailVo.setAliasName(entity.getAliasName());
                        sampleDetailVo.setSpecs(entity.getSpecs());
                        sampleDetailVo.setBatchNumber(entity.getBatchNumber());
                        sampleDetailVo.setSampleQuantity(entity.getSampleQuantity());
                        sampleDetailVo.setGeneration(entity.getGeneration());
                        sampleDetailVo.setSampleOrigin(entity.getSampleOrigin());
                        sampleDetailVo.setSampleRemark(entity.getSampleRemark());
                        sampleDetailVo.setSampleCode(entity.getSampleCode());
                        // 生产厂家/样品产地
                        sampleDetailVo.setManufacturer(entity.getManufacturer());
                        sampleEntityList.add(sampleDetailVo);
                    }
                    samples.addAll(sampleEntityList);
                }
                if (taskDetailInfoVo.getSampleDetailList() == null || taskDetailInfoVo.getSampleDetailList().isEmpty()) {
                    return doc;
                }
                int start = 8;
                if (samples.size() > 6) {
                    // 处理行数
                    AsposeUtil.addRowsIndex(tables.get(0), 3, samples.size() - 6,8);
                    start = 8 + (samples.size() - 6);
                    //遍历表格插入数据
//                    XWPFTable table1 = tables.get(j);
//                    List<XWPFTableRow> rows1 = table1.getRows();
//                    for (int i = 1; i < rows1.size(); i++) {
//                        List<XWPFTableCell> cells = rows1.get(i).getTableCells();
//                        for (int j1 = 0; j1 < cells.size(); j1++) {
//                            XWPFTableCell cell = cells.get(j1);
//                            // 设置水平居中,需要ooxml-schemas包支持
//                            CTTc cttc = cell.getCTTc();
//                            CTTcPr ctPr = cttc.addNewTcPr();
//                            ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
//                            cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
//                        }
//                    }
                }
                for (int i = 0; i < samples.size(); i++) {
                    SampleDetailVo sampleDetailVo = samples.get(i);
                    // 补充表格数据 样品名称
                    rows.get(i + 2).getTableCells().get(0).setText(sampleDetailVo.getAliasName());
                    // 规格/等级
                    rows.get(i + 2).getTableCells().get(1).setText(StringUtils.isEmpty(sampleDetailVo.getSpecs()) ? "——" : sampleDetailVo.getSpecs());
                    // 批号/编号
                    rows.get(i + 2).getTableCells().get(2).setText(StringUtils.isEmpty(sampleDetailVo.getBatchNumber()) ? "——" : sampleDetailVo.getBatchNumber());
                    // 样品数量
                    rows.get(i + 2).getTableCells().get(3).setText(sampleDetailVo.getSampleQuantity());
                    // 样品产地
                    rows.get(i + 2).getTableCells().get(4).setText("——");
                    //样品编号
                    rows.get(i + 2).getTableCells().get(5).setText(sampleDetailVo.getSampleCode());
                    // 备注
                    rows.get(i + 2).getTableCells().get(6).setText(StringUtils.isEmpty(sampleDetailVo.getSampleRemark()) ? "——" : sampleDetailVo.getSampleRemark());
                    //6月22日 (多组样品有相同的检测项无法预览任务单；产品标准、检测项都要去重展示；没有价格的子检测项目不展示) 废弃
                    //9月2日  检测项名称一致和标准规范一致。进行去重。
                    if(!StringUtils.isEmpty(sampleDetailVo.getCheckItemInfoList())){
                        for(CheckItemInfoVo checkItemInfoVo :sampleDetailVo.getCheckItemInfoList()){
                            checkItemInfoVoMap.put(checkItemInfoVo.getCheckItemName()+String.valueOf(checkItemInfoVo.getStandardId()),
                                    checkItemInfoVo);
                        }
                    }
                }
                // 提供资料
                rows.get(start+0).getTableCells().get(1).setText(taskDetailInfoVo.getPresentInformation());
                // 取样方式
                rows.get(start+1).getTableCells().get(1).setText(taskDetailInfoVo.getSamplingMethod());
                // 检验目的
                rows.get(start+1).getTableCells().get(3).setText(taskDetailInfoVo.getCheckPurpose());
                // 产品标准 去重
                if (!StringUtils.isEmpty(taskDetailInfoVo.getJudgmentBasis())) {
                    String[] arrays = taskDetailInfoVo.getJudgmentBasis().split(",");
                    Set<String> set = new HashSet<>();
                    for (int i = 0; i < arrays.length; i++) {
                        set.add(arrays[i]);
                    }
                    StringBuilder stringBuilder1 = new StringBuilder();
                    for(int x=0; x<set.toArray().length; x++){
                        stringBuilder1.append(set.toArray()[x]);
                        stringBuilder1.append(",");
                    }
                    if(stringBuilder1.length()>2){
                        rows.get(start+1).getTableCells().get(5).setText(stringBuilder1.deleteCharAt(stringBuilder1.length()-1).toString());
                    }
                }
                // 检测项目及检验依据
                // 6月22日 (多组样品有相同的检测项无法预览任务单；产品标准、检测项都要去重展示；没有价格的子检测项目不展示) 废弃
                // 9月2日  检测项名称一致和标准规范一致。进行去重。
                for (String key : checkItemInfoVoMap.keySet()) {
                    CheckItemInfoVo checkItemInfoVo = checkItemInfoVoMap.get(key);
                    String name = checkItemInfoVo.getCheckItemName();
                    stringBuilder.append(name);
                    if (!StringUtils.isEmpty(checkItemInfoVo.getStandardName())) {
                        stringBuilder.append("（");
                        String s = checkItemInfoVo.getStandardName();
                        String aa = s.split("《")[0];
                        stringBuilder.append(aa);
                        stringBuilder.append("）");
                    }
                    stringBuilder.append("，");
                    // 获取样品的检测项信息
                    checkItemInfoVo.setTimes(checkItemInfoVo.getTimes() != null ? checkItemInfoVo.getTimes() : 0);
                    checkItemInfoVo.setCheckPrice(checkItemInfoVo.getCheckPrice() != null ? checkItemInfoVo.getCheckPrice() : "0");
                    cost += (checkItemInfoVo.getTimes() * Integer.parseInt(checkItemInfoVo.getCheckPrice()));
                }
                String substring = "";
                if(stringBuilder.length()>1){
                    substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                }
                rows.get(start+2).getTableCells().get(1).setText(substring);
                // 要求检验完成日期
                rows.get(start+3).getTableCells().get(1).setText(taskDetailInfoVo.getRequiredCompletionTime());
                // 本单产值 6月17日 任务单 test_task 字段 task_price 为准
                if (!StringUtils.isEmpty(taskDetailInfoVo.getCost())) {
                    rows.get(start+3).getTableCells().get(3).setText(String.valueOf(taskDetailInfoVo.getCost()));
                } else {
                    rows.get(start+3).getTableCells().get(3).setText(String.valueOf("--"));
                }
            }
            // 数据：处理 2023年08月01日发布 第二页
            if (j == 1) {
                // 检测项目处理 add增加表格。
                // 判断表格 是否大于5
                if(checkItemInfoVoMap.size()>5){
                    AsposeUtil.addRowsIndex(tables.get(j), 8, checkItemInfoVoMap.size() - 5,9);
//                    //遍历表格插入数据
//                    XWPFTable table1 = tables.get(j);
//                    List<XWPFTableRow> rows1 = table1.getRows();
//                    for (int i = 1; i < rows1.size(); i++) {
//                        List<XWPFTableCell> cells = rows1.get(i).getTableCells();
//                        for (int j1 = 0; j1 < cells.size(); j1++) {
//                            XWPFTableCell cell = cells.get(j1);
//
//                            // 设置水平居中,需要ooxml-schemas包支持
//                            CTTc cttc = cell.getCTTc();
//                            CTTcPr ctPr = cttc.addNewTcPr();
//                            ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
//                            cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
//                        }
//                    }
                }
                // 塞入数据
                int serialNumber = 4;
                for (String key : checkItemInfoVoMap.keySet()) {
                    CheckItemInfoVo checkItemInfoVo = checkItemInfoVoMap.get(key);
                    rows.get(serialNumber).getTableCells().get(1).setText(checkItemInfoVo.getCheckItemName());
                    serialNumber+=1;
                }
            }
        }
        return doc;
    }

    /**
     * 根据任务单id 更新样品状态 = 1
     * @param taskIds
     */
    protected void updateSampleStateMethod(List<Long> taskIds,List<TaskTestEntity> taskTestEntities) {
        // 根据任务单id 获取样品状态
        List<SampleEntity> sampleList = sampleEntityMapper.selectAllState(taskIds);
        // 遍历 state < 1 : 更新状态 = 1
        for (SampleEntity sampleEntity : sampleList) {
            if (sampleEntity.getState() != null &&
                    (Integer.parseInt(sampleEntity.getState()) < 1 || Integer.parseInt(sampleEntity.getState()) == 5)) {
                // 更新操作。
                TestSampleEntity data = new TestSampleEntity();
                data.setId(sampleEntity.getId());
                data.setState("1");
                testSampleEntityMapper.updateById(data);
            }
            // 根据样品id 查询样品流转列表
            List<SampleCirculationRecord> circulationList = sampleEntityMapper.getRecords(sampleEntity.getId(), 30);
            if (CollectionUtil.isNotEmpty(circulationList)) {
                Boolean flag = false;
                for (SampleCirculationRecord sampleCirculationRecord : circulationList) {
                    if (sampleCirculationRecord.getStatus().equals("1")) {
                        flag = true;
                    }
                }
                if (!flag) {
                    // 从任务单列表 获取 留样人、留样时间
                    for(TaskTestEntity taskData : taskTestEntities){
                        if(taskData.getId().equals(sampleEntity.getTaskId())){
                            String[] lableArrays = taskData.getSampler().split("&");
                            // 增加样品样品流转状态
                            SampleCirculationRecord sa = new SampleCirculationRecord();
                            sa.setSampleId(sampleEntity.getId());
                            sa.setStatus("1");
                            sa.setOperatorId(Long.parseLong(lableArrays[1]));
                            sa.setOperatorName(lableArrays[0]);
                            // 通过样品id 查询任务单列表 获取流转日期
                            sa.setTime(taskData.getSampleReceivingTime());
                            sampleEntityMapper.saveSampleCirculationRecord(sa);
                        }
                    }

                }
            }
        }
    }


    @Override
    public String verifyUserInformation(Long userId) {
        List<LabelValueVo> labelValueVos = sysRoleDao.selectSysyRoleName(66L);
        if (CollectionUtil.isEmpty(labelValueVos)) {
            return "用户授权人用户为空";
        }
        Boolean status = false;
        for (LabelValueVo data : labelValueVos) {
            if (data.getValue().equals(userId)) {
                status = true;
            }
        }
        if (!status) {
            return "当前操作人 无用户授权人角色";
        }
        return null;
    }

    @Override
    public PageInfo<TestTaskPool> taskHall(ReqTaskPool bean) {
        PageHelper.startPage(bean.getPageNum(),bean.getPageSize());
        List<TestTaskPool> list = taskMapper.taskHall(bean);
        PageInfo<TestTaskPool> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public PageInfo<TestTaskPool> myTaskList(ReqTaskPool bean) {
        PageHelper.startPage(bean.getPageNum(),bean.getPageSize());
        List<TestTaskPool> list = taskMapper.myTaskList(bean);
        PageInfo<TestTaskPool> pageInfo = new PageInfo<>(list);
        for (TestTaskPool taskPool :pageInfo.getList()){
            List<TaskRes> list1 = Lists.newArrayList();
            String[] split = taskPool.getTaskCode().split(",");
            for (String s :split){
                String[] strings = s.split("&");
                TaskRes taskRes = new TaskRes();
                taskRes.setId(strings[1]);
                taskRes.setTaskCode(strings[0]);
                list1.add(taskRes);
            }
            taskPool.setList(list1);
        }
        return pageInfo;
    }

    @Override
    public Boolean judgeTaskEndTest(Long id, ExcelInsertVo excelInsertVo) {
        // 判断操作检测项 == 中间复核 不验证任务单直接返回即可
        // 获取 类型（中间复核 或 最终复核）
        if (excelInsertVo.getCheckReview().equals("中间复核")) {
            return true;
        }
        // 任务单单自身 == 4 ：试验完成
        Integer state = taskMapper.getJudgmentTaskList(id);
        if(state!=null && state >= 4){
            return true;
        }
        return false;
    }
}

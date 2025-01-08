package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.aspose.cells.Cells;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bytedeco.opencv.presets.opencv_core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@Service
public class TestCheckItemsTaskRelServiceImpl extends ServiceImpl<TestCheckItemsTaskRelMapper, TestCheckItemsTaskRel> implements TestCheckItemsTaskRelService {
    @Resource
    private TestTeamDao testTeamDao;
    @Resource
    private TaskMapper taskMapper;
    @Resource
    private TestTaskOrderWorkingHoursMapper testTaskOrderWorkingHoursMapper;
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    TestItemOrderWorkingHoursMapper testItemOrderWorkingHoursMapper;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private TestCheckItemsTaskRelMapper testCheckItemsTaskRelMapper;
    @Autowired
    private StatisticsMapper statisticsMapper;
    @Autowired
    private ProductItemEntityMapper productItemEntityMapper;
    @Autowired
    private TestTaskOrderWorkingHoursService testTaskOrderWorkingHoursService;
    @Autowired
    private TestItemOrderWorkingHoursService testItemOrderWorkingHoursService;
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private TestEntrustedTaskRelDao testEntrustedTaskRelDao;
    @Autowired
    private TestTaskPoolService testTaskPoolService;

    @Override
    public IPage<WorkHourStatisticVo> getWorkHoursList(Page<WorkHourStatisticVo> page, Map<String, Object> paramMap) {
        //根据传入的部门id获取部门及下级的所有部门id
        paramMap.put("deptIds", getTeamIdList(MapUtils.getInteger(paramMap, "deptId")));
        return baseMapper.getWorkHoursList(page, paramMap);
    }


    public List<Integer> getTeamIdList(Integer id) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        LambdaQueryWrapper<TestTeam> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(TestTeam::getPid, id);
        List<TestTeam> teamList = testTeamDao.selectList(queryWrapper);
        if (teamList != null && teamList.size() > 0) {
            for (TestTeam team : teamList) {
                ids.addAll(getTeamIdList(team.getId()));
            }
        }
        return ids;
    }

    /**
     * 查询 - 工时统计-我的工时
     *
     * @param taskStatisticsVo
     * @return
     */
    @Override
    public Result getMyHoursStatistics(TaskStatisticsVo taskStatisticsVo) {
        if (taskStatisticsVo.getPageNum() == null || taskStatisticsVo.getPageSize() == null) {
            return ResultUtil.error("分页参数不能为空");
        }
        // 查询条件 = 来源 不为空的话。
        if (StringUtils.isNotEmpty(taskStatisticsVo.getSource())) {
//            System.out.println("来源信息 == 暂定为空" + taskStatisticsVo.getSource());
            taskStatisticsVo.setSource(null);
        }
        // 获取当前用户登录的信息
        SysUserEntity user = ShiroUtils.getUserInfo();
        // 当前登录人 = 授权签字人
        taskStatisticsVo.setReceiverUserId(user.getUserId());
        // 进行 查询分页。
        PageHelper.clearPage();
        PageHelper.startPage(taskStatisticsVo.getPageNum(), taskStatisticsVo.getPageSize());
        if (taskStatisticsVo.getStopDate() != null) {
            SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd");
            String startStr = startFormat.format(taskStatisticsVo.getStopDate());
            String[] split = startStr.split("-");
            LocalDate localDate = LocalDate.of(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
            LocalDate endDate = localDate.plusDays(1);
            Date date = Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            taskStatisticsVo.setStopDate(date);
        }
        List<TaskStatisticsVo> list = baseMapper.getMyHoursStatistics(taskStatisticsVo);
        // 分页后： 进行统计每组检测项 工时
        if (CollectionUtil.isNotEmpty(list)) {
            for (TaskStatisticsVo taskStatisticsVo1 : list) {
                if (taskStatisticsVo1.getState() == 0) {
                    taskStatisticsVo1.setStatus(true);
                    taskStatisticsVo1.setState(null);
                } else {
                    taskStatisticsVo1.setStatus(false);
                    taskStatisticsVo1.setState(null);
                }
            }
        }
        PageInfo<TaskStatisticsVo> result = new PageInfo<>(list);
        return ResultUtil.success(result);
    }

    @Override
    public Result getMyHoursStatisticsSum(TaskStatisticsVo taskStatisticsVo) {
        // 获取当前用户登录的信息
        SysUserEntity user = ShiroUtils.getUserInfo();
        // 当前登录人 = 授权签字人
        taskStatisticsVo.setReceiverUserId(user.getUserId());
        // 进行工时 总计
        PageHelper.clearPage();
        taskStatisticsVo.setPageSize(null);
        taskStatisticsVo.setPageNum(null);
        if (taskStatisticsVo.getStopDate() != null) {
            SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd");
            String startStr = startFormat.format(taskStatisticsVo.getStopDate());
            String[] split = startStr.split("-");
            LocalDate localDate = LocalDate.of(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
            LocalDate endDate = localDate.plusDays(1);
            Date date = Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            taskStatisticsVo.setStopDate(date);
        }
        String countSum = baseMapper.getMyHoursStatisticsSum(taskStatisticsVo);
        if (countSum != null) {
            // 返回数据
            Map<String, Object> map = new HashMap<>();
            map.put("sumCount", String.format("%.2f", Double.parseDouble(countSum)));
            return ResultUtil.success(map);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sumCount", "0.00");
        return ResultUtil.success(map);
    }

    /**
     * 查询我的工时-统计-导出
     *
     * @param taskStatisticsVo
     * @return
     */
    @Override
    public InputStream getMyHoursStatisticsExport(TaskStatisticsVo taskStatisticsVo) throws IOException {
        taskStatisticsVo.setPageNum(1);
        taskStatisticsVo.setPageSize(100000);
        Result result = getMyHoursStatistics(taskStatisticsVo);
        PageInfo<TaskStatisticsVo> pageInfo = (PageInfo<TaskStatisticsVo>) result.getData();
        List<TaskStatisticsVo> list = pageInfo.getList();
        // 我的工时统计-导出
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet = wb.createSheet("sheet0");
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1 = sheet.createRow(0);
        //创建单元格并设置单元格内容
        row1.createCell(0).setCellValue("任务编号");
        row1.createCell(1).setCellValue("工时");
        row1.createCell(2).setCellValue("来源");
        row1.createCell(3).setCellValue("接收任务时间");
        row1.createCell(4).setCellValue("完成时间");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (CollectionUtil.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                TaskStatisticsVo statisticsVo = list.get(i);
                //在sheet里创建第二行
                HSSFRow row3 = sheet.createRow(i + 1);
                row3.createCell(0).setCellValue(statisticsVo.getTaskCode());
//                String Begandate = formatter.format(clientOrderdetailVo.getAcceptancebeganDate());
                row3.createCell(1).setCellValue(statisticsVo.getWorkingHours() != null ? statisticsVo.getWorkingHours().toString() : "-");
                row3.createCell(2).setCellValue(statisticsVo.getSource() != null ? statisticsVo.getSource() : "-");
                row3.createCell(3).setCellValue(statisticsVo.getOrderTime() != null ? formatter.format(statisticsVo.getOrderTime()) : "-");
                row3.createCell(4).setCellValue(statisticsVo.getRequiredCompletionTime() != null ? formatter.format(statisticsVo.getRequiredCompletionTime()) : "-");
            }
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    public Result getMyHoursStatisticsDetails(Long taskId, String workingHoursId) {
        // 查询任务单下 人员比例信息
        LambdaQueryWrapper<TestTaskOrderWorkingHours> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, taskId);
        if (workingHoursId != null) {
            lambdaQueryWrapper.eq(TestTaskOrderWorkingHours::getWorkingHoursId, Long.valueOf(workingHoursId));
        }
        List<TestTaskOrderWorkingHours> list = testTaskOrderWorkingHoursMapper.selectList(lambdaQueryWrapper);
        // 返回数据
        Map<String, Object> map = new HashMap<>();
        if (CollectionUtils.isEmpty(list)) {
            // 根据任务单 查询任务下对应操作信息
            TaskTestEntity taskDetails = taskMapper.selectTaskEntity(taskId);
            //TODO:11月10 查询基础表信息 - 检测类型包含工时
            List<TestInitDataEntity> basisList = taskMapper.selectEntrustBasis(30);
            // 辅助人包括报告制作和辅助人员
            if (StringUtils.isNotEmpty(taskDetails.getReportProducer()) && StringUtils.isNotEmpty(taskDetails.getAuxiliaryPersonnel())) {
                // 任务单中 包含报告制作人与辅助人员 不为空 则平摊比例
                TestInitDataEntity reportProducerData = basisList.get(3);
                int zhi1 = Integer.parseInt(reportProducerData.getRemark()) / 2;
                reportProducerData.setRemark(String.valueOf(zhi1));
                basisList.set(3, reportProducerData);
                TestInitDataEntity auxiliaryPersonnel = basisList.get(4);
                int zhi2 = Integer.parseInt(auxiliaryPersonnel.getRemark()) / 2;
                auxiliaryPersonnel.setRemark(String.valueOf(zhi2));
                basisList.set(4, auxiliaryPersonnel);
            } else {
                // 辅助人员 工时 = 0
                TestInitDataEntity auxiliaryPersonnel = basisList.get(4);
                auxiliaryPersonnel.setRemark("0");
                basisList.set(4, auxiliaryPersonnel);
            }
            List<TestTaskOrderWorkingHours> countList = new ArrayList<>();
            // 使用 map 进行 数据统计 key = userId , value = 参数
            Map<Long, TestTaskOrderWorkingHours> mapData = new HashMap<>();
            // 直接返回任务单数据即可。
            // 检测人员
            if (StringUtils.isNotEmpty(taskDetails.getInspector())) {
                //  调用方法 去处理 签名信息进行截取
                methodSubstr(0, taskDetails.getInspector(), mapData, taskId, basisList);
            }
            // 记录人员
            if (StringUtils.isNotEmpty(taskDetails.getRecorder())) {
                //  调用方法 去处理 签名信息进行截取
                methodSubstr(1, taskDetails.getRecorder(), mapData, taskId, basisList);
            }
            // 复核人
            if (StringUtils.isNotEmpty(taskDetails.getReviewer())) {
                //  调用方法 去处理 签名信息进行截取
                methodSubstr(2, taskDetails.getReviewer(), mapData, taskId, basisList);
            }
            // 报告制作人
            if (StringUtils.isNotEmpty(taskDetails.getReportProducer())) {
                //  调用方法 去处理 签名信息进行截取
                methodSubstr(3, taskDetails.getReportProducer(), mapData, taskId, basisList);
            }
            // 接单人
            if (StringUtils.isNotEmpty(taskDetails.getReceiver())) {
                //  调用方法 去处理 签名信息进行截取
                // 查找userId与name
                String name = sysUserDao.getSysUserName(Long.valueOf(taskDetails.getReceiver()));
                methodSubstr(5, name + "&" + taskDetails.getReceiver(), mapData, taskId, basisList);
            }
            // 辅助人员
            if (StringUtils.isNotEmpty(taskDetails.getAuxiliaryPersonnel())) {
                //  调用方法 去处理 签名信息进行截取
                methodSubstr(4, taskDetails.getAuxiliaryPersonnel(), mapData, taskId, basisList);
            }
            // 进行循环迭代 mapData 数据
            if (CollectionUtil.isNotEmpty(mapData.keySet())) {
                for (Long key : mapData.keySet()) {
                    TestTaskOrderWorkingHours data = mapData.get(key);
                    countList.add(data);
                }
            }
            map.put("list", countList);
            // 通过任务单id 获取任务单下对应的检测项总工时
            String workingHours = null;
            if (workingHoursId != null) {
                workingHours = testItemOrderWorkingHoursMapper.getTotalWorkingHours(taskId, Long.valueOf(workingHoursId));
            } else {
                workingHours = baseMapper.getWorkingHours(taskId);
            }
            // 任务单 工时
            map.put("sumCount", workingHours);
        } else {
            map.put("list", list);
            // 获取总工时：
            map.put("sumCount", list.get(0).getTotalWorkingHours());
        }
        return ResultUtil.success(map);
    }

    /**
     * 调用方法 去处理 签名信息进行截取
     *
     * @param type                 签名类型 0=检测人员：、 1=记录人员：、2=复核人：、3=报告制作人：
     * @param signatureInformation 签名信息
     * @param mapData              使用 map 进行 数据统计 key = userId , value = 参数
     * @param taskId               任务单id
     */
    public void methodSubstr(Integer type, String signatureInformation, Map<Long, TestTaskOrderWorkingHours> mapData, Long taskId, List<TestInitDataEntity> basisList) {
        String typeStr = basisList.get(type).getName();
        // key = 类型 value = 比例值
        Map<String, Integer> map = new HashMap<>();
        for (TestInitDataEntity dataEntity : basisList) {
            map.put(dataEntity.getName(), Integer.parseInt(dataEntity.getRemark()));
        }
        // 检测人员
        String[] inspectorarrays = signatureInformation.split(",");
        int listSize = inspectorarrays.length;
        for (int i = 0; i < inspectorarrays.length; i++) {
            String[] inspectorStr = inspectorarrays[i].split("&");
            if (mapData.get(Long.parseLong(inspectorStr[1])) == null) {
                TestTaskOrderWorkingHours data = new TestTaskOrderWorkingHours();
                data.setUserName(inspectorStr[0]);
                data.setUserId(Long.parseLong(inspectorStr[1]));
                // 设置比例
                if (map.get(typeStr) != null) {
                    Double value = Double.valueOf(map.get(typeStr));
                    BigDecimal zhi1 = BigDecimal.valueOf(value / listSize).setScale(2, BigDecimal.ROUND_HALF_UP);
                    data.setProportion(String.valueOf(zhi1));
                } else {
                    data.setProportion("0");
                }
                data.setDetectionType(typeStr);
                data.setTaskId(taskId);
                mapData.put(data.getUserId(), data);
            } else {
                TestTaskOrderWorkingHours data = mapData.get(Long.parseLong(inspectorStr[1]));
                data.setUserName(inspectorStr[0]);
                data.setUserId(Long.parseLong(inspectorStr[1]));
                // 设置比例
                if (map.get(typeStr) != null) {
                    Double value1 = Double.valueOf(map.get(typeStr));
                    BigDecimal zhi1 = BigDecimal.valueOf(value1 / listSize).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal value2 = BigDecimal.valueOf(Double.parseDouble(data.getProportion())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal sum = zhi1.add(value2);
                    data.setProportion(String.valueOf(sum));
                } else {
                    Double value1 = Double.valueOf(data.getProportion());
                    data.setProportion(String.valueOf(value1));
                }
                data.setDetectionType(data.getDetectionType() + "、" + typeStr);
                data.setTaskId(taskId);
                mapData.put(data.getUserId(), data);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result postAdjustingQuotas(List<TestTaskOrderWorkingHours> list, String workingHoursId) throws ParseException {
        // 当前登录需要为授权签字人： TODO: 11月10日 roleId = 66L 为授权签字人
        Long roleId = 66L;
        List<Long> userIds = sysUserDao.selectUserIds(roleId);
        // 获取当前用户登录的信息
        SysUserEntity user = ShiroUtils.getUserInfo();
        if (CollectionUtil.isEmpty(userIds)) {
            // 抛出 授权签字人角色信息 为空
            return ResultUtil.error("授权签字人角色信息为空");
        }
        Boolean falg = false;
        for (Long userId : userIds) {
            if (userId.equals(user.getUserId())) {
                falg = true;
            }
        }
        if (!falg) {
            return ResultUtil.error("分配失败，当前操作人无授权签字人角色");
        }
        // 查询工时任务单信息 比对 是否为领单人。
        LambdaQueryWrapper<TestTaskOrderWorkingHours> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TestTaskOrderWorkingHours::getWorkingHoursId, workingHoursId);
        lambdaQueryWrapper.like(TestTaskOrderWorkingHours::getAddOperator, user.getUserId());
        lambdaQueryWrapper.last("limit 1");
        TestTaskOrderWorkingHours taskOrderWokingHoursData = testTaskOrderWorkingHoursMapper.selectOne(lambdaQueryWrapper);
        if (taskOrderWokingHoursData == null) {
            return ResultUtil.error("分配失败，不是领单人");
        }
        // 判断任务单是否存在
        TaskTestEntity taskDetails = taskMapper.selectTaskEntity(list.get(0).getTaskId());
        if (taskDetails == null) {
            return ResultUtil.error("分配失败，任务单不存在");
        }
        // 通过任务单id 获取 报告的签发时间
        ReportRecordEntity reportDetails = reportMapper.queryReportDetailsByTaskKey(taskDetails.getId());
        if (reportDetails == null) {
            return ResultUtil.error("分配失败，报告单不存在");
        }
        List<TestInitDataEntity> entrustBasis = taskMapper.selectEntrustBasis(26);
        Integer type = Integer.parseInt(entrustBasis.get(0).getName());

        // 获取 系统基础效验信息：
        if (type == 0) {
            // 当前签发时间（24小时之内）。
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date nowDate = new Date();
            // 去除当前时间 时分秒
            Date currentLatestTime = format.parse(format.format(nowDate));

            // 报告签发时间 09-05
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(reportDetails.getIssuerTime());
            calendar.add(Calendar.DAY_OF_MONTH, +1);
            Date lastDayOfMonth = calendar.getTime();
            //  报告签发（2024-09-05 ) < 当前时间 2024-09-05 + 1
            if (lastDayOfMonth.before(currentLatestTime)) {
                return ResultUtil.error("分配失败,最晚分配时间为 " + format.format(lastDayOfMonth));
            }
        }

        // 删除旧数据
        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, list.get(0).getTaskId());
        if (workingHoursId != null) {
            queryWrapper.eq(TestTaskOrderWorkingHours::getWorkingHoursId, Long.parseLong(workingHoursId));
        }
        testTaskOrderWorkingHoursMapper.delete(queryWrapper);
        // 任务单号
        String taskCode = taskDetails.getTaskCode();
        // 接单人 =  来源
        String name = sysUserDao.getSysUserName(Long.valueOf(taskDetails.getReceiver()));
        // 通过任务单 获取样品信息
        List<String> sampleNames = taskMapper.getTaskSamples(list.get(0).getTaskId());
        StringBuffer nameBuffer = new StringBuffer();
        if (CollectionUtil.isNotEmpty(sampleNames)) {
            for (String sampleName : sampleNames) {
                nameBuffer.append(sampleName + " ");
            }
        }
        for (TestTaskOrderWorkingHours taskOrderWorkingHours : list) {
            taskOrderWorkingHours.setAddOperator(user.getName() + "&" + user.getUserId());
            taskOrderWorkingHours.setCreateTime(new Date());
            // 样品名称 = 任务单下 样品名称
            taskOrderWorkingHours.setSampleName(nameBuffer.toString());
            // 来源 = 任务单的下单人
            taskOrderWorkingHours.setSource(name);
            // 任务单号
            taskOrderWorkingHours.setTaskCode(taskCode);
            // 使用工时
            if (StringUtils.isEmpty(taskOrderWorkingHours.getProportion())) {
                // 工时 = 0
                taskOrderWorkingHours.setWorkingHours("0");
            } else {
                // 求比例工时
                double one22 = Double.parseDouble(taskOrderWorkingHours.getProportion()) / 100d;
                double zhi = Double.parseDouble(taskOrderWorkingHours.getTotalWorkingHours()) * one22;
                String context = String.format("%.2f", zhi);
                taskOrderWorkingHours.setWorkingHours(context);
            }
            if (workingHoursId != null) {
                taskOrderWorkingHours.setWorkingHoursId(workingHoursId);
            }
            taskOrderWorkingHours.setUpdateTime(null);
            testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        }
        return ResultUtil.success("数据新增成功");
    }

    private static TestTeam getTopDepartment(Map<Integer, TestTeam> departmentMap, int departmentId) {

        TestTeam department = departmentMap.get(departmentId);

        if (department == null) {
            return null;
        }
        if (department.getPid() == 0) { // 如果顶级部门的父ID为0，则返回该部门本身。在本例中，顶级部门是指ID为0的部门。
            return department;
        } else { // 否则，递归查找顶级部门。
            return getTopDepartment(departmentMap, department.getPid());
        }
    }

    /**
     * 调用方法查询全部部门信息
     *
     * @return 返回 Map: key =部门 value = 顶级部门的信息
     */
    public Map<Integer, TestTeam> methodDepartmentTopMap() {
        // 查询 每个团队的顶级部门。
        List<TestTeam> teamAll = testTeamDao.selectList(null);
        // key=团队 value = 顶级部门
        Map<Integer, TestTeam> departmentTopMap = new HashMap<>();
        // 部门map集合
        Map<Integer, TestTeam> departmentMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(teamAll)) {
            for (TestTeam testTeam : teamAll) {
                departmentMap.put(testTeam.getId(), testTeam);
            }
            for (TestTeam testTeam2 : teamAll) {
                // 使用递归方法。
                TestTeam topDepartment = getTopDepartment(departmentMap, testTeam2.getId());
                if (topDepartment != null) {
                    departmentTopMap.put(testTeam2.getId(), topDepartment);
                }
            }
        }
        return departmentTopMap;
    }

    /**
     * 根据顶级部门名称 获取 所属下级部门id集合
     *
     * @param deptList         key = 顶级组织名，  所在组织 都是顶级部门：根据顶级部门 带出所属部门id 集合。
     * @param departmentTopMap key=团队id value = 顶级部门 ： 调用方法获取
     */
    public void methodDepartmentNameAcquisitionSet(Map<String, List<Long>> deptList, Map<Integer, TestTeam> departmentTopMap) {
        if (CollectionUtil.isNotEmpty(departmentTopMap.keySet())) {
            for (Integer teamId : departmentTopMap.keySet()) {
                TestTeam testTeam = departmentTopMap.get(teamId);
                if (deptList.get(testTeam.getName()) != null) {
                    List<Long> teamList = deptList.get(testTeam.getName());
                    teamList.add(teamId.longValue());
                } else {
                    List<Long> teamList = new ArrayList<>();
                    teamList.add(teamId.longValue());
                    deptList.put(testTeam.getName(), teamList);
                }
            }
        }
    }

    /**
     * 全部任务单 工时统计
     *
     * @param totalWorkforAllTasks 根据人员 查询 全部任务总工时列表
     * @param list                 人员数据集合不为空
     */
    public void methodWorkingHours(List<TestTaskOrderWorkingHours> totalWorkforAllTasks, List<TaskStatisticsVo> list) {
        if (CollectionUtil.isNotEmpty(totalWorkforAllTasks)) {
            // key = 人员id、 value = 工时信息
            Map<Long, TaskStatisticsVo> map = new HashMap<>();
            // 调用方法 : 循环遍历每组工时信息 进行汇总
            for (TestTaskOrderWorkingHours taskOrderWorkingHours : totalWorkforAllTasks) {
                if (map.get(taskOrderWorkingHours.getUserId()) == null) {
                    TaskStatisticsVo data = new TaskStatisticsVo();
                    // 人员id
                    data.setReceiverUserId(taskOrderWorkingHours.getUserId());
                    // 个人工时
                    double zhi = Double.parseDouble(taskOrderWorkingHours.getWorkingHours());
                    String context = String.format("%.2f", zhi);
                    data.setWorkingHours(context);
                    // 完成单量 任务单 state >=3 算是完成
                    if (taskOrderWorkingHours.getState() >= 3) {
                        data.setCompletedTaskVolume(String.valueOf(1));
                    } else {
                        data.setCompletedTaskVolume(String.valueOf(0));
                    }
                    map.put(taskOrderWorkingHours.getUserId(), data);
                } else {
                    TaskStatisticsVo data = map.get(taskOrderWorkingHours.getUserId());
                    // 人员id
                    data.setReceiverUserId(taskOrderWorkingHours.getUserId());
                    // 个人工时 = 旧工时 + 新工时
                    double zhi1 = Double.parseDouble(data.getWorkingHours());
                    double zhi2 = Double.parseDouble(taskOrderWorkingHours.getWorkingHours());
                    double sum = zhi1 + zhi2;
                    String context = String.format("%.2f", sum);
                    data.setWorkingHours(context);
                    // 完成单量 任务单 state >=3 算是完成 = 任务量 + 1
                    if (taskOrderWorkingHours.getState() >= 3) {
                        data.setCompletedTaskVolume(String.valueOf(Integer.parseInt(data.getCompletedTaskVolume()) + 1));
                    } else {
                        data.setCompletedTaskVolume(String.valueOf(Integer.parseInt(data.getCompletedTaskVolume()) + 0));
                    }
                    map.put(taskOrderWorkingHours.getUserId(), data);
                }
            }
            for (TaskStatisticsVo statisticsVo : list) {
                TaskStatisticsVo data = map.get(statisticsVo.getReceiverUserId());
                if (data != null) {
                    // 个人工时
                    statisticsVo.setWorkingHours(data.getWorkingHours());
                    // 完成单量 任务单 state >=3 算是完成
                    statisticsVo.setCompletedTaskVolume(data.getCompletedTaskVolume());
                }
            }
        }
    }

    @Override
    public Result getPersonnelStatistics(TaskStatisticsVo taskStatisticsVo) {
        if (taskStatisticsVo.getPageNum() == null || taskStatisticsVo.getPageSize() == null) {
            return ResultUtil.error("分页参数不能为空");
        }
        //  key=团队id value = 顶级部门 ： 调用方法获取
        Map<Integer, TestTeam> departmentTopMap = methodDepartmentTopMap();
        // key = 顶级组织名，  所在组织 都是顶级部门：根据顶级部门 带出所属部门id 集合。
        Map<String, List<Long>> deptList = new HashMap<>();
        // 调用方法： 根据顶级部门名称 获取 所属下级部门id集合
        methodDepartmentNameAcquisitionSet(deptList, departmentTopMap);
        if (!StringUtils.isEmpty(taskStatisticsVo.getTeamName())) {
            List<Long> longList = deptList.get(taskStatisticsVo.getTeamName());
            taskStatisticsVo.setLongList(longList);
        }
        // 展示 人员信息及部门信息
        // 进行 查询分页。
        PageHelper.clearPage();
        PageHelper.startPage(taskStatisticsVo.getPageNum(), taskStatisticsVo.getPageSize());
        List<TaskStatisticsVo> list = testTeamDao.getEmployeesAndDepartments(taskStatisticsVo);
        // 用户id集合
        List<Long> userIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (TaskStatisticsVo statisticsVo : list) {
                // 部门id 对应的顶级部门信息
                if (statisticsVo.getTeamId() != null) {
                    TestTeam team = departmentTopMap.get(statisticsVo.getTeamId().intValue());
                    if (team != null && team.getName() != null) {
                        statisticsVo.setTeamId(team.getId().longValue());
                        statisticsVo.setTeamName(team.getName());
                    }
                }
                userIds.add(statisticsVo.getReceiverUserId());
            }
            // TODO: 11月9日 暂时替换 userIds = deptIds
            taskStatisticsVo.setLongList(userIds);
            // 根据人员 查询 全部任务总工时列表
            if (taskStatisticsVo.getStopDate() != null) {
                SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd");
                String startStr = startFormat.format(taskStatisticsVo.getStopDate());
                String[] split = startStr.split("-");
                LocalDate localDate = LocalDate.of(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
                LocalDate endDate = localDate.plusDays(1);
                Date date = Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                taskStatisticsVo.setStopDate(date);
            }
            List<TestTaskOrderWorkingHours> totalWorkforAllTasks = testTaskOrderWorkingHoursMapper.selectTaskOrderWorkingHours(taskStatisticsVo);
            // 调用方法：全部任务单 工时统计
            methodWorkingHours(totalWorkforAllTasks, list);
            // 计算 人员中任务单参与情况
            for (TaskStatisticsVo statisticsVo : list) {
                TaskListParamVo paramVo = new TaskListParamVo();
                if (taskStatisticsVo.getStartDate() != null && taskStatisticsVo.getStopDate() != null) {
                    paramVo.setStartDate(taskStatisticsVo.getStartDate());
                    paramVo.setStopDate(taskStatisticsVo.getStopDate());
                }
                // 复核人
                paramVo.setReviewer(statisticsVo.getReceiverUserId().toString());
                // 检测人
                paramVo.setInspector(statisticsVo.getReceiverUserId().toString());
                //记录人
                paramVo.setRecorder(statisticsVo.getReceiverUserId().toString());
                //报告制作人
                paramVo.setReportProducer(statisticsVo.getReceiverUserId().toString());
                // 见习生：实习的新手
                paramVo.setProbationer(statisticsVo.getReceiverUserId().toString());
                // 实习生
                paramVo.setInterns(statisticsVo.getReceiverUserId().toString());
                // 辅助人员
                paramVo.setAuxiliaryPersonnel(statisticsVo.getReceiverUserId().toString());
                // 签发人
                paramVo.setReceiver(statisticsVo.getReceiverUserId().toString());
                Integer count = taskMapper.selectTaskTDetectionType(paramVo);
                // 已经接的工作量
                statisticsVo.setReceivedTaskVolume(String.valueOf(count));
            }
        }
        PageInfo<TaskStatisticsVo> result = new PageInfo<>(list);
        return ResultUtil.success(result);
    }

    @Override
    public InputStream getPersonnelStatisticsExport(TaskStatisticsVo taskStatisticsVo) throws IOException {
        taskStatisticsVo.setPageNum(1);
        taskStatisticsVo.setPageSize(100000);
        Result result = getPersonnelStatistics(taskStatisticsVo);
        PageInfo<TaskStatisticsVo> pageInfo = (PageInfo<TaskStatisticsVo>) result.getData();
        List<TaskStatisticsVo> list = pageInfo.getList();
// 我的工时统计-导出
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet = wb.createSheet("sheet0");
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1 = sheet.createRow(0);
        //创建单元格并设置单元格内容
        row1.createCell(0).setCellValue("名称");
        row1.createCell(1).setCellValue("已接任务");
        row1.createCell(2).setCellValue("完成任务");
        row1.createCell(3).setCellValue("已完成合计（工时）");
        row1.createCell(4).setCellValue("所在组织");
        if (CollectionUtil.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                TaskStatisticsVo statisticsVo = list.get(i);
                //在sheet里创建第二行
                HSSFRow row3 = sheet.createRow(i + 1);
                row3.createCell(0).setCellValue(statisticsVo.getReceiver());
                // 已接任务
                row3.createCell(1).setCellValue(statisticsVo.getReceivedTaskVolume() != null ? statisticsVo.getReceivedTaskVolume().toString() : "-");
                // 完成任务
                row3.createCell(2).setCellValue(statisticsVo.getCompletedTaskVolume() != null ? statisticsVo.getCompletedTaskVolume().toString() : "-");
                // 工时
                row3.createCell(3).setCellValue(statisticsVo.getWorkingHours() != null ? statisticsVo.getWorkingHours().toString() : "-");
                // 所在组织
                row3.createCell(4).setCellValue(statisticsVo.getTeamName() != null ? statisticsVo.getTeamName() : "-");
            }
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    public Result getTotalPersonnelHours(TaskStatisticsVo taskStatisticsVo) {
        // 进行工时 总计
        PageHelper.clearPage();
        taskStatisticsVo.setPageSize(null);
        taskStatisticsVo.setPageNum(null);
        //  key=团队id value = 顶级部门 ： 调用方法获取
        Map<Integer, TestTeam> departmentTopMap = methodDepartmentTopMap();
        // key = 顶级组织名，  所在组织 都是顶级部门：根据顶级部门 带出所属部门id 集合。
        Map<String, List<Long>> deptList = new HashMap<>();
        // 调用方法： 根据顶级部门名称 获取 所属下级部门id集合
        methodDepartmentNameAcquisitionSet(deptList, departmentTopMap);
        if (!StringUtils.isEmpty(taskStatisticsVo.getTeamName())) {
            List<Long> longList = deptList.get(taskStatisticsVo.getTeamName());
            taskStatisticsVo.setLongList(longList);
        }
        // 展示 人员信息及部门信息
        // 进行 查询分页。
        PageHelper.clearPage();
        List<TaskStatisticsVo> list = testTeamDao.getEmployeesAndDepartments(taskStatisticsVo);
        // 用户id集合
        List<Long> userIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (TaskStatisticsVo statisticsVo : list) {
                // 部门id 对应的顶级部门信息
                if (statisticsVo.getTeamId() != null) {
                    TestTeam team = departmentTopMap.get(statisticsVo.getTeamId().intValue());
                    if (team != null && team.getName() != null) {
                        statisticsVo.setTeamId(team.getId().longValue());
                        statisticsVo.setTeamName(team.getName());
                    }
                }
                userIds.add(statisticsVo.getReceiverUserId());
            }
            // TODO: 11月9日 暂时替换 userIds = deptIds
            taskStatisticsVo.setLongList(userIds);
            // 根据人员 查询 全部任务总工时列表
            if (taskStatisticsVo.getStopDate() != null) {
                SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd");
                String startStr = startFormat.format(taskStatisticsVo.getStopDate());
                String[] split = startStr.split("-");
                LocalDate localDate = LocalDate.of(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
                LocalDate endDate = localDate.plusDays(1);
                Date date = Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                taskStatisticsVo.setStopDate(date);
            }
            String totalData = testTaskOrderWorkingHoursMapper.selectTaskOrderCountWorkingHours(taskStatisticsVo);
            if (totalData != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("sumCount", String.format("%.2f", Double.parseDouble(totalData)));
                return ResultUtil.success(map);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sumCount", "0.00");
        return ResultUtil.success(map);
    }

    @Override
    public Result getPersonnelStatisticsDetails(TaskStatisticsVo taskStatisticsVo) {
        if (taskStatisticsVo.getPageNum() == null || taskStatisticsVo.getPageSize() == null) {
            return ResultUtil.error("分页参数不能为空");
        }
        if (taskStatisticsVo.getReceiverUserId() == null) {
            return ResultUtil.error("参数不能为空");
        }
        // 进行 查询分页。
        PageHelper.clearPage();
        PageHelper.startPage(taskStatisticsVo.getPageNum(), taskStatisticsVo.getPageSize());
//        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(TestTaskOrderWorkingHours::getUserId, taskStatisticsVo.getReceiverUserId());
//        List<TestTaskOrderWorkingHours> list = testTaskOrderWorkingHoursMapper.selectList(queryWrapper);
        if (taskStatisticsVo.getStopDate() != null) {
            SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd");
            String startStr = startFormat.format(taskStatisticsVo.getStopDate());
            String[] split = startStr.split("-");
            LocalDate localDate = LocalDate.of(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
            LocalDate endDate = localDate.plusDays(1);
            Date date = Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            taskStatisticsVo.setStopDate(date);
        }
        List<TestTaskOrderWorkingHours> list = testTaskOrderWorkingHoursMapper.selectTaskOrderWorkingHoursList(taskStatisticsVo);
        PageInfo<TestTaskOrderWorkingHours> result = new PageInfo<>(list);
        return ResultUtil.success(result);
    }

    @Override
    public Result getAuthorizedSignatureList(TaskStatisticsVo taskStatisticsVo) {
        if (taskStatisticsVo.getPageNum() == null || taskStatisticsVo.getPageSize() == null) {
            return ResultUtil.error("分页参数不能为空");
        }
        // 根据授权签字（role_id = 66）
        // 进行 查询分页。
        PageHelper.clearPage();
        PageHelper.startPage(taskStatisticsVo.getPageNum(), taskStatisticsVo.getPageSize());
        List<TaskStatisticsVo> list = testTeamDao.getRoleUserInformation(taskStatisticsVo);
        // 用户id集合
        List<Long> userIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (TaskStatisticsVo statisticsVo : list) {
                userIds.add(statisticsVo.getReceiverUserId());
            }
            // TODO: 11月9日 暂时替换 userIds = deptIds
            taskStatisticsVo.setLongList(userIds);
            // 根据授权人员 查询 任务单工时列表-总工时
            if (taskStatisticsVo.getStopDate() != null) {
                SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd");
                String startStr = startFormat.format(taskStatisticsVo.getStopDate());
                String[] split = startStr.split("-");
                LocalDate localDate = LocalDate.of(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
                LocalDate endDate = localDate.plusDays(1);
                Date date = Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                taskStatisticsVo.setStopDate(date);
            }
            List<TestTaskOrderWorkingHours> totalWorkforAllTasks = testTaskOrderWorkingHoursMapper.selectTaskOrderTotalWorkingHours(taskStatisticsVo);
            // 调用方法：全部任务单 工时统计
            methodWorkingHours(totalWorkforAllTasks, list);
        }
        for (TaskStatisticsVo taskStatisticsVo1 : list) {
            if (taskStatisticsVo1.getWorkingHours() == null) {
                taskStatisticsVo1.setWorkingHours("-");
            }
            if (taskStatisticsVo1.getReceivedTaskVolume() == null) {
                taskStatisticsVo1.setReceivedTaskVolume("-");
            }
            if (taskStatisticsVo1.getCompletedTaskVolume() == null) {
                taskStatisticsVo1.setCompletedTaskVolume("-");
            }
        }
        // 计算 人员中任务单参与情况
        for (TaskStatisticsVo statisticsVo : list) {
            TaskListParamVo paramVo = new TaskListParamVo();
            if (taskStatisticsVo.getStartDate() != null && taskStatisticsVo.getStopDate() != null) {
                paramVo.setStartDate(taskStatisticsVo.getStartDate());
                paramVo.setStopDate(taskStatisticsVo.getStopDate());
            }
            // 复核人
            paramVo.setReviewer(statisticsVo.getReceiverUserId().toString());
            // 检测人
            paramVo.setInspector(statisticsVo.getReceiverUserId().toString());
            //记录人
            paramVo.setRecorder(statisticsVo.getReceiverUserId().toString());
            //报告制作人
            paramVo.setReportProducer(statisticsVo.getReceiverUserId().toString());
            // 见习生：实习的新手
            paramVo.setProbationer(statisticsVo.getReceiverUserId().toString());
            // 实习生
            paramVo.setInterns(statisticsVo.getReceiverUserId().toString());
            // 辅助人员
            paramVo.setAuxiliaryPersonnel(statisticsVo.getReceiverUserId().toString());
            // 签发人
            paramVo.setReceiver(statisticsVo.getReceiverUserId().toString());
            Integer count = taskMapper.selectTaskTDetectionType(paramVo);
            // 已经接的工作量
            statisticsVo.setReceivedTaskVolume(String.valueOf(count));
        }
        PageInfo<TaskStatisticsVo> result = new PageInfo<>(list);
        return ResultUtil.success(result);
    }

    @Override
    public InputStream getAuthorizedSignatureListExport(TaskStatisticsVo taskStatisticsVo) throws IOException {
        taskStatisticsVo.setPageNum(1);
        taskStatisticsVo.setPageSize(100000);
        Result result = getAuthorizedSignatureList(taskStatisticsVo);
        PageInfo<TaskStatisticsVo> pageInfo = (PageInfo<TaskStatisticsVo>) result.getData();
        List<TaskStatisticsVo> list = pageInfo.getList();
// 我的工时统计-导出
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet = wb.createSheet("sheet0");
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1 = sheet.createRow(0);
        //创建单元格并设置单元格内容
        row1.createCell(0).setCellValue("名称");
        row1.createCell(1).setCellValue("已接任务");
        row1.createCell(2).setCellValue("完成任务");
        row1.createCell(3).setCellValue("已完成合计（工时）");
        if (CollectionUtil.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                TaskStatisticsVo statisticsVo = list.get(i);
                //在sheet里创建第二行
                HSSFRow row3 = sheet.createRow(i + 1);
                row3.createCell(0).setCellValue(statisticsVo.getReceiver());
                // 已接任务
                row3.createCell(1).setCellValue(statisticsVo.getReceivedTaskVolume() != null ? statisticsVo.getReceivedTaskVolume().toString() : "-");
                // 完成任务
                row3.createCell(2).setCellValue(statisticsVo.getCompletedTaskVolume() != null ? statisticsVo.getCompletedTaskVolume().toString() : "-");
                // 工时
                row3.createCell(3).setCellValue(statisticsVo.getWorkingHours() != null ? statisticsVo.getWorkingHours().toString() : "-");
            }
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    public Result getAuthorizedSignatureListDetails(TaskStatisticsVo taskStatisticsVo) {
        if (taskStatisticsVo.getPageNum() == null || taskStatisticsVo.getPageSize() == null) {
            return ResultUtil.error("分页参数不能为空");
        }
        if (taskStatisticsVo.getReceiverUserId() == null) {
            return ResultUtil.error("参数不能为空");
        }
        // 进行 查询分页。
        PageHelper.clearPage();
        PageHelper.startPage(taskStatisticsVo.getPageNum(), taskStatisticsVo.getPageSize());
        if (taskStatisticsVo.getStopDate() != null) {
            SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd");
            String startStr = startFormat.format(taskStatisticsVo.getStopDate());
            String[] split = startStr.split("-");
            LocalDate localDate = LocalDate.of(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
            LocalDate endDate = localDate.plusDays(1);
            Date date = Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            taskStatisticsVo.setStopDate(date);
        }
        List<TestTaskOrderWorkingHours> list = testTaskOrderWorkingHoursMapper.selectTaskOrderList(taskStatisticsVo);
        if (CollectionUtil.isNotEmpty(list)) {
            for (TestTaskOrderWorkingHours data : list) {
                LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, data.getTaskId());
                if (data.getWorkingHoursId() != null) {
                    queryWrapper.eq(TestTaskOrderWorkingHours::getWorkingHoursId, Long.valueOf(data.getWorkingHoursId()));
                }
                List<TestTaskOrderWorkingHours> testTaskOrderWorkingHoursList = testTaskOrderWorkingHoursMapper.selectList(queryWrapper);
                // 输出任务单下 人员比例信息
                StringBuffer stringBuffer = new StringBuffer();
                if (CollectionUtil.isNotEmpty(testTaskOrderWorkingHoursList)) {
                    for (TestTaskOrderWorkingHours taskOrderWorkingHours : testTaskOrderWorkingHoursList) {
                        stringBuffer.append(taskOrderWorkingHours.getUserName());
                        String msg = taskOrderWorkingHours.getProportion() != null ? taskOrderWorkingHours.getProportion() + "%" : "--";
                        stringBuffer.append(msg + " ");
                    }
                }
                data.setProportion(stringBuffer.toString());
            }
        }
        PageInfo<TestTaskOrderWorkingHours> result = new PageInfo<>(list);
        return ResultUtil.success(result);
    }

    @Override
    public Result getAuthorizedSignatureHours(TaskStatisticsVo taskStatisticsVo) {
        // 进行工时 总计
        PageHelper.clearPage();
        taskStatisticsVo.setPageSize(null);
        taskStatisticsVo.setPageNum(null);
        List<TaskStatisticsVo> list = testTeamDao.getRoleUserInformation(taskStatisticsVo);
        // 用户id集合
        List<Long> userIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (TaskStatisticsVo statisticsVo : list) {
                userIds.add(statisticsVo.getReceiverUserId());
            }
            // TODO: 11月9日 暂时替换 userIds = deptIds
            taskStatisticsVo.setLongList(userIds);
        }
        PageHelper.clearPage();
        taskStatisticsVo.setPageSize(null);
        taskStatisticsVo.setPageNum(null);
        if (taskStatisticsVo.getStopDate() != null) {
            SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd");
            String startStr = startFormat.format(taskStatisticsVo.getStopDate());
            String[] split = startStr.split("-");
            LocalDate localDate = LocalDate.of(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
            LocalDate endDate = localDate.plusDays(1);
            Date date = Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            taskStatisticsVo.setStopDate(date);
        }
        String totalData = testTaskOrderWorkingHoursMapper.selectAuthorizedSignatureHours(taskStatisticsVo);
        if (totalData != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("sumCount", String.format("%.2f", Double.parseDouble(totalData)));
            return ResultUtil.success(map);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sumCount", "0.00");
        return ResultUtil.success(map);
    }

    /**
     * 报告签发后：分配工时
     *
     * @param taskId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean endReportAllottedTime(Long taskId) {
        // 根据任务单 查询任务下对应操作信息
        TaskTestEntity taskDetails = taskMapper.selectTaskEntity(taskId);
        if (taskDetails == null) {
            return false;
        }
        //TODO:11月10 查询基础表信息 - 检测类型包含工时
        List<TestInitDataEntity> basisList = taskMapper.selectEntrustBasis(30);
        // 辅助人包括报告制作和辅助人员
        if (StringUtils.isNotEmpty(taskDetails.getReportProducer()) && StringUtils.isNotEmpty(taskDetails.getAuxiliaryPersonnel())) {
            // 任务单中 包含报告制作人与辅助人员 不为空 则平摊比例
            TestInitDataEntity reportProducerData = basisList.get(3);
            int zhi1 = Integer.parseInt(reportProducerData.getRemark()) / 2;
            reportProducerData.setRemark(String.valueOf(zhi1));
            basisList.set(3, reportProducerData);
            TestInitDataEntity auxiliaryPersonnel = basisList.get(4);
            int zhi2 = Integer.parseInt(auxiliaryPersonnel.getRemark()) / 2;
            auxiliaryPersonnel.setRemark(String.valueOf(zhi2));
            basisList.set(4, auxiliaryPersonnel);
        } else {
            // 辅助人员 工时 = 0
            TestInitDataEntity auxiliaryPersonnel = basisList.get(4);
            auxiliaryPersonnel.setRemark("0");
            basisList.set(4, auxiliaryPersonnel);
        }
        List<TestTaskOrderWorkingHours> countList = new ArrayList<>();
        // 使用 map 进行 数据统计 key = userId , value = 参数
        Map<Long, TestTaskOrderWorkingHours> mapData = new HashMap<>();
        // 直接返回任务单数据即可。
        // 检测人员
        if (StringUtils.isNotEmpty(taskDetails.getInspector())) {
            //  调用方法 去处理 签名信息进行截取
            methodSubstr(0, taskDetails.getInspector(), mapData, taskId, basisList);
        }
        // 记录人员
        if (StringUtils.isNotEmpty(taskDetails.getRecorder())) {
            //  调用方法 去处理 签名信息进行截取
            methodSubstr(1, taskDetails.getRecorder(), mapData, taskId, basisList);
        }
        // 复核人
        if (StringUtils.isNotEmpty(taskDetails.getReviewer())) {
            //  调用方法 去处理 签名信息进行截取
            methodSubstr(2, taskDetails.getReviewer(), mapData, taskId, basisList);
        }
        // 报告制作人
        if (StringUtils.isNotEmpty(taskDetails.getReportProducer())) {
            //  调用方法 去处理 签名信息进行截取
            methodSubstr(3, taskDetails.getReportProducer(), mapData, taskId, basisList);
        }
        // 接单人
        String name = "";
        if (StringUtils.isNotEmpty(taskDetails.getReceiver())) {
            //  调用方法 去处理 签名信息进行截取
            // 查找userId与name
            name = sysUserDao.getSysUserName(Long.valueOf(taskDetails.getReceiver()));
            methodSubstr(5, name + "&" + taskDetails.getReceiver(), mapData, taskId, basisList);
        }
        // 辅助人员
        if (StringUtils.isNotEmpty(taskDetails.getAuxiliaryPersonnel())) {
            //  调用方法 去处理 签名信息进行截取
            methodSubstr(4, taskDetails.getAuxiliaryPersonnel(), mapData, taskId, basisList);
        }
        // 进行循环迭代 mapData 数据
        if (CollectionUtil.isNotEmpty(mapData.keySet())) {
            for (Long key : mapData.keySet()) {
                TestTaskOrderWorkingHours data = mapData.get(key);
                countList.add(data);
            }
        }
        // 任务单号
        String taskCode = taskDetails.getTaskCode();
        // 接单人 =  来源
        if (StringUtils.isEmpty(name)) {
            name = sysUserDao.getSysUserName(Long.valueOf(taskDetails.getReceiver()));
        }
        // 通过任务单 获取样品信息
        List<String> sampleNames = taskMapper.getTaskSamples(taskDetails.getId());
        StringBuffer nameBuffer = new StringBuffer();
        if (CollectionUtil.isNotEmpty(sampleNames)) {
            for (String sampleName : sampleNames) {
                nameBuffer.append(sampleName + " ");
            }
        }
        // 工时id
        Long workingHoursId = GenID.getID();
//        // 通过任务单id 获取任务单下对应的检测项总工时
        String workingHours = renturnWorkingHours(taskId, workingHoursId, name);
        if (StringUtils.isNotEmpty(workingHours)) {
            for (TestTaskOrderWorkingHours taskOrderWorkingHours : countList) {
                // 总工时
                taskOrderWorkingHours.setTotalWorkingHours(workingHours);
                taskOrderWorkingHours.setAddOperator(name + "&" + taskDetails.getReceiver());
                taskOrderWorkingHours.setCreateTime(new Date());
                // 样品名称 = 任务单下 样品名称
                taskOrderWorkingHours.setSampleName(nameBuffer.toString());
                // 来源 = 任务单的下单人
                taskOrderWorkingHours.setSource(name);
                // 任务单号
                taskOrderWorkingHours.setTaskCode(taskCode);
                // 使用工时
                Double proportion = Double.parseDouble(taskOrderWorkingHours.getProportion());
                if (proportion == 0) {
                    // 工时 = 0
                    taskOrderWorkingHours.setWorkingHours("0");
                } else {
                    // 求比例工时
                    double one22 = Double.parseDouble(taskOrderWorkingHours.getProportion()) / 100d;
                    double zhi = Double.parseDouble(taskOrderWorkingHours.getTotalWorkingHours()) * one22;
                    String context = String.format("%.2f", zhi);
                    taskOrderWorkingHours.setWorkingHours(context);
                }
                taskOrderWorkingHours.setWorkingHoursId(String.valueOf(workingHoursId));
                testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
            }
        }
        return true;
    }

    /**
     * 计算百分比
     *
     * @param a
     * @param b
     * @return
     */
    public static double getPercentage(double a, double b) {
        double result = (a / b) * 100;
        DecimalFormat df = new DecimalFormat("#.###");
        return Double.parseDouble(df.format(result));
    }

    /**
     * 报告签发后： 存储工时信息时，处理业务信息
     * 1、报告签发： 任务单 已经生成过了检测工时，报告签发时，把任务单工时删除，重新生成。
     * 根据报告id 查询委托id 获得任务单列表。判断任务单工时生成规则（为试验操作时的则删除，为报告签发的则正常新增即可） 进行删除后，再新增。
     *
     * @param reportId 报告id
     * @param state    state = 0 报告签发、state = 1 报告驳回
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean handleWorkingHours(Long reportId, Integer state) {
        // 1、通过报告单 获取任务单工时列表。
        ReportRecordEntity detailById = reportMapper.getDetailById(reportId);
        if (detailById == null) {
            return false;
        }
        Long entrustId;
        //      读取委托id
        if (detailById.getType().equals("0")) {
            // 最终报告
            entrustId = detailById.getEntrustmentId();
        } else {
            // 中间报告
            entrustId = detailById.getEntrustId();

            return false;
        }
        List<Long> taskIds = testTaskOrderWorkingHoursMapper.getTaskList(entrustId);
        if (CollectionUtil.isEmpty(taskIds)) {
            // 查询是否是 旧任务单数据
            List<Long> oldTaskIds = testTaskOrderWorkingHoursMapper.getTaskOldList(entrustId);
            if (CollectionUtil.isEmpty(oldTaskIds)) {
                return false;
            }
            // 处理旧任务id集合 不包含 工时信息
            for (Long taskId : oldTaskIds) {
                LambdaQueryWrapper<TestTaskOrderWorkingHours> deleteTaskOrderWorkingHours = new LambdaQueryWrapper<>();
                deleteTaskOrderWorkingHours.eq(TestTaskOrderWorkingHours::getTaskId, taskId);
                testTaskOrderWorkingHoursMapper.delete(deleteTaskOrderWorkingHours);
                LambdaQueryWrapper<TestItemOrderWorkingHours> deleteItemOrderWorkingHours = new LambdaQueryWrapper<>();
                deleteItemOrderWorkingHours.eq(TestItemOrderWorkingHours::getTaskId, taskId);
                testItemOrderWorkingHoursMapper.delete(deleteItemOrderWorkingHours);
                // 根据taskId 循环新增工时数据
                endReportAllottedTime(taskId);
            }
            return true;
        }
        // 通过任务单id 获取工时列表
        List<TestTaskOrderWorkingHours> testTaskOrderWorkingHoursList = testTaskOrderWorkingHoursMapper.getTestTaskOrderWorkingHoursList(taskIds);
        if (state == 0) {
            // 报告发起签发：
            if (CollectionUtil.isEmpty(testTaskOrderWorkingHoursList)) {
                // 任务id集合 不包含 工时信息
                for (Long taskId : taskIds) {
                    // 查询任务单下所属检测项是否包含工时
                    List<TestItemOrderWorkingHours> listWorks = testItemOrderWorkingHoursMapper.selectTaskList(taskId);
                    Boolean status = false;
                    for (TestItemOrderWorkingHours testItemOrderWorkingHours : listWorks) {
                        if (StringUtils.isNotEmpty(testItemOrderWorkingHours.getWorkingHours())) {
                            status = true;
                        }
                    }
                    if (status) {
                        // 根据taskId 循环新增工时数据
                        reportIssuanceAllottedTime(taskId);
                    }
                }
            } else {
                // 任务id集合获取 得到 工时信息:
                //                      判断任务单工时生成规则（为试验操作时的则删除，为报告签发的则正常新增即可）
                for (TestTaskOrderWorkingHours workingHoursData : testTaskOrderWorkingHoursList) {
                    LambdaQueryWrapper<TestTaskOrderWorkingHours> deleteQueryWrapper = new LambdaQueryWrapper<>();
                    deleteQueryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, workingHoursData.getTaskId());
                    if (StringUtils.isEmpty(workingHoursData.getWorkingHoursId())) {
                        deleteQueryWrapper.isNull(TestTaskOrderWorkingHours::getWorkingHoursId);
                    } else {
                        deleteQueryWrapper.eq(TestTaskOrderWorkingHours::getWorkingHoursId, workingHoursData.getWorkingHoursId());
                    }
                    testTaskOrderWorkingHoursMapper.delete(deleteQueryWrapper);
                    LambdaQueryWrapper<TestItemOrderWorkingHours> itemDeleteQueryWrapper = new LambdaQueryWrapper<>();
                    if (StringUtils.isEmpty(workingHoursData.getWorkingHoursId())) {
                        itemDeleteQueryWrapper.isNull(TestItemOrderWorkingHours::getWorkingHoursId);
                    } else {
                        itemDeleteQueryWrapper.eq(TestItemOrderWorkingHours::getWorkingHoursId, workingHoursData.getWorkingHoursId());
                    }
                    itemDeleteQueryWrapper.eq(TestItemOrderWorkingHours::getTaskId, workingHoursData.getTaskId());
                    testItemOrderWorkingHoursMapper.delete(itemDeleteQueryWrapper);
                }
                for (Long taskId : taskIds) {
                    // 查询任务单下所属检测项是否包含工时
                    List<TestItemOrderWorkingHours> listWorks = testItemOrderWorkingHoursMapper.selectTaskList(taskId);
                    Boolean status = false;
                    for (TestItemOrderWorkingHours testItemOrderWorkingHours : listWorks) {
                        if (StringUtils.isNotEmpty(testItemOrderWorkingHours.getWorkingHours())) {
                            status = true;
                        }
                    }
                    if (status) {
                        // 根据taskId 循环新增工时数据
                        reportIssuanceAllottedTime(taskId);
                    }
                }
            }
        } else {
            // 报告驳回
            if (CollectionUtil.isEmpty(testTaskOrderWorkingHoursList)) {
                // 任务单id集合 不包含工时信息
                return true;
            } else {
                // 进行 日期的截取比较
                Date now = new Date();
                //实现将字符串转成⽇期类型
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String now1 = dateFormat.format(now);
                // 查询工时信息 ： 撤回的情况。 当天撤回。 工时清除 重新计算。
                for (TestTaskOrderWorkingHours workingHoursData : testTaskOrderWorkingHoursList) {
                    String now2 = dateFormat.format(workingHoursData.getCreateTime());
                    if (now1.equals(now2)) {
                        // now1 != now2 进行工时清除 重新计算
                        LambdaQueryWrapper<TestTaskOrderWorkingHours> deleteQueryWrapper = new LambdaQueryWrapper<>();
                        deleteQueryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, workingHoursData.getTaskId());
                        if (StringUtils.isEmpty(workingHoursData.getWorkingHoursId())) {
                            deleteQueryWrapper.isNull(TestTaskOrderWorkingHours::getWorkingHoursId);
                        } else {
                            deleteQueryWrapper.eq(TestTaskOrderWorkingHours::getWorkingHoursId, workingHoursData.getWorkingHoursId());
                        }
                        testTaskOrderWorkingHoursMapper.delete(deleteQueryWrapper);
                        LambdaQueryWrapper<TestItemOrderWorkingHours> itemDeleteQueryWrapper = new LambdaQueryWrapper<>();
                        if (StringUtils.isEmpty(workingHoursData.getWorkingHoursId())) {
                            itemDeleteQueryWrapper.isNull(TestItemOrderWorkingHours::getWorkingHoursId);
                        } else {
                            itemDeleteQueryWrapper.eq(TestItemOrderWorkingHours::getWorkingHoursId, workingHoursData.getWorkingHoursId());
                        }
                        itemDeleteQueryWrapper.eq(TestItemOrderWorkingHours::getTaskId, workingHoursData.getTaskId());
                        testItemOrderWorkingHoursMapper.delete(itemDeleteQueryWrapper);
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean testCommit(List<Long> taskIds) {

        // 任务id集合 不包含 工时信息
        for (Long taskId : taskIds) {
            // 根据taskId 循环新增工时数据
//            endReportAllottedTime(taskId);
            reportIssuanceAllottedTime(taskId);
        }
        return true;
    }

    /**
     * 根据taskId 进行获取检测项 存储工时对应检测项信息 返回总工时。
     *
     * @param taskId
     * @param workingHoursId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String renturnWorkingHours(Long taskId, Long workingHoursId, String source) {
        PageHelper.clearPage();
        // 1、 根据taskId 获取检测项工时。
        List<TestItemOrderWorkingHours> workingHoursList = testItemOrderWorkingHoursMapper.selectTaskList(taskId);
        // 检测项工时-实现阶梯计算。
        if (CollectionUtil.isNotEmpty(workingHoursList)) {
            methodStepCharging(workingHoursList);
        }
        // 1.1 根据taskId 获取检测项工时详情 可以为空
        LambdaQueryWrapper<TestItemOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestItemOrderWorkingHours::getTaskId, taskId);
        List<TestItemOrderWorkingHours> itemworkingHoursList = testItemOrderWorkingHoursMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(itemworkingHoursList)) {
            // 根据任务单id 查询 检测项工时 已经存在
            //                             与 workingHoursList 进行比对后 获取 差集 后 进行新增。
            // 检测项详情的差集
            List<TestItemOrderWorkingHours> difference = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(workingHoursList)) {
                for (TestItemOrderWorkingHours element : workingHoursList) {
                    boolean found = false;
                    for (TestItemOrderWorkingHours compareElement : itemworkingHoursList) {
                        if (element.getSampleId().equals(compareElement.getSampleId()) && element.getCheckItemId().equals(compareElement.getCheckItemId())) {
                            found = true;
                            LambdaQueryWrapper<TestItemOrderWorkingHours> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                            lambdaQueryWrapper.eq(TestItemOrderWorkingHours::getTaskId, taskId);
                            lambdaQueryWrapper.eq(TestItemOrderWorkingHours::getWorkingHoursId, workingHoursId);
                            lambdaQueryWrapper.eq(TestItemOrderWorkingHours::getItemId, compareElement.getItemId());
                            compareElement.setWorkingHours(element.getWorkingHours());
                            compareElement.setUpdateTime(new Date());
                            testItemOrderWorkingHoursMapper.update(compareElement, lambdaQueryWrapper);
                            break;
                        }
                    }
                    if (!found) {
                        difference.add(element);
                    }
                }
            }
            if (CollectionUtil.isNotEmpty(difference)) {
                for (TestItemOrderWorkingHours itemOrderWorkingHours : difference) {
                    itemOrderWorkingHours.setTaskId(taskId);
                    itemOrderWorkingHours.setWorkingHoursId(workingHoursId);
                    itemOrderWorkingHours.setSource(source);
                    // 处理工时
                    try {
                        double zhi = Double.parseDouble(itemOrderWorkingHours.getWorkingHours());
                        String context = String.format("%.2f", zhi);
                        itemOrderWorkingHours.setWorkingHours(context);
                    } catch (Exception e) {
                        // 总工时 转double 失败的进 Exception
                        itemOrderWorkingHours.setWorkingHours("0");
                    }
                    itemOrderWorkingHours.setCreateTime(new Date());
                    // 2、 存储检测项工时信息
                    testItemOrderWorkingHoursMapper.insert(itemOrderWorkingHours);
                }
            }
        } else {
            // 执行新增操作即可。
            if (CollectionUtil.isNotEmpty(workingHoursList)) {
                for (TestItemOrderWorkingHours itemOrderWorkingHours : workingHoursList) {
                    itemOrderWorkingHours.setTaskId(taskId);
                    itemOrderWorkingHours.setWorkingHoursId(workingHoursId);
                    itemOrderWorkingHours.setSource(source);
                    // 处理工时
                    try {
                        double zhi = Double.parseDouble(itemOrderWorkingHours.getWorkingHours());
                        String context = String.format("%.2f", zhi);
                        itemOrderWorkingHours.setWorkingHours(context);
                    } catch (Exception e) {
                        // 总工时 转double 失败的进 Exception
                        itemOrderWorkingHours.setWorkingHours("0");
                    }
                    itemOrderWorkingHours.setCreateTime(new Date());
                    // 2、 存储检测项工时信息
                    testItemOrderWorkingHoursMapper.insert(itemOrderWorkingHours);
                }
            }
        }
        // 3、 返回检测项工时
        //  根据 workingHoursId 与 taskId 统计检测项id 条数是否存在
        LambdaQueryWrapper<TestItemOrderWorkingHours> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(TestItemOrderWorkingHours::getTaskId, taskId);
        queryWrapper1.eq(TestItemOrderWorkingHours::getWorkingHoursId, workingHoursId);
        List<TestItemOrderWorkingHours> totalWorkingHours = testItemOrderWorkingHoursMapper.selectList(queryWrapper1);
        if (CollectionUtil.isNotEmpty(totalWorkingHours)) {
            return testItemOrderWorkingHoursMapper.getTotalWorkingHours(taskId, workingHoursId);
        } else {
            return null;
        }
    }

    /**
     * 执行新增 工时信息
     *
     * @param taskId
     * @param workingHoursId
     * @param source
     */
    @Transactional(rollbackFor = Exception.class)
    public void inserWorkingHours(Long taskId, Long workingHoursId, String source) {
        PageHelper.clearPage();
        // 1、 根据taskId 获取检测项工时。
        List<TestItemOrderWorkingHours> workingHoursList = testItemOrderWorkingHoursMapper.selectTaskList(taskId);
        // 1.1 根据taskId 获取检测项工时详情 可以为空
        LambdaQueryWrapper<TestItemOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestItemOrderWorkingHours::getTaskId, taskId);
        List<TestItemOrderWorkingHours> itemworkingHoursList = testItemOrderWorkingHoursMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(itemworkingHoursList)) {
            // 根据任务单id 查询 检测项工时 已经存在
            //                             与 workingHoursList 进行比对后 获取 差集 后 进行新增。
            // 检测项详情的差集
            List<TestItemOrderWorkingHours> difference = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(workingHoursList)) {
                for (TestItemOrderWorkingHours element : workingHoursList) {
                    boolean found = false;
                    for (TestItemOrderWorkingHours compareElement : itemworkingHoursList) {
                        if (element.getSampleId().equals(compareElement.getSampleId()) && element.getCheckItemId().equals(compareElement.getCheckItemId())) {
                            found = true;
                            LambdaQueryWrapper<TestItemOrderWorkingHours> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                            lambdaQueryWrapper.eq(TestItemOrderWorkingHours::getTaskId, taskId);
                            lambdaQueryWrapper.eq(TestItemOrderWorkingHours::getWorkingHoursId, workingHoursId);
                            lambdaQueryWrapper.eq(TestItemOrderWorkingHours::getItemId, compareElement.getItemId());
                            compareElement.setWorkingHours(element.getWorkingHours());
                            compareElement.setUpdateTime(new Date());
                            testItemOrderWorkingHoursMapper.update(compareElement, lambdaQueryWrapper);
                            break;
                        }
                    }
                    if (!found) {
                        difference.add(element);
                    }
                }
            }
            if (CollectionUtil.isNotEmpty(difference)) {
                for (TestItemOrderWorkingHours itemOrderWorkingHours : difference) {
                    itemOrderWorkingHours.setTaskId(taskId);
                    itemOrderWorkingHours.setWorkingHoursId(workingHoursId);
                    itemOrderWorkingHours.setSource(source);
                    // 处理工时
                    try {
                        double zhi = Double.parseDouble(itemOrderWorkingHours.getWorkingHours());
                        String context = String.format("%.2f", zhi);
                        itemOrderWorkingHours.setWorkingHours(context);
                    } catch (Exception e) {
                        // 总工时 转double 失败的进 Exception
                        itemOrderWorkingHours.setWorkingHours("0");
                    }
                    itemOrderWorkingHours.setCreateTime(new Date());
                    // 2、 存储检测项工时信息
                    testItemOrderWorkingHoursMapper.insert(itemOrderWorkingHours);
                }
            }
        } else {
            // 执行新增操作即可。
            if (CollectionUtil.isNotEmpty(workingHoursList)) {
                for (TestItemOrderWorkingHours itemOrderWorkingHours : workingHoursList) {
                    itemOrderWorkingHours.setTaskId(taskId);
                    itemOrderWorkingHours.setWorkingHoursId(workingHoursId);
                    itemOrderWorkingHours.setSource(source);
                    // 处理工时
                    try {
                        double zhi = Double.parseDouble(itemOrderWorkingHours.getWorkingHours());
                        String context = String.format("%.2f", zhi);
                        itemOrderWorkingHours.setWorkingHours(context);
                    } catch (Exception e) {
                        // 总工时 转double 失败的进 Exception
                        itemOrderWorkingHours.setWorkingHours("0");
                    }
                    itemOrderWorkingHours.setCreateTime(new Date());
                    // 2、 存储检测项工时信息
                    testItemOrderWorkingHoursMapper.insert(itemOrderWorkingHours);
                }
            }
        }
    }

    @Override
    public List<HourCount> exportHours(TaskStatisticsVo bean) {
        List<HourCount> hourCounts = testTaskOrderWorkingHoursMapper.exportHours(bean.getStartDate(), bean.getStopDate());
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(hourCounts)) {
            //处理部门名称
            //兼容旧团队
            //List<TestTeam> list = teamMapper.getTeamsByUserId(ShiroUtils.getUserInfo().getUserId());
            //List<TestTeam> list = testTeamDao.getTeamsByPids(hourCounts);
            for (HourCount hourCount : hourCounts) {
                hourCount.setDoubleHours(Double.parseDouble(hourCount.getHours()));
                if (StringUtils.isEmpty(hourCount.getTeamName())){
                    hourCount.setDeptName("辅助人员");
                }else {
                    hourCount.setDeptName(hourCount.getTeamName());
                }
            }
            //计算部门总积分
            double totalScore = hourCounts.stream().mapToDouble(HourCount::getDoubleHours).sum();
            //计算个人所占部门比例
            for (HourCount hourCount :hourCounts){
                hourCount.setTeamHours(totalScore);
                double percentage = getPercentage(hourCount.getDoubleHours(), totalScore);
                hourCount.setPercentage(percentage+"%");
            }
            //计算部门产值和个人绩效占比
            List<TeamOutputValueVo> teamOutputValueVos = statisticsMapper.teamStatistics231219(bean.getSTime()+" 00:00:00", bean.getETime()+" 23:59:59", null);
            //统计总产值
            for (TeamOutputValueVo outputValueVo :teamOutputValueVos){
                outputValueVo.setPrice(outputValueVo.getActualReportPrice());
            }
            Double price = teamOutputValueVos.stream().mapToDouble(TeamOutputValueVo::getPrice)
                    .sum();
            for (HourCount hourCount :hourCounts){
                hourCount.setTeamPrice(price.intValue());
                if (price != null) {
                    double result = price * (0.05);
                    result = Math.abs(result);
                    hourCount.setPerformance(result);
                }
            }
        }
        return hourCounts;
    }

    @Override
    public void handExcelData(List<HourCount> list, Workbook workbook, TaskStatisticsVo bean, HttpServletResponse response) throws Exception {
        Worksheet worksheet = workbook.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        cells.get("A1").setValue("积分统计表（" + DateUtil.formatDate(bean.getStartDate()) + "～" + DateUtil.formatDate(bean.getDate()) + "）");
        int index = 3;
        for (HourCount hourCount : list) {
            cells.get("A" + index).setValue(hourCount.getUserName());
            cells.get("B" + index).setValue(hourCount.getDeptName());
            cells.get("C" + index).setValue(hourCount.getTeamHours());
            cells.get("D" + index).setValue(hourCount.getTeamPrice());
            cells.get("E" + index).setValue(hourCount.getPerformance());
            if ("辅助人员".equals(hourCount.getDeptName())) {
                cells.get("F" + index).setValue("辅助人员");
            } else {
                cells.get("F" + index).setValue(hourCount.getDeptName());
            }
            cells.get("G" + index).setValue(hourCount.getDoubleHours());
            cells.get("H" + index).setValue(hourCount.getPercentage());
            String percentage = hourCount.getPercentage();
            Double performance = hourCount.getPerformance();
            double aValue = Double.parseDouble(percentage.replace("%", "")) / 100;
            double result = Math.abs(aValue * performance);
            cells.get("I" + index).setValue(result);
            index++;
        }
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/x-msdownload");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + "gongshi.xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.save(outputStream, SaveFormat.XLSX);
    }

    /**
     * 生成工时:钉钉 任务单通知
     *
     * @param planCreator
     * @param publisher
     */
    void methodDingTalkNotification(String planCreator, String taskCode) {

        DingNotifyUtils dingNotifyUtils = new DingNotifyUtils();

        // 通知时间
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String nowString = simpleDateFormat.format(now);
        // 当前年份
        SimpleDateFormat yyyyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String yyyyString = yyyyFormat.format(now);
        // 查询钉钉id
        String dingId = "";
        try {
            // 获取 任务单下检测人信息 userId
            LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserEntity::getUserId, planCreator);
            SysUserEntity userDetails = sysUserDao.selectOne(queryWrapper);
            dingId = userDetails.getDingUserId();
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(nowString + "工时生成通知");
            StringBuffer contextBuffer = new StringBuffer();
            contextBuffer.append(yyyyString + " 任务单号为：" + taskCode + "请登录公司系统生成分配工时，特此通知！");
            dingNotifyUtils.OAWorkNotice(dingId, titleBuffer.toString(), "系统推送", contextBuffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 报告签发后：分配工时
     *
     * @param taskId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean reportIssuanceAllottedTime(Long taskId) {
        // 根据任务单 查询任务下对应操作信息
        TaskTestEntity taskDetails = taskMapper.selectTaskEntity(taskId);
        if (taskDetails == null) {
            return false;
        }
        //TODO:11月10 查询基础表信息 - 检测类型包含工时
        List<TestInitDataEntity> sqlBasisList = taskMapper.selectEntrustBasis(30);
        String[] strings = new String[sqlBasisList.size()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = sqlBasisList.get(i).getRemark();
        }
        // 分组设置任务大厅 获取工时信息
        HashMap<Integer, List<TestTaskOrderWorkingHours>> bitTaskRelList = methodReturnHashMap(taskId, sqlBasisList, strings);
        if (CollectionUtil.isEmpty(bitTaskRelList.keySet())) {
            return false;
        }
        // 获取分组检测项信息 进行合并 存储。
        List<TestInitDataEntity> updateSqlBasisList = sqlBasisList.stream().collect(Collectors.toList());
        for (int i = 0; i < strings.length; i++) {
            TestInitDataEntity dataEntity = updateSqlBasisList.get(i);
            dataEntity.setRemark(strings[i]);
            updateSqlBasisList.set(i, dataEntity);
        }
        // 处理工时信息
        addTestTaskOrderWorkingHoursMapper(taskId, taskDetails.getEntrustmentId(), bitTaskRelList, updateSqlBasisList);

        //  报告签发时 设置人员比例信息、 及进行通知授权签字人信息。
        try {

            testTaskPoolService.informationSubstitution(taskDetails.getTaskCode());
            // 钉钉通知授权签字人 任务单工时已生成
//            methodDingTalkNotification(taskDetails.getReceiver(), taskDetails.getTaskCode());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 进行阶梯 ： 处理检测项次数，对应工时信息 调整比例。
     *
     * @param itemOrderWorkingHoursList
     */
    public void methodStepCharging(List<TestItemOrderWorkingHours> itemOrderWorkingHoursList) {
        // 获取 check_item_id 集合 查询 阶梯表。
        Set<Integer> checkItemIdSet = new HashSet<>();
        for (TestItemOrderWorkingHours itemOrderWorkingHours : itemOrderWorkingHoursList) {
            checkItemIdSet.add(itemOrderWorkingHours.getCheckItemId());
        }
        // 查询 check_item_id 与工时表 test_item_work_hour_ladder 检测项 工时阶梯。
        List<TestItemWorkHourLadderVo> itemOrderWorkingHoursVos = testItemOrderWorkingHoursMapper.selectTestItemWorkHourLadderVos(checkItemIdSet);
        // 检测项工时信息 遍历
        for (TestItemOrderWorkingHours testItemOrderWorkingHours : itemOrderWorkingHoursList) {
            // 待计算 check_item_id 工时信息集合
            List<TestItemWorkHourLadderVo> testItemWorkHourLadderVos = new ArrayList<>();
            // 检测项阶梯不为空 && 当前检测项工时不为空
            if (CollectionUtil.isNotEmpty(itemOrderWorkingHoursVos) && StringUtils.isNotEmpty(testItemOrderWorkingHours.getWorkingHours())) {
                // 检测项工时阶梯不为空的话。 遍历数据
                for (TestItemWorkHourLadderVo itemWorkHourLadderVo : itemOrderWorkingHoursVos) {
                    // 检测项工时中 check_item_id 与阶梯中 check_item_id 相同的话，提出来  计算。
                    if (testItemOrderWorkingHours.getCheckItemId().equals(itemWorkHourLadderVo.getCheckItemId())) {
                        // 进行汇总
                        testItemWorkHourLadderVos.add(itemWorkHourLadderVo);
                    }
                }
            }
            // if testItemWorkHourLadderVos != null
            if (CollectionUtil.isNotEmpty(testItemWorkHourLadderVos)) {
                // 找出最匹配区间数值。
                for (TestItemWorkHourLadderVo itemWorkHourLadderVo : testItemWorkHourLadderVos) {
                    // 当前检测项次数 >= 最小值 && 当前检测项次数 <= 最小值
                    if (testItemOrderWorkingHours.getTimes() >= itemWorkHourLadderVo.getStartTimes() && testItemOrderWorkingHours.getTimes() <= itemWorkHourLadderVo.getEndTimes()) {
                        // 工时信息 填报: 检测项工时 = 工时 * 阶梯工时比例 保留小数点后四位。
                        // 工时
                        BigDecimal zhi1 = BigDecimal.valueOf(Double.parseDouble(testItemOrderWorkingHours.getWorkingHours())).setScale(4, BigDecimal.ROUND_HALF_UP);
                        // 阶梯工时比例
                        BigDecimal zhi2 = BigDecimal.valueOf(Double.parseDouble(itemWorkHourLadderVo.getDecimalPoint())).setScale(4, BigDecimal.ROUND_HALF_UP);
                        // 检测项工时 = 工时 * 阶梯工时比例 保留小数点后四位。
                        BigDecimal workingHours = zhi1.multiply(zhi2).setScale(4, BigDecimal.ROUND_HALF_UP);
                        testItemOrderWorkingHours.setWorkingHours(String.valueOf(workingHours));
                    }
                }
            }
        }
    }

    /**
     * 分组设置任务大厅 获取工时信息
     *
     * @param taskId
     * @param sqlBasisList
     * @param strings
     * @return
     */
    public HashMap<Integer, List<TestTaskOrderWorkingHours>> methodReturnHashMap(Long taskId, List<TestInitDataEntity> sqlBasisList, String[] strings) {
        // 通过任务单id 查询所有的 任务大厅 分配的检测项列表
        List<TestCheckItemsTaskRel> bitValueList = testCheckItemsTaskRelMapper.selectAllDataBitValue(taskId);
        // 通过任务单获取检测项工时信息
        List<TestItemOrderWorkingHours> itemOrderWorkingHoursList = testItemOrderWorkingHoursMapper.selectTaskList(taskId);
        // 调用方法：进行阶梯计费
        if (CollectionUtil.isNotEmpty(itemOrderWorkingHoursList)) {
            methodStepCharging(itemOrderWorkingHoursList);
        }
        // 分组设置任务大厅 工时信息:key = 分组序号 ，value = 集合工时人员信息
        HashMap<Integer, List<TestCheckItemsTaskRel>> bitValueMap = new HashMap<>();
        // 分组设置任务大厅 获取工时信息
        HashMap<Integer, List<TestTaskOrderWorkingHours>> bitTaskRelList = new HashMap<>();
        for (TestCheckItemsTaskRel taskRel : bitValueList) {
            if (CollectionUtil.isNotEmpty(bitValueMap.get(taskRel.getBitValue()))) {
                List<TestCheckItemsTaskRel> list = bitValueMap.get(taskRel.getBitValue());
                list.add(taskRel);
                bitValueMap.put(taskRel.getBitValue(), list);
            } else {
                List<TestCheckItemsTaskRel> list = new ArrayList<>();
                list.add(taskRel);
                bitValueMap.put(taskRel.getBitValue(), list);
            }
        }
        List<TestInitDataEntity> updateSqlBasisList = sqlBasisList.stream().collect(Collectors.toList());
        // 获取每组的工时信息：遍历key
        for (Integer key : bitValueMap.keySet()) {
            // 初始化默认人员比例。
            for (int i = 0; i < strings.length; i++) {
                TestInitDataEntity dataEntity = updateSqlBasisList.get(i);
                dataEntity.setRemark(strings[i]);
                updateSqlBasisList.set(i, dataEntity);
            }
            List<TestCheckItemsTaskRel> itemList = bitValueMap.get(key);
            // 工具类：设置人员工时比例
            WorkHourRatioMethodUtils workHourRatioMethodUtils = new WorkHourRatioMethodUtils();
            List<TestTaskOrderWorkingHours> list = workHourRatioMethodUtils.searchingLoop(updateSqlBasisList, itemList, itemOrderWorkingHoursList);
            if (CollectionUtil.isNotEmpty(list)) {
                bitTaskRelList.put(key, list);
            }
        }
        return bitTaskRelList;
    }

    /**
     * 任务流转单-折扣率
     *
     * @param testEntrustedTaskRelList
     * @return
     */
    public String hourDiscount(List<TestEntrustedTaskRelEntity> testEntrustedTaskRelList) {

        // 进行比较 流转日期 >= 与当前签发时间
        Date taskFlowDate = null;
        for (TestEntrustedTaskRelEntity testEntrustedTaskRelEntity : testEntrustedTaskRelList) {
            if (testEntrustedTaskRelEntity.getType() == 0) {
                taskFlowDate = testEntrustedTaskRelEntity.getTaskFlowDate();
            }
        }

        try {
            // 当前签发时间
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date nowDate = new Date();
            // 去除当前时间 时分秒
            Date currentLatestTime = format.parse(format.format(nowDate));
            // 任务单 流转时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(taskFlowDate);
            Date lastDayOfMonth = calendar.getTime();
            // 任务单流转时间（2024-09-09 ) < 当前时间 2024-09-09
            if (lastDayOfMonth.before(currentLatestTime)) {
                return "0.9";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "1";
    }

    /**
     * 分配工时信息
     *
     * @param taskId
     * @param entrustId
     * @param bitTaskRelList 分组设置任务大厅 获取工时信息
     * @param sqlBasisList   查询基础表信息 - 检测类型包含工时
     */
    public void addTestTaskOrderWorkingHoursMapper(Long taskId, Long entrustId, HashMap<Integer, List<TestTaskOrderWorkingHours>> bitTaskRelList, List<TestInitDataEntity> sqlBasisList) {
        //  人员对应的检测工时及比例信息。
        // 使用map 进行用户数据合并
        Map<Long, TestTaskOrderWorkingHours> taskRelMap = new HashMap<>();
        // 总工时
        // 私有方法：人员多组工时合并工时
        String totalWorkingHours = methodUserMerge(bitTaskRelList, taskRelMap);

        // TODO: 9月11 暂时停止   通过任务单id 获取 流转日期  与报告签发时间 是否一致
//        LambdaQueryWrapper<TestEntrustedTaskRelEntity> entityLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        entityLambdaQueryWrapper.eq(TestEntrustedTaskRelEntity::getEntrustId, entrustId);
//        List<TestEntrustedTaskRelEntity> testEntrustedTaskRelList = testEntrustedTaskRelDao.selectList(entityLambdaQueryWrapper);
//        String discount = "1";
//        if (CollectionUtil.isNotEmpty(testEntrustedTaskRelList)) {
//            discount = hourDiscount(testEntrustedTaskRelList);
//            totalWorkingHours = BigDecimal.valueOf(Double.valueOf(totalWorkingHours)).multiply(BigDecimal.valueOf(Double.valueOf(discount))).toString();
//        }

        // 展示数据
        TaskTestEntity taskDetails = taskMapper.selectTaskEntity(taskId);
        // 签发人： 获取
        Boolean issuerInformation = false;
        for (Long userId : taskRelMap.keySet()) {
            // 签收人为
            if (Long.valueOf(taskDetails.getReceiver()).equals(userId)) {
                issuerInformation = true;
            }
        }
        // 人员拥有的检测项工时信息调整比例。
        for (Long userId : taskRelMap.keySet()) {
            TestTaskOrderWorkingHours data = taskRelMap.get(userId);
            // 进行 计算比例。
            // taskRelMap中存在 任务单签收人 比例追加 签收人 sqlBasisList.get(5);
            if (Long.valueOf(taskDetails.getReceiver()).equals(userId) && issuerInformation) {
                // 设置比例。
                TestInitDataEntity dataEntity = sqlBasisList.get(5);
                // 当前符合条件 增加工时 = 百分比 * 总工时
                // 百分比
                BigDecimal proportion = BigDecimal.valueOf(Double.valueOf(dataEntity.getRemark()) / 100).setScale(4, BigDecimal.ROUND_HALF_UP);
                // 总工时
                BigDecimal totalWorkingHoursBig = BigDecimal.valueOf(Double.valueOf(totalWorkingHours)).setScale(4, BigDecimal.ROUND_HALF_UP);
                //  增加工时 = 百分比 * 总工时
                BigDecimal addWorkingHours = proportion.multiply(totalWorkingHoursBig).setScale(4, BigDecimal.ROUND_HALF_UP);
                //所占工时 保留四位小数。
                BigDecimal workingHours = BigDecimal.valueOf(Double.valueOf(data.getWorkingHours())).setScale(4, BigDecimal.ROUND_HALF_UP);
                BigDecimal he = workingHours.add(addWorkingHours);
                // 补充信息 检测类型
                data.setDetectionType(data.getDetectionType() + "、" + dataEntity.getName());
                data.setWorkingHours(String.valueOf(he));
            }
            // 总工时
            data.setTotalWorkingHours(totalWorkingHours);
            BigDecimal totalWorkingHoursBig = BigDecimal.valueOf(Double.valueOf(totalWorkingHours)).setScale(4, BigDecimal.ROUND_HALF_UP);

            //所占工时 保留四位小数。
            BigDecimal workingHours = BigDecimal.valueOf(Double.valueOf(data.getWorkingHours())).setScale(4, BigDecimal.ROUND_HALF_UP);

            // 百分比 = 所占工时 / 总工时 * 100 保留两位小数
            //当前人的工时 = 比例 * zhi 保留四位小数。
            BigDecimal he = workingHours.divide(totalWorkingHoursBig, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(4, BigDecimal.ROUND_HALF_UP);
            // 所占工时
            data.setProportion(String.valueOf(he));
            taskRelMap.put(userId, data);
        }
        // 签发人不参与检测组中：新建
        if (!issuerInformation) {
            // 设置比例。
            TestInitDataEntity dataEntity = sqlBasisList.get(5);
            // 当前符合条件 增加工时 = 百分比 * 总工时
            // 百分比
            BigDecimal proportion = BigDecimal.valueOf(Double.valueOf(dataEntity.getRemark()) / 100).setScale(4, BigDecimal.ROUND_HALF_UP);
            // 总工时
            BigDecimal totalWorkingHoursBig = BigDecimal.valueOf(Double.valueOf(totalWorkingHours)).setScale(4, BigDecimal.ROUND_HALF_UP);
            //  增加工时 = 百分比 * 总工时
            BigDecimal addWorkingHours = proportion.multiply(totalWorkingHoursBig).setScale(4, BigDecimal.ROUND_HALF_UP);
            TestTaskOrderWorkingHours data = new TestTaskOrderWorkingHours();
            // 使用人id
            data.setUserId(Long.valueOf(taskDetails.getReceiver()));
            // 接单人name
            String name = sysUserDao.getSysUserName(Long.valueOf(taskDetails.getReceiver()));
            data.setUserName(name);
            // 接单类型
            data.setDetectionType(dataEntity.getName());
            //总工时
            data.setTotalWorkingHours(String.valueOf(totalWorkingHours));
            // 百分比
            data.setProportion(dataEntity.getRemark());
            // 工时信息
            data.setWorkingHours(String.valueOf(addWorkingHours));
            // 新增签发人信息。
            taskRelMap.put(data.getUserId(), data);
        }
        // 把调整后的任务单中人员、工时、比例信息、总工时 动态处理、或新增、或更新操作。
        updateOrAddTaskRelMap(taskId, taskRelMap, taskDetails, null);
    }

    /**
     * 人员多组工时合并工时
     *
     * @param bitTaskRelList
     * @param taskRelMap
     */
    public String methodUserMerge(HashMap<Integer, List<TestTaskOrderWorkingHours>> bitTaskRelList, Map<Long, TestTaskOrderWorkingHours> taskRelMap) {
        String totalWorkingHours = "0.0";
        for (Integer key : bitTaskRelList.keySet()) {
            List<TestTaskOrderWorkingHours> list = bitTaskRelList.get(key);
            // 当前组的工时
            BigDecimal zhi1 = BigDecimal.valueOf(Double.parseDouble(list.get(0).getTotalWorkingHours())).setScale(4, BigDecimal.ROUND_HALF_UP);
            BigDecimal zhi2 = BigDecimal.valueOf(Double.parseDouble(totalWorkingHours)).setScale(4, BigDecimal.ROUND_HALF_UP);
            BigDecimal workingHours = zhi1.add(zhi2);
            totalWorkingHours = String.valueOf(workingHours);
            // 拆分组数据进行
            for (TestTaskOrderWorkingHours taskRel : list) {
                if (taskRelMap.get(taskRel.getUserId()) != null) {
                    // 第一组工时
                    TestTaskOrderWorkingHours data = taskRelMap.get(taskRel.getUserId());
                    // 进行两组 检测类型进行合并。
                    Set<String> stringSet = new HashSet<>();
                    String[] strings = data.getDetectionType().split("、");
                    for (int i = 0; i < strings.length; i++) {
                        stringSet.add(strings[i]);
                    }
                    String[] strings2 = taskRel.getDetectionType().split("、");
                    for (int j = 0; j < strings2.length; j++) {
                        stringSet.add(strings2[j]);
                    }
                    StringBuffer stringBuffer = new StringBuffer();
                    // 遍历set数据
                    for (String str : stringSet) {
                        stringBuffer.append(str);
                        stringBuffer.append("、");
                    }
                    data.setDetectionType(stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
                    // 进行两组 获取工时 相加。
                    //第一组的工时
                    BigDecimal value1 = BigDecimal.valueOf(Double.parseDouble(data.getWorkingHours())).setScale(4, BigDecimal.ROUND_HALF_UP);
                    // 第二组工时
                    BigDecimal value2 = BigDecimal.valueOf(Double.parseDouble(taskRel.getWorkingHours())).setScale(4, BigDecimal.ROUND_HALF_UP);
                    BigDecimal he = value1.add(value2).setScale(4, BigDecimal.ROUND_HALF_UP);
                    data.setWorkingHours(String.valueOf(he));
                    taskRelMap.put(data.getUserId(), data);
                } else {
                    // 如果两组 都无 则新增即可。
                    taskRelMap.put(Long.valueOf(taskRel.getUserId()), taskRel);
                }
            }
        }
        return totalWorkingHours;
    }

    /**
     * 动态增加或更新工时信息。
     * 增加item详情工时数据。
     *
     * @param taskId
     * @param taskRelMap
     * @param taskDetails
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrAddTaskRelMap(Long taskId, Map<Long, TestTaskOrderWorkingHours> taskRelMap, TaskTestEntity taskDetails, String discount) {
        // 工时id
        Long workingHoursId = GenID.getID();
        // 任务单号
        String taskCode = taskDetails.getTaskCode();
        // 接单人 =  来源
        String name = "";
        if (StringUtils.isEmpty(name)) {
            name = sysUserDao.getSysUserName(Long.valueOf(taskDetails.getReceiver()));
        }
        // 查询任务单工时是否存在
        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, taskId);
        List<TestTaskOrderWorkingHours> workingHoursCount = testTaskOrderWorkingHoursMapper.selectList(queryWrapper);
        // 通过taskId 判断 工时是否存在 存在则更新操作、不存在则新增。
        if (CollectionUtil.isNotEmpty(workingHoursCount)) {
            // 赋值
            workingHoursId = Long.valueOf(workingHoursCount.get(0).getWorkingHoursId());
        }
        // 根据taskId 进行获取检测项 存储工时对应检测项信息 返回总工时。
        inserWorkingHours(taskId, workingHoursId, name);
        // 通过任务单 获取样品信息
        List<String> sampleNames = taskMapper.getTaskSamples(taskDetails.getId());
        StringBuffer nameBuffer = new StringBuffer();
        if (CollectionUtil.isNotEmpty(sampleNames)) {
            for (String sampleName : sampleNames) {
                nameBuffer.append(sampleName + " ");
            }
        }
        List<TestTaskOrderWorkingHours> list = new ArrayList<>();
        // 待删除的人员集合
        List<Long> deleteUserIds = new ArrayList<>();
        for (Long userId : taskRelMap.keySet()) {
            TestTaskOrderWorkingHours taskOrderWorkingHours = taskRelMap.get(userId);
            taskOrderWorkingHours.setAddOperator(name + "&" + taskDetails.getReceiver());
            taskOrderWorkingHours.setCreateTime(new Date());
            // 样品名称 = 任务单下 样品名称
            taskOrderWorkingHours.setSampleName(nameBuffer.toString());
            // 来源 = 任务单的下单人
            taskOrderWorkingHours.setSource(name);
            // 新增任务单id
            taskOrderWorkingHours.setTaskId(taskId);
            // 任务单号
            taskOrderWorkingHours.setTaskCode(taskCode);
            taskOrderWorkingHours.setWorkingHoursId(String.valueOf(workingHoursId));
            // 总工时 保留小位小数
            BigDecimal totalWorkingHoursZhi = BigDecimal.valueOf(Double.valueOf(taskOrderWorkingHours.getTotalWorkingHours())).setScale(2, BigDecimal.ROUND_FLOOR);
            taskOrderWorkingHours.setTotalWorkingHours(totalWorkingHoursZhi.toString());
            list.add(taskOrderWorkingHours);
            deleteUserIds.add(userId);
        }
        // 判断任务工时存在 则进行删除根据 taskId and workingHoursId and is not in （deleteUserIds）
        if (CollectionUtil.isNotEmpty(workingHoursCount)) {
            LambdaQueryWrapper<TestTaskOrderWorkingHours> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.notIn(TestTaskOrderWorkingHours::getUserId, deleteUserIds);
            deleteWrapper.eq(TestTaskOrderWorkingHours::getTaskId, taskId);
            deleteWrapper.eq(TestTaskOrderWorkingHours::getWorkingHoursId, workingHoursId);
            testTaskOrderWorkingHoursMapper.delete(deleteWrapper);
        }
        // list排序 进行保留小数点后两位
        for (int i = 0; i < list.size(); i++) {
            TestTaskOrderWorkingHours workingHoursData = list.get(i);
            // 工时保留小位小数。
            BigDecimal workHours = BigDecimal.valueOf(Double.valueOf(workingHoursData.getWorkingHours())).setScale(2, RoundingMode.FLOOR);
            workingHoursData.setWorkingHours(workHours.toString());
            // 百分比 = 所占工时 / 总工时 * 100 保留两位小数
            BigDecimal proportion = workHours.divide(new BigDecimal(Double.valueOf(workingHoursData.getTotalWorkingHours())), 4, BigDecimal.ROUND_FLOOR);
            BigDecimal percentum = proportion.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_FLOOR);
            workingHoursData.setProportion(percentum.toString());
            // 通过taskId 判断 工时是否存在 存在则更新操作、不存在则新增。
            if (CollectionUtil.isNotEmpty(workingHoursCount)) {
                workingHoursData.setUpdateTime(new Date());
                LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(TestTaskOrderWorkingHours::getTaskId, taskId);
                queryWrapper1.eq(TestTaskOrderWorkingHours::getUserId, workingHoursData.getUserId());
                queryWrapper1.eq(TestTaskOrderWorkingHours::getWorkingHoursId, workingHoursId);
                workingHoursData.setCreateTime(null);
                workingHoursData.setDiscount(discount);
                testTaskOrderWorkingHoursMapper.update(workingHoursData, queryWrapper1);
            } else {
                testTaskOrderWorkingHoursMapper.insert(workingHoursData);
            }
        }
        // 统计数据 是否被整除
        String count = testTaskOrderWorkingHoursMapper.selectTaskOrderWorkingCount(taskId);
        if (StringUtils.isNotEmpty(count)) {
            // 计算剩余工时信息、及百分比
            surplusWorkingHours(taskId);
        }
    }

    /**
     * 计算剩余工时信息、及百分比
     *
     * @param taskId
     */
    public void surplusWorkingHours(Long taskId) {
        // 获取比例信息 进行更新
        LambdaQueryWrapper<TestTaskOrderWorkingHours> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, taskId);
        lambdaQueryWrapper.orderByAsc(TestTaskOrderWorkingHours::getWorkingHours);
        List<TestTaskOrderWorkingHours> testTaskOrderWorkingHoursList = testTaskOrderWorkingHoursMapper.selectList(lambdaQueryWrapper);
        TestTaskOrderWorkingHours data = testTaskOrderWorkingHoursList.get(0);
        // 获取任务单对应人员工时
        BigDecimal sqlTotalWorking = new BigDecimal("0");
        // 结余比例信息
        BigDecimal surplusProportion = new BigDecimal("100");
        for (TestTaskOrderWorkingHours taskOrderWorkingHours : testTaskOrderWorkingHoursList) {
            // 任务单下 人员工时累计相加
            BigDecimal workHours = sqlTotalWorking.add(new BigDecimal(taskOrderWorkingHours.getWorkingHours()));
            sqlTotalWorking = workHours;
            // 任务单下比例信息 剩余比例 = 100 - 人员对应比例
            BigDecimal proportion = surplusProportion.subtract(new BigDecimal(taskOrderWorkingHours.getProportion()));
            surplusProportion = proportion;
        }
        // 剩余工时 = 总工时 - 实际工时 保留小位点两位
        BigDecimal zhi01 = BigDecimal.valueOf(Double.valueOf(data.getTotalWorkingHours())).setScale(4, BigDecimal.ROUND_FLOOR);
        BigDecimal zhi02 = sqlTotalWorking.setScale(4, BigDecimal.ROUND_FLOOR);
        BigDecimal zhi03 = zhi01.subtract(zhi02).setScale(4, BigDecimal.ROUND_FLOOR);
        // 结果保留小位小数 = 剩余工时 + 最小工时相加
        BigDecimal workHours = zhi03.add(new BigDecimal(data.getWorkingHours()));
        data.setWorkingHours(workHours.toString());
        // 当前人比例信息 = 剩余百分比 + 当前比例信息
        BigDecimal percentum = surplusProportion.add(new BigDecimal(data.getProportion())).setScale(2, BigDecimal.ROUND_FLOOR);
        data.setProportion(percentum.toString());
        testTaskOrderWorkingHoursMapper.updateByPrimaryKeySelective(data);
    }

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public void updateTaskSimplexHourRatio() {

        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        // 获取任务单工时信息：
        List<TestTaskOrderWorkingHours> latestWorkingHoursList = testTaskOrderWorkingHoursMapper.selectList(queryWrapper);
        // 有序 map
        Map<Long, List<TestTaskOrderWorkingHours>> taskWorkingHoursMap = new TreeMap<>();

        for (TestTaskOrderWorkingHours taskOrderWorkingHours : latestWorkingHoursList) {
            if (taskWorkingHoursMap.get(taskOrderWorkingHours.getTaskId()) == null) {
                List<TestTaskOrderWorkingHours> addList = new ArrayList<>();
                addList.add(taskOrderWorkingHours);
                taskWorkingHoursMap.put(taskOrderWorkingHours.getTaskId(), addList);
            } else {
                List<TestTaskOrderWorkingHours> addDataList = taskWorkingHoursMap.get(taskOrderWorkingHours.getTaskId());
                addDataList.add(taskOrderWorkingHours);
                taskWorkingHoursMap.put(taskOrderWorkingHours.getTaskId(), addDataList);
            }
        }

        LambdaQueryWrapper<TestItemOrderWorkingHours> itemQueryWrapper = new LambdaQueryWrapper<>();
        // 获取检测项下 对下 工时
        List<TestItemOrderWorkingHours> itemWorkingHoursList = testItemOrderWorkingHoursMapper.selectList(itemQueryWrapper);
        Map<Long, List<TestItemOrderWorkingHours>> itemWorkingHoursMap = new HashMap<>();

        for (TestItemOrderWorkingHours taskOrderWorkingHours : itemWorkingHoursList) {
            if (itemWorkingHoursMap.get(taskOrderWorkingHours.getTaskId()) == null) {
                List<TestItemOrderWorkingHours> addList = new ArrayList<>();
                addList.add(taskOrderWorkingHours);
                itemWorkingHoursMap.put(taskOrderWorkingHours.getTaskId(), addList);
            } else {
                List<TestItemOrderWorkingHours> addDataList = itemWorkingHoursMap.get(taskOrderWorkingHours.getTaskId());
                addDataList.add(taskOrderWorkingHours);
                itemWorkingHoursMap.put(taskOrderWorkingHours.getTaskId(), addDataList);
            }
        }

        //  获取全部 阶梯检测项工时信息
        List<TestItemWorkHourLadderVo> itemWorkHourLadderVos = testItemOrderWorkingHoursMapper.selectAllTestItemWorkHourLadderVos();

        // 检测项id 对应工时阶梯集合
        Map<Integer, List<TestItemWorkHourLadderVo>> checlItemIdWorkHourMap = new HashMap<>();
        for (TestItemWorkHourLadderVo testItemWorkHourLadderVo : itemWorkHourLadderVos) {

            if (checlItemIdWorkHourMap.get(testItemWorkHourLadderVo.getCheckItemId()) == null) {

                // 待计算 check_item_id 工时信息集合
                List<TestItemWorkHourLadderVo> testItemWorkHourLadderVos = new ArrayList<>();
                testItemWorkHourLadderVos.add(testItemWorkHourLadderVo);
                checlItemIdWorkHourMap.put(testItemWorkHourLadderVo.getCheckItemId(), testItemWorkHourLadderVos);
            } else {
                List<TestItemWorkHourLadderVo> testItemWorkHourLadderVos = checlItemIdWorkHourMap.get(testItemWorkHourLadderVo.getCheckItemId());
                testItemWorkHourLadderVos.add(testItemWorkHourLadderVo);
                checlItemIdWorkHourMap.put(testItemWorkHourLadderVo.getCheckItemId(), testItemWorkHourLadderVos);
            }
        }


        int i = 0;
        // 比较任务单工时 与 现有检测项 工时 是否一致：
        for (Long key : taskWorkingHoursMap.keySet()) {
            StringBuffer stringBuffer = new StringBuffer();
            i = i + 1;
            stringBuffer.append("\"序号\" + (i += 1) == " + i + "+ \"key \" + key" + key);
            System.out.println(stringBuffer.toString());
            // 任务单工时集合
            List<TestTaskOrderWorkingHours> taskDataWorkingHoursList = taskWorkingHoursMap.get(key);

            // 比较当前任务单下检测项工时信息
            List<TestItemOrderWorkingHours> itemDataWorkingHoursList = itemWorkingHoursMap.get(key);
            try {
                comintWorks(taskDataWorkingHoursList, itemDataWorkingHoursList, checlItemIdWorkHourMap);
            } catch (Exception e) {
                e.printStackTrace();
                stringBuffer.append("任务单key 抛出异常 key =" + key);
                System.out.println("任务单key 抛出异常 key =" + key);
            }

            System.out.println(stringBuffer.toString());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrAddTaskRelMap02(Long taskId, List<TestTaskOrderWorkingHours> list) {


        // list排序 进行保留小数点后两位
        for (int i = 0; i < list.size(); i++) {
            TestTaskOrderWorkingHours workingHoursData = list.get(i);
            BigDecimal proportion = BigDecimal.valueOf(Double.valueOf(workingHoursData.getProportion())).setScale(2, RoundingMode.FLOOR);
            workingHoursData.setProportion(proportion.toString());
            BigDecimal workHours = proportion.multiply(new BigDecimal(Double.valueOf(workingHoursData.getTotalWorkingHours())).setScale(4, BigDecimal.ROUND_FLOOR));
            BigDecimal workHours1 = workHours.divide(new BigDecimal(100), 4, BigDecimal.ROUND_FLOOR);
            workingHoursData.setWorkingHours(workHours1.toString());
            // 通过taskId 判断 工时是否存在 存在则更新操作、不存在则新增。
            workingHoursData.setUpdateTime(new Date());
            LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(TestTaskOrderWorkingHours::getTaskId, taskId);
            queryWrapper1.eq(TestTaskOrderWorkingHours::getUserId, workingHoursData.getUserId());

            workingHoursData.setCreateTime(null);

            testTaskOrderWorkingHoursMapper.update(workingHoursData, queryWrapper1);
        }
        // 统计数据 是否被整除
        String count = testTaskOrderWorkingHoursMapper.selectTaskOrderWorkingCount(taskId);
        if (StringUtils.isNotEmpty(count)) {
            // 计算剩余工时信息、及百分比
            surplusWorkingHours(taskId);
        }
    }

    @Override
    public void adjustTheTimeAllocationRatio(String taskCode) {

        // 获取 任务单信息：
        TaskVo taskDetailInfo = taskMapper.selectTaskOneDetails(taskCode);
        if (taskDetailInfo == null) {
            System.out.println("查询失败 " + " 不存在 " + taskCode);
            return;
        }

        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        // 获取任务单工时信息：
        queryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, taskDetailInfo.getId());
        List<TestTaskOrderWorkingHours> latestWorkingHoursList = testTaskOrderWorkingHoursMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(latestWorkingHoursList)) {
            return;
        }

        BigDecimal bigDecimal = new BigDecimal("0");
        for (TestTaskOrderWorkingHours data : latestWorkingHoursList) {
            BigDecimal sum = bigDecimal.add(new BigDecimal(data.getProportion())).setScale(2, RoundingMode.FLOOR);
            bigDecimal = sum;
        }

        if (bigDecimal.compareTo(new BigDecimal("100")) == 0) {
            updateOrAddTaskRelMap02(taskDetailInfo.getId(), latestWorkingHoursList);
        }
    }

    /**
     * 检测项工时 与 次数 处理阶梯管理
     *
     * @param workingHours
     * @param times
     * @param testItemWorkHourLadderVos
     */
    String returnTimeLadderWorkingHours(String workingHours, Integer times, List<TestItemWorkHourLadderVo> testItemWorkHourLadderVos) {
        // 找出最匹配区间数值。
        for (TestItemWorkHourLadderVo itemWorkHourLadderVo : testItemWorkHourLadderVos) {
            // 当前检测项次数 >= 最小值 && 当前检测项次数 <= 最小值
            if (times >= itemWorkHourLadderVo.getStartTimes() && times <= itemWorkHourLadderVo.getEndTimes()) {
                // 工时信息 填报: 检测项工时 = 工时 * 阶梯工时比例 保留小数点后四位。
                // 工时
                BigDecimal zhi1 = BigDecimal.valueOf(Double.parseDouble(workingHours)).setScale(4, BigDecimal.ROUND_HALF_UP);
                // 阶梯工时比例
                BigDecimal zhi2 = BigDecimal.valueOf(Double.parseDouble(itemWorkHourLadderVo.getDecimalPoint())).setScale(4, BigDecimal.ROUND_HALF_UP);
                // 检测项工时 = 工时 * 阶梯工时比例 保留小数点后四位。
                BigDecimal itemWorkingHours = zhi1.multiply(zhi2).setScale(4, BigDecimal.ROUND_HALF_UP);
                return String.valueOf(itemWorkingHours);
            }
        }
        return workingHours;
    }

    @Transactional(rollbackFor = Exception.class)
    public void comintWorks(List<TestTaskOrderWorkingHours> taskDataWorkingHoursList, List<TestItemOrderWorkingHours> itemDataWorkingHoursList,
                            Map<Integer, List<TestItemWorkHourLadderVo>> checlItemIdWorkHourMap) {

        // 获取check_item_id
        List<Integer> checkItemIds = new ArrayList<>();
        for (TestItemOrderWorkingHours testItemOrderWorkingHours : itemDataWorkingHoursList) {
            checkItemIds.add(testItemOrderWorkingHours.getCheckItemId());
        }

        // 获取产品检测项 工时 根据检测项 Ids
        List<CheckItemInfoVo> itemVoWorkingHoursList = productItemEntityMapper.selectItemVoWorkingHoursList(checkItemIds);

        // 总工时
        BigDecimal totalWorkingHours = new BigDecimal("0");
        // 计算工时
        for (TestItemOrderWorkingHours testItemOrderWorkingHours : itemDataWorkingHoursList) {
            for (CheckItemInfoVo checkItemInfoVo : itemVoWorkingHoursList) {
                if (testItemOrderWorkingHours.getCheckItemId().equals(checkItemInfoVo.getCheckItemId())) {
                    // 总工时 =  检测项工时 * 次数 (保留两位小数)
                    String workHours = "0";
                    if (StringUtils.isNotEmpty(checkItemInfoVo.getWorkingHours())) {
                        // 包含阶梯次数
                        if (CollectionUtil.isNotEmpty(checlItemIdWorkHourMap.get(testItemOrderWorkingHours.getCheckItemId()))) {
                            List<TestItemWorkHourLadderVo> testItemWorkHourLadderVos = checlItemIdWorkHourMap.get(testItemOrderWorkingHours.getCheckItemId());
                            // 调用方法 返回阶梯业务数据
                            String returnWorkingHours = returnTimeLadderWorkingHours(checkItemInfoVo.getWorkingHours(), testItemOrderWorkingHours.getTimes(), testItemWorkHourLadderVos);
                            workHours = returnWorkingHours;
                        } else {
                            workHours = checkItemInfoVo.getWorkingHours();
                        }
                    }
                    totalWorkingHours = totalWorkingHours.add(new BigDecimal(workHours).multiply(new BigDecimal(testItemOrderWorkingHours.getTimes()))).setScale(2, BigDecimal.ROUND_FLOOR);
                    // 工时不一致 取检测工时即可
                    if (!checkItemInfoVo.getWorkingHours().equals(testItemOrderWorkingHours.getWorkingHours())) {
                        testItemOrderWorkingHours.setWorkingHours(new BigDecimal(workHours).setScale(4, BigDecimal.ROUND_FLOOR).toString());
                    }
                }
            }
        }
        // 比较检测项工时 与 任务单提供工时不一致
        if (!totalWorkingHours.equals(taskDataWorkingHoursList.get(0).getWorkingHours())) {
            // 更新任务单工时
            for (TestTaskOrderWorkingHours taskOrderWorkingHours : taskDataWorkingHoursList) {
                // 替换工时任务单中 总工时
                taskOrderWorkingHours.setTotalWorkingHours(totalWorkingHours.toString());
                // 当前工时 = 总工时 * 比例  / 100（保留小位小数）
                taskOrderWorkingHours.setWorkingHours(totalWorkingHours.multiply(new BigDecimal(taskOrderWorkingHours.getProportion())).divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_FLOOR).toString());
            }
            // 执行批量更新任务单工时
            testTaskOrderWorkingHoursService.saveOrUpdateBatch(taskDataWorkingHoursList, taskDataWorkingHoursList.size());
            // 更新检测项工时
            testItemOrderWorkingHoursService.saveOrUpdateBatch(itemDataWorkingHoursList, itemDataWorkingHoursList.size());
            // 统计数据 是否被整除
            String count = testTaskOrderWorkingHoursMapper.selectTaskOrderWorkingCount(taskDataWorkingHoursList.get(0).getTaskId());
            if (StringUtils.isNotEmpty(count)) {
                // 计算剩余工时信息、及百分比
                surplusWorkingHours(taskDataWorkingHoursList.get(0).getTaskId());
            }
        }

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updatedCheckItemParameters(String taskCode) {
        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskOrderWorkingHours::getTaskCode, taskCode);
        List<TestTaskOrderWorkingHours> testTaskOrderWorkingHoursList = testTaskOrderWorkingHoursMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(testTaskOrderWorkingHoursList)) {
            return null;
        }
        TestTaskOrderWorkingHours data = testTaskOrderWorkingHoursList.get(0);


        // 通过任务单id 获取任务单下对应的检测项总工时
        String totalWorkingHours = renturnWorkingHours(data.getTaskId(), Long.parseLong(data.getWorkingHoursId()), data.getSource());
//        BigDecimal he = new BigDecimal(totalWorkingHours).multiply(new BigDecimal("0.85")).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal he = new BigDecimal(totalWorkingHours).setScale(4, BigDecimal.ROUND_HALF_UP);

        // 更新工时
        for (TestTaskOrderWorkingHours testTaskOrderWorkingHours : testTaskOrderWorkingHoursList) {
            testTaskOrderWorkingHours.setTotalWorkingHours(String.valueOf(he));
            testTaskOrderWorkingHoursMapper.updateById(testTaskOrderWorkingHours);
        }
        // 更新工时比例
        adjustTheTimeAllocationRatio(taskCode);
        return null;
    }

    @Override
    public InputStream getPersonnelStatisticsDetailsExport(TaskStatisticsVo taskStatisticsVo) throws IOException {
        taskStatisticsVo.setPageNum(1);
        taskStatisticsVo.setPageSize(100000);
        Result result = getPersonnelStatisticsDetails(taskStatisticsVo);
        PageInfo<TestTaskOrderWorkingHours> pageInfo = (PageInfo<TestTaskOrderWorkingHours>) result.getData();
        List<TestTaskOrderWorkingHours> list = pageInfo.getList();
// 我的工时统计-导出
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet = wb.createSheet("sheet0");
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1 = sheet.createRow(0);
        //创建单元格并设置单元格内容
        row1.createCell(0).setCellValue("任务单编号");
        row1.createCell(1).setCellValue("样品名称");
        row1.createCell(2).setCellValue("工时");
        row1.createCell(3).setCellValue("获得时间");
        row1.createCell(4).setCellValue("来源");
        if (CollectionUtil.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                TestTaskOrderWorkingHours statisticsVo = list.get(i);
                //在sheet里创建第二行
                HSSFRow row3 = sheet.createRow(i + 1);
                // 任务单编号
                row3.createCell(0).setCellValue(statisticsVo.getTaskCode());
                // 样品名称
                row3.createCell(1).setCellValue(statisticsVo.getSampleName() != null ? statisticsVo.getSampleName().toString() : "-");
                // 工时
                row3.createCell(2).setCellValue(statisticsVo.getWorkingHours() != null ? statisticsVo.getWorkingHours().toString() : "-");
                // 获得时间
                row3.createCell(3).setCellValue(statisticsVo.getCreateTime() != null ? DateUtil.formatDate(statisticsVo.getCreateTime()) : "-");
                // 来源
                row3.createCell(4).setCellValue(statisticsVo.getSource() != null ? statisticsVo.getSource() : "-");
            }
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }
}

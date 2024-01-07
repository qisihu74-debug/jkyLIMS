package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.lims.manage.erp.service.TestCheckItemsTaskRelService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TaskStatisticsVo;
import com.lims.manage.erp.vo.WorkHourStatisticVo;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

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
            // 通过任务单id 获取任务单下对应的检测项总工时
            String workingHours = null;
            if (workingHoursId != null) {
                workingHours = testItemOrderWorkingHoursMapper.getTotalWorkingHours(taskId, Long.valueOf(workingHoursId));
            } else {
                workingHours = baseMapper.getWorkingHours(taskId);
            }
            map.put("sumCount", workingHours);
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
    public Result postAdjustingQuotas(List<TestTaskOrderWorkingHours> list, String workingHoursId) {
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
        TestTaskOrderWorkingHours taskOrderWokingHoursData = testTaskOrderWorkingHoursMapper.selectOne(lambdaQueryWrapper);
        if (taskOrderWokingHoursData == null) {
            return ResultUtil.error("分配失败，不是领单人");
        }
        // 允许调整一次。
        TaskTestEntity taskDetails = taskMapper.selectTaskEntity(list.get(0).getTaskId());
        if (taskDetails == null) {
            return ResultUtil.error("分配失败，任务单不存在");
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
            int proportion = Integer.parseInt(taskOrderWorkingHours.getProportion());
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
            if (workingHoursId != null) {
                taskOrderWorkingHours.setWorkingHoursId(workingHoursId);
            }
            taskOrderWorkingHours.setUpdateTime(new Date());
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
                    // 已接任务单量
                    data.setReceivedTaskVolume(String.valueOf(1));
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
                    // 已接任务单量 = 任务单量 + 1
                    data.setReceivedTaskVolume(String.valueOf(Integer.parseInt(data.getReceivedTaskVolume()) + 1));
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
                    // 已接任务单量
                    statisticsVo.setReceivedTaskVolume(data.getReceivedTaskVolume());
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
            List<TestTaskOrderWorkingHours> totalWorkforAllTasks = testTaskOrderWorkingHoursMapper.selectTaskOrderWorkingHours(taskStatisticsVo);
            // 调用方法：全部任务单 工时统计
            methodWorkingHours(totalWorkforAllTasks, list);
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
        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskOrderWorkingHours::getUserId, taskStatisticsVo.getReceiverUserId());
        List<TestTaskOrderWorkingHours> list = testTaskOrderWorkingHoursMapper.selectList(queryWrapper);
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
    // TODO：2023年12月18日 任务单完成后 分配工时 废弃
    /*@Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean endTaskAllottedTime(Long taskId) {
        // 根据任务单 查询任务下对应操作信息
        TaskTestEntity taskDetails = taskMapper.selectTaskEntity(taskId);
        if (taskDetails != null) {
            if (taskDetails.getWorkingHoursId() != null) {
                return true;
            }
        }
        String workingHoursId = baseMapper.getTaskIdWorkingHours(taskId);
        if (workingHoursId != null) {
            return true;
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
            TestInitDataEntity auxiliaryPersonnel = basisList.get(5);
            int zhi2 = Integer.parseInt(auxiliaryPersonnel.getRemark()) / 2;
            auxiliaryPersonnel.setRemark(String.valueOf(zhi2));
            basisList.set(5, auxiliaryPersonnel);
        } else {
            // 辅助人员 工时 = 0
            TestInitDataEntity auxiliaryPersonnel = basisList.get(5);
            auxiliaryPersonnel.setRemark("0");
            basisList.set(5, auxiliaryPersonnel);
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
            methodSubstr(4, name + "&" + taskDetails.getReceiver(), mapData, taskId, basisList);
        }
        // 辅助人员
        if (StringUtils.isNotEmpty(taskDetails.getAuxiliaryPersonnel())) {
            //  调用方法 去处理 签名信息进行截取
            methodSubstr(5, taskDetails.getAuxiliaryPersonnel(), mapData, taskId, basisList);
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
        // 通过任务单id 获取任务单下对应的检测项总工时
        String workingHours = baseMapper.getWorkingHours(taskId);
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
            int proportion = Integer.parseInt(taskOrderWorkingHours.getProportion());
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
            testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        }
        return true;
    }*/

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
     * 根据taskId 进行获取检测项 存储工时对应检测项信息 返回总工时。
     *
     * @param taskId
     * @param workingHoursId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String renturnWorkingHours(Long taskId, Long workingHoursId, String source) {
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
        }
        List<Long> taskIds = testTaskOrderWorkingHoursMapper.getTaskList(entrustId);
        if (CollectionUtil.isEmpty(taskIds)) {
            return false;
        }
        // 通过任务单id 获取工时列表
        List<TestTaskOrderWorkingHours> testTaskOrderWorkingHoursList = testTaskOrderWorkingHoursMapper.getTestTaskOrderWorkingHoursList(taskIds);
        if (state == 0) {
            // 报告发起签发：
            if (CollectionUtil.isEmpty(testTaskOrderWorkingHoursList)) {
                // 任务id集合 不包含 工时信息
                for (Long taskId : taskIds) {
                    // 根据taskId 循环新增工时数据
                    reportIssuanceAllottedTime(taskId);
                }
            } else {
                // 任务id集合获取 得到 工时信息:
                //                      判断任务单工时生成规则（为试验操作时的则删除，为报告签发的则正常新增即可）
                for (TestTaskOrderWorkingHours workingHoursData : testTaskOrderWorkingHoursList) {
                    if (StringUtils.isEmpty(workingHoursData.getWorkingHoursId())) {
                        LambdaQueryWrapper<TestTaskOrderWorkingHours> deleteQueryWrapper = new LambdaQueryWrapper<>();
                        deleteQueryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, workingHoursData.getTaskId());
                        deleteQueryWrapper.isNull(TestTaskOrderWorkingHours::getWorkingHoursId);
                        testTaskOrderWorkingHoursMapper.delete(deleteQueryWrapper);
                    }
                }
                for (Long taskId : taskIds) {
                    // 根据taskId 循环新增工时数据
                    reportIssuanceAllottedTime(taskId);
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
            endReportAllottedTime(taskId);
//            reportIssuanceAllottedTime(taskId);
        }
        return true;
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
        if (taskDetails.getTaskListStatus() == null) {
            return false;
        }

        // 通过任务单id 查询所有的 任务大厅 分配的检测项列表
        List<TestCheckItemsTaskRel> bitValueList = testCheckItemsTaskRelMapper.selectAllDataBitValue(taskId);
        // 通过任务单获取检测项工时信息
        List<TestItemOrderWorkingHours> itemOrderWorkingHoursList = testItemOrderWorkingHoursMapper.selectTaskList(taskId);
        // 分组设置任务大厅 工时信息
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
        // 获取每组的工时信息
        for (Integer key : bitValueMap.keySet()) {
            // 分组中比例信息
            //TODO:1月5日  查询基础表信息 - 检测类型包含工时
            List<TestInitDataEntity> sqlBasisList = taskMapper.selectEntrustBasis(30);
            List<TestCheckItemsTaskRel> itemList = bitValueMap.get(key);
            // 检测项去重
            Set<Integer> itemSet = new HashSet<>();
            for (TestCheckItemsTaskRel taskRel : itemList) {
                itemSet.add(taskRel.getItemId());
            }
            // 根据检测项信息 生成 对应的工时、获取实际委托中组数。
            List<TestItemOrderWorkingHours> itemWorkingHoursList = new ArrayList<>();
            for (Integer itemId : itemSet) {
                for (TestItemOrderWorkingHours itemOrderWorkingHours : itemOrderWorkingHoursList) {
                    if (itemOrderWorkingHours.getItemId().equals(itemId)) {
                        itemWorkingHoursList.add(itemOrderWorkingHours);
                    }
                }
            }
            // 把检测项与 表 test_item_work_hour_ladder 比对后 求乘积。
            System.out.println("输出信息");
            // 工时总信息
            String sum = "";
            for (TestItemOrderWorkingHours itemOrderWorkingHours : itemWorkingHoursList) {
                Integer times = itemOrderWorkingHours.getTimes();
                Double workingHours = 0.0;
                if (StringUtils.isNotEmpty(itemOrderWorkingHours.getWorkingHours())) {
                    workingHours = Double.valueOf(itemOrderWorkingHours.getWorkingHours());
                }
                BigDecimal he = BigDecimal.valueOf(times * workingHours).setScale(2, BigDecimal.ROUND_HALF_UP);
                if (StringUtils.isEmpty(sum)) {
                    sum = String.valueOf(he);
                } else {
                    // sum 不为空
                    BigDecimal sumbig = new BigDecimal(sum);
                    sum = String.valueOf(sumbig.add(he).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            }
            // 每个人的所属比例及工时信息。
            System.out.println("每个人的所属比例及工时信息");
            List<TestTaskOrderWorkingHours> list = workHourRatioMethod(itemList, sqlBasisList, sum);
            if (CollectionUtil.isNotEmpty(list)) {
                bitTaskRelList.put(key, list);
            }
        }
        // 获取分组检测项信息 进行合并 存储。
        System.out.println("输出");
        addTestTaskOrderWorkingHoursMapper(taskId, bitTaskRelList);
        return true;
    }

    /**
     * 返回 求每个人的所占比例及获取工时信息
     *
     * @param itemList  任务大厅 每组 检测项分配人员信息。
     * @param basisList 人员比例
     * @param sum       分组工时比例。
     */
    public List<TestTaskOrderWorkingHours> workHourRatioMethod(List<TestCheckItemsTaskRel> itemList, List<TestInitDataEntity> basisList, String sum) {
        // key = userId 、 value = 比例信息。
        // 使用 map 进行 数据统计 key = userId , value = 参数
        Map<Long, TestTaskOrderWorkingHours> mapData = new HashMap<>();
        /**
         * 调整比例 ： 辅助人包括报告制作和辅助人员 : 两者存在 比例/2
         */
        methodAdjustingThePproportion(itemList, basisList);

        /**
         * 获取分组下人员占比信息
         */
        methodObtainProportionInformation(mapData, itemList, basisList);
        System.out.println("获取分组下人员占比信息");
        List<TestTaskOrderWorkingHours> list = new ArrayList<>();
        // 返回 工时信息及比例
        if (CollectionUtil.isNotEmpty(mapData.keySet())) {
            for (Long userId : mapData.keySet()) {
                TestTaskOrderWorkingHours data = mapData.get(userId);
                // 比例 保留四位小数。
                BigDecimal proportion = BigDecimal.valueOf(Double.valueOf(data.getProportion()) / 100).setScale(4, BigDecimal.ROUND_HALF_UP);
                // 分组工时
                data.setTotalWorkingHours(sum);
                BigDecimal zhi1 = BigDecimal.valueOf(Double.valueOf(data.getTotalWorkingHours())).setScale(2, BigDecimal.ROUND_HALF_UP);
                //当前人的工时 = 比例 * zhi 保留四位小数。
                BigDecimal he = proportion.multiply(zhi1).setScale(4, BigDecimal.ROUND_HALF_UP);
                data.setWorkingHours(String.valueOf(he));
                mapData.put(userId, data);
                list.add(data);
            }
            return list;
        }
        return list;
    }

    /**
     * 私有方法：
     * 调整比例 ： 辅助人包括报告制作和辅助人员 : 两者存在 比例/2
     *
     * @param itemList
     * @param basisList
     */
    private void methodAdjustingThePproportion(List<TestCheckItemsTaskRel> itemList, List<TestInitDataEntity> basisList) {
        // 报告制作人员
        String reportProducer = null;
        // 辅助人员
        String auxiliaryPersonnelperson = null;
        for (TestCheckItemsTaskRel taskRel : itemList) {
            if (taskRel.getUserType() == 3) {
                reportProducer = "报告制作人存在";
            }
            if (taskRel.getUserType() == 4) {
                auxiliaryPersonnelperson = "辅助人员存在";
            }
        }

        // 辅助人包括报告制作和辅助人员 : 两者存在 比例/2
        if (StringUtils.isNotEmpty(reportProducer) && StringUtils.isNotEmpty(auxiliaryPersonnelperson)) {
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
            TestInitDataEntity auxiliaryPersonnel = basisList.get(5);
            auxiliaryPersonnel.setRemark("0");
            basisList.set(4, auxiliaryPersonnel);
        }
    }

    /**
     * 获取分组下人员占比信息
     *
     * @param mapData
     * @param itemList
     * @param basisList
     */
    private void methodObtainProportionInformation(Map<Long, TestTaskOrderWorkingHours> mapData, List<TestCheckItemsTaskRel> itemList, List<TestInitDataEntity> basisList) {
        // itemList 中包含  0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生
        // basisList 中包含：0 检测人员	25 。1、记录人员	25。2 复核人	20。3 报告制作人	10。 4 辅助人员	10 。 5 签发人	20。
        for (int i = 0; i < basisList.size() - 1; i++) {
            // 截取list中数据
            String inspector = methodOrganizeData(i, itemList);
            if (StringUtils.isNotEmpty(inspector)) {
                methodSubstr(i, inspector, mapData, null, basisList);
            }
        }
    }

    /**
     * 整理检测类型数据
     *
     * @param type
     * @param itemList
     * @return
     */
    private String methodOrganizeData(Integer type, List<TestCheckItemsTaskRel> itemList) {
        // 检测人员
        StringBuffer inspector = new StringBuffer();
        // 获取 type类型去重后数据
        HashMap<Long, TestCheckItemsTaskRel> map = new HashMap<>();
        for (TestCheckItemsTaskRel taskRel : itemList) {
            if (taskRel.getUserType() == type) {
                map.put(Long.valueOf(taskRel.getUserId()), taskRel);
            }
        }
        if (CollectionUtil.isNotEmpty(map.keySet())) {
            for (Long userId : map.keySet()) {
                TestCheckItemsTaskRel taskRel = map.get(userId);
                inspector.append(taskRel.getUserName() + "&" + taskRel.getUserId());
                inspector.append(",");
            }
            return inspector.deleteCharAt(inspector.length() - 1).toString();
        }
        return null;
    }

    /**
     * 分配工时信息
     *
     * @param taskId
     * @param bitTaskRelList 分组设置任务大厅 获取工时信息
     */
    private void addTestTaskOrderWorkingHoursMapper(Long taskId, HashMap<Integer, List<TestTaskOrderWorkingHours>> bitTaskRelList) {
        //  人员对应的检测工时及比例信息。
        // 使用map 进行用户数据合并
        Map<Long, TestTaskOrderWorkingHours> taskRelMap = new HashMap<>();
        // 总工时
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
                    // 下一组工时
//                    taskRel
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
                    System.out.println("value1 " + value1.toString() + " value2 =" + value2.toString());
                    BigDecimal he = value1.add(value2).setScale(4, BigDecimal.ROUND_HALF_UP);
                    System.out.println("he " + he.toString());
                    data.setWorkingHours(String.valueOf(he));
                    taskRelMap.put(data.getUserId(), data);
                } else {
                    // 如果两组 都无 则新增即可。
                    taskRelMap.put(Long.valueOf(taskRel.getUserId()), taskRel);
                }
            }
        }
        // 展示数据
        TaskTestEntity taskDetails = taskMapper.selectTaskEntity(taskId);
        //TODO:1月5日  查询基础表信息 - 检测类型包含工时
        List<TestInitDataEntity> sqlBasisList = taskMapper.selectEntrustBasis(30);
        // 总工时
        System.out.println("总工时 == " + totalWorkingHours);

        // 签发人： 获取
        Boolean issuerInformation = false;
        for (Long userId : taskRelMap.keySet()) {
            // 签收人为
            if (Long.valueOf(taskDetails.getReceiver()).equals(userId)) {
                issuerInformation = true;
            }
        }
        // 每组人员拥有的检测项信息。
        for (Long userId : taskRelMap.keySet()) {
            TestTaskOrderWorkingHours data = taskRelMap.get(userId);
            // 进行 计算比例。
            // 签收人为
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
            BigDecimal totalWorkingHoursBig = BigDecimal.valueOf(Double.valueOf(totalWorkingHours)).setScale(2, BigDecimal.ROUND_HALF_UP);
            //所占工时 保留四位小数。
            BigDecimal workingHours = BigDecimal.valueOf(Double.valueOf(data.getWorkingHours())).setScale(4, BigDecimal.ROUND_HALF_UP);
            // 百分比 = 所占工时 / 总工时 * 100 保留两位小数
            //当前人的工时 = 比例 * zhi 保留四位小数。
            BigDecimal he = workingHours.divide(totalWorkingHoursBig).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            // 所占工时
            data.setProportion(String.valueOf(he));
            System.out.println("每组人员拥有的检测项信息 == " + data);
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
        // 把数据 新增进去。
        addtaskRelMap(taskId, taskRelMap, taskDetails);
    }

    /**
     * 增加工时信息。增加item详情工时数据。
     *
     * @param taskId
     * @param taskRelMap
     * @param taskDetails
     */
    @Transactional(rollbackFor = Exception.class)
    private void addtaskRelMap(Long taskId, Map<Long, TestTaskOrderWorkingHours> taskRelMap, TaskTestEntity taskDetails) {
        // 工时id
        Long workingHoursId = GenID.getID();

        // 任务单号
        String taskCode = taskDetails.getTaskCode();

        // 接单人 =  来源
        String name = "";
        if (StringUtils.isEmpty(name)) {
            name = sysUserDao.getSysUserName(Long.valueOf(taskDetails.getReceiver()));
        }
        // 通过任务单id 获取任务单下对应的检测项总工时
        renturnWorkingHours(taskId, workingHoursId, name);

        // 通过任务单 获取样品信息
        List<String> sampleNames = taskMapper.getTaskSamples(taskDetails.getId());
        StringBuffer nameBuffer = new StringBuffer();
        if (CollectionUtil.isNotEmpty(sampleNames)) {
            for (String sampleName : sampleNames) {
                nameBuffer.append(sampleName + " ");
            }
        }
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
            testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        }
    }
}

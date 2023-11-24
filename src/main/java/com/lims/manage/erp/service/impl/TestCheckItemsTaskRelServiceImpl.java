package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
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
import com.lims.manage.erp.service.TestCheckItemsTaskRelService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.PersonalStatsVo;
import com.lims.manage.erp.vo.TaskStatisticsVo;
import com.lims.manage.erp.vo.WorkHourStatisticVo;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.xlsx4j.sml.Col;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
            System.out.println("来源信息 == 暂定为空" + taskStatisticsVo.getSource());
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
                if (taskStatisticsVo1.getState() == null) {
                    taskStatisticsVo1.setStatus(true);
                    taskStatisticsVo1.setState(null);
                } else {
                    taskStatisticsVo1.setStatus(false);
                    taskStatisticsVo1.setState(null);
                }
                // 通过任务单id 获取任务单下对应的检测项总工时
                String workingHours = baseMapper.getWorkingHours(taskStatisticsVo1.getTaskId());
                // 补充工时
                taskStatisticsVo1.setWorkingHours(workingHours);
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
    public Result getMyHoursStatisticsDetails(Long taskId) {
        // 查询任务单下 人员比例信息
        LambdaQueryWrapper<TestTaskOrderWorkingHours> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, taskId);
        List<TestTaskOrderWorkingHours> list = testTaskOrderWorkingHoursMapper.selectList(lambdaQueryWrapper);
        // 返回数据
        Map<String, Object> map = new HashMap<>();
        if (CollectionUtils.isEmpty(list)) {
            // 根据任务单 查询任务下对应操作信息
            TaskTestEntity taskDetails = taskMapper.selectTaskEntity(taskId);
            //TODO:11月10 查询基础表信息 - 检测类型包含工时
            List<TestInitDataEntity> basisList = taskMapper.selectEntrustBasis(30);
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
            map.put("list", countList);
            // 通过任务单id 获取任务单下对应的检测项总工时
            String workingHours = baseMapper.getWorkingHours(taskId);
            // 任务单 工时
            map.put("sumCount", workingHours);
        } else {
            map.put("list", list);
            // 通过任务单id 获取任务单下对应的检测项总工时
            String workingHours = baseMapper.getWorkingHours(taskId);
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
        for (int i = 0; i < inspectorarrays.length; i++) {
//                "王亚玲&1652365523132106,马瑞玲&1652404746060112"
            String[] inspectorStr = inspectorarrays[i].split("&");
            if (mapData.get(Long.parseLong(inspectorStr[1])) == null) {
                TestTaskOrderWorkingHours data = new TestTaskOrderWorkingHours();
                data.setUserName(inspectorStr[0]);
                data.setUserId(Long.parseLong(inspectorStr[1]));
                // 设置比例
                if (map.get(typeStr) != null) {
                    Integer value = map.get(typeStr);
                    data.setProportion(value.toString());
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
                    Integer value1 = map.get(typeStr);
                    Integer value2 = Integer.parseInt(data.getProportion());
                    data.setProportion(String.valueOf(value1 + value2));
                } else {
                    Integer value1 = Integer.parseInt(data.getProportion());
                    Integer value2 = 0;
                    data.setProportion(String.valueOf(value1 + value2));
                }
                data.setDetectionType(data.getDetectionType() + "、" + typeStr);
                data.setTaskId(taskId);
                mapData.put(data.getUserId(), data);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result postAdjustingQuotas(List<TestTaskOrderWorkingHours> list) {
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
        // 允许调整一次。
        TaskTestEntity taskDetails = taskMapper.selectTaskEntity(list.get(0).getTaskId());
        if (taskDetails == null) {
            return ResultUtil.error("分配失败，任务单不存在");
        }
        if (taskDetails.getWorkingHoursId() != null) {
            return ResultUtil.error("当前任务单号 调整分配（仅可调整一次）");
        }
        // 删除旧数据
        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, list.get(0).getTaskId());
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
            testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        }
        // 数据新增后： 更新任务单数据 workingHoursId = 1
        taskMapper.updateTaskWorkingHoursId(taskDetails.getId());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean endTaskAllottedTime(Long taskId) {
        // 根据任务单 查询任务下对应操作信息
        TaskTestEntity taskDetails = taskMapper.selectTaskEntity(taskId);
        if (taskDetails != null) {
            if (taskDetails.getWorkingHoursId() != null) {
                return true;
            }
        }
        //TODO:11月10 查询基础表信息 - 检测类型包含工时
        List<TestInitDataEntity> basisList = taskMapper.selectEntrustBasis(30);
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
    }
}

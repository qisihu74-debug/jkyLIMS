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
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestCheckItemsTaskRelMapper;
import com.lims.manage.erp.mapper.TestTaskOrderWorkingHoursMapper;
import com.lims.manage.erp.mapper.TestTeamDao;
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
                // 通过任务单id 获取任务单下对应的检测项总工时
                Integer workingHours = baseMapper.getWorkingHours(taskStatisticsVo1.getTaskId());
                // 补充工时
                taskStatisticsVo1.setWorkingHours(workingHours);
            }
        }
        PageInfo<TaskStatisticsVo> result = new PageInfo<>(list);
        return ResultUtil.success(result);
    }

    @Override
    public Result getMyHoursStatisticsSum() {
        TaskStatisticsVo taskStatisticsVo = new TaskStatisticsVo();
        // 获取当前用户登录的信息
        SysUserEntity user = ShiroUtils.getUserInfo();
        // 当前登录人 = 授权签字人
        taskStatisticsVo.setReceiverUserId(user.getUserId());
        // 进行工时 总计
        PageHelper.clearPage();
        Integer countSum = baseMapper.getMyHoursStatisticsSum(taskStatisticsVo);
        return ResultUtil.success(countSum);
    }

    /**
     * 查询我的工时-统计-导出
     *
     * @param taskStatisticsVo
     * @return
     */
    @Override
    public InputStream getMyHoursStatisticsExport(TaskStatisticsVo taskStatisticsVo) throws IOException {
        // 获取当前用户登录的信息
        SysUserEntity user = ShiroUtils.getUserInfo();
        // 当前登录人 = 授权签字人
        taskStatisticsVo.setReceiverUserId(user.getUserId());
        // 进行 查询分页。
        PageHelper.clearPage();
        List<TaskStatisticsVo> list = baseMapper.getMyHoursStatistics(taskStatisticsVo);
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
        // 根据任务单 查询任务下对应操作信息
        TaskTestEntity taskDetails = taskMapper.selectTaskEntity(taskId);
        // 查询任务单下 人员比例信息
        LambdaQueryWrapper<TestTaskOrderWorkingHours> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, taskId);
        List<TestTaskOrderWorkingHours> list = testTaskOrderWorkingHoursMapper.selectList(lambdaQueryWrapper);
        // 返回数据
        Map<String, Object> map = new HashMap<>();
        if (CollectionUtils.isEmpty(list)) {
            List<TestTaskOrderWorkingHours> countList = new ArrayList<>();
            // 直接返回任务单数据即可。
            // 检测人员
            String[] inspectorarrays = taskDetails.getInspector().split(",");
            for (int i = 0; i < inspectorarrays.length; i++) {
//                "王亚玲&1652365523132106,马瑞玲&1652404746060112"
                TestTaskOrderWorkingHours data = new TestTaskOrderWorkingHours();
                String[] inspectorStr = inspectorarrays[i].split("&");
                data.setUserName(inspectorStr[0]);
                data.setUserId(Long.parseLong(inspectorStr[1]));
                data.setDetectionType("检测人员：");
                countList.add(data);
            }
            // 记录人员
            String[] recorderarrays = taskDetails.getRecorder().split(",");
            for (int i = 0; i < recorderarrays.length; i++) {
//                "王亚玲&1652365523132106,马瑞玲&1652404746060112"
                TestTaskOrderWorkingHours data = new TestTaskOrderWorkingHours();
                String[] inspectorStr = recorderarrays[i].split("&");
                data.setUserName(inspectorStr[0]);
                data.setUserId(Long.parseLong(inspectorStr[1]));
                data.setDetectionType("记录人员：");
                countList.add(data);
            }
            // 复核人
            String[] reviewerarrays = taskDetails.getReviewer().split(",");
            for (int i = 0; i < reviewerarrays.length; i++) {
//                "王亚玲&1652365523132106,马瑞玲&1652404746060112"
                TestTaskOrderWorkingHours data = new TestTaskOrderWorkingHours();
                String[] inspectorStr = reviewerarrays[i].split("&");
                data.setUserName(inspectorStr[0]);
                data.setUserId(Long.parseLong(inspectorStr[1]));
                data.setDetectionType("复核人：");
                countList.add(data);
            }
            // 报告制作人
            String[] reportProducerarrays = taskDetails.getReportProducer().split(",");
            for (int i = 0; i < reportProducerarrays.length; i++) {
//                "王亚玲&1652365523132106,马瑞玲&1652404746060112"
                TestTaskOrderWorkingHours data = new TestTaskOrderWorkingHours();
                String[] inspectorStr = reportProducerarrays[i].split("&");
                data.setUserName(inspectorStr[0]);
                data.setUserId(Long.parseLong(inspectorStr[1]));
                data.setDetectionType("报告制作人：");
                countList.add(data);
            }
            map.put("list", countList);
            // 通过任务单id 获取任务单下对应的检测项总工时
            Integer workingHours = baseMapper.getWorkingHours(taskId);
            // 任务单 工时
            map.put("sumCount", workingHours);
        } else {
            map.put("list", list);
            // 通过任务单id 获取任务单下对应的检测项总工时
            Integer workingHours = baseMapper.getWorkingHours(taskId);
            map.put("sumCount", workingHours);
        }
        return ResultUtil.success(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result postAdjustingQuotas(List<TestTaskOrderWorkingHours> list) {
        // 允许调整一次。
        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, list.get(0).getTaskId());
        List<TestTaskOrderWorkingHours> dataList = testTaskOrderWorkingHoursMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(dataList)) {
            return ResultUtil.error("当前任务单号 调整分配（仅可调整一次）");
        }
        // 获取当前用户登录的信息
        SysUserEntity user = ShiroUtils.getUserInfo();
        for (TestTaskOrderWorkingHours taskOrderWorkingHours : list) {
            taskOrderWorkingHours.setAddOperator(user.getName() + "&" + user.getUserId());
            taskOrderWorkingHours.setCreateTime(new Date());
            testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        }
        return ResultUtil.success("数据新增成功");
    }
}

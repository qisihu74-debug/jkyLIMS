package com.lims.manage.erp.interfaces;

import com.alibaba.fastjson.JSON;
import com.aspose.cells.Workbook;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.HourCount;
import com.lims.manage.erp.entity.TestTaskOrderWorkingHours;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestCheckItemsTaskRelService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.TaskStatisticsVo;
import com.lims.manage.erp.vo.TestTaskOrderWorkingHoursVo;
import com.lims.manage.erp.vo.WorkHourStatisticVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计分析接口列表
 * @author: zhq
 * @date: 2023-02-01
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/statisticAnalysis")
public class StatisticAnalysisInterface {

    @Resource
    private TestCheckItemsTaskRelService testCheckItemsTaskRelService;
    @Resource
    private TaskMapper taskMapper;

    /**
     * 统计分析-根据条件获取工时统计信息
     *
     * @param paramMap 查询条件
     * @param pageNo   页码
     * @param pageSize 页数
     * @return Result<?>
     */
    @GetMapping(value = "getWorkHoursList")
    public Result<?> getWorkHoursList(@RequestParam Map<String, Object> paramMap,
                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<WorkHourStatisticVo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(testCheckItemsTaskRelService.getWorkHoursList(page, paramMap));
    }

    /**
     * 工时统计-我的工时
     *
     * @param taskStatisticsVo
     * @return
     */
    @GetMapping(value = "getMyHoursStatistics")
    public Result<?> getMyHoursStatistics(TaskStatisticsVo taskStatisticsVo) {

        return testCheckItemsTaskRelService.getMyHoursStatistics(taskStatisticsVo);
    }

    /**
     * 工时统计-我的工时-我的总工时
     *
     * @return
     */
    @GetMapping(value = "getMyHoursStatisticsSum")
    public Result<?> getMyHoursStatisticsSum(TaskStatisticsVo taskStatisticsVo) {

        return testCheckItemsTaskRelService.getMyHoursStatisticsSum(taskStatisticsVo);
    }

    /**
     * 工时统计-我的工时-导出
     *
     * @param taskStatisticsVo
     * @return
     */
    @GetMapping(value = "getMyHoursStatisticsExport")
    public void getMyHoursStatisticsExport(TaskStatisticsVo taskStatisticsVo, HttpServletResponse response) throws IOException {
        BufferedOutputStream bos = null;
        String fileName = "工时统计-我的工时-导出" + DateUtil.formatDate(new Date());
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(fileName + ".xlsx", "UTF-8"));
        InputStream inputStream = testCheckItemsTaskRelService.getMyHoursStatisticsExport(taskStatisticsVo);
        ServletOutputStream outputStream = response.getOutputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        bos = new BufferedOutputStream(outputStream);
        byte[] buff = new byte[2048];
        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
            bos.flush();
        }
        bos.close();
    }


    /**
     * 工时统计-我的工时-调整分配-返回数据
     *
     * @param taskId
     * @return
     */
    @GetMapping(value = "getMyHoursStatisticsDetails")
    public Result<?> getMyHoursStatisticsDetails(Long taskId, String workingHoursId) {
        System.out.println("workingHoursId == " + workingHoursId);
        if (StringUtils.isEmpty(workingHoursId)) {
            workingHoursId = null;
        }
        return testCheckItemsTaskRelService.getMyHoursStatisticsDetails(taskId, workingHoursId);
    }

    /**
     * 工时统计-我的工时-调整分配
     *
     * @param taskOrderWorkingHoursVo
     * @return
     */
    @PostMapping(value = "postAdjustingQuotas")
    public Result<?> postAdjustingQuotas(@RequestBody TestTaskOrderWorkingHoursVo taskOrderWorkingHoursVo) {
        if (taskOrderWorkingHoursVo == null) {
            return ResultUtil.error("缺少必填参数");
        }
        if (CollectionUtils.isEmpty(taskOrderWorkingHoursVo.getList())) {
            return ResultUtil.error("数据不能为空");
        }
        System.out.println("workingHoursId == " + taskOrderWorkingHoursVo.getWorkingHoursId());

        return testCheckItemsTaskRelService.postAdjustingQuotas(taskOrderWorkingHoursVo.getList(), taskOrderWorkingHoursVo.getWorkingHoursId());
    }

    /**
     * 工时统计-按照人员统计
     *
     * @return
     */
    @GetMapping(value = "getPersonnelStatistics")
    public Result<?> getPersonnelStatistics(TaskStatisticsVo taskStatisticsVo) {

        return testCheckItemsTaskRelService.getPersonnelStatistics(taskStatisticsVo);
    }

    /**
     * 工时统计-按照人员统计_导出
     *
     * @return
     */
    @GetMapping(value = "getPersonnelStatisticsExport")
    public void getPersonnelStatisticsExport(TaskStatisticsVo taskStatisticsVo, HttpServletResponse response) throws IOException {
        BufferedOutputStream bos = null;
        String fileName = "工时统计-按照人员统计_导出" + DateUtil.formatDate(new Date());
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(fileName + ".xlsx", "UTF-8"));
        InputStream inputStream = testCheckItemsTaskRelService.getPersonnelStatisticsExport(taskStatisticsVo);
        ServletOutputStream outputStream = response.getOutputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        bos = new BufferedOutputStream(outputStream);
        byte[] buff = new byte[2048];
        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
            bos.flush();
        }
        bos.close();
    }

    /**
     * 工时统计-按照人员统计_人员总工时
     *
     * @return
     */
    @GetMapping(value = "getTotalPersonnelHours")
    public Result<?> getTotalPersonnelHours(TaskStatisticsVo taskStatisticsVo) {

        return testCheckItemsTaskRelService.getTotalPersonnelHours(taskStatisticsVo);
    }

    /**
     * 工时统计-按照人员统计-详情
     *
     * @return
     */
    @GetMapping(value = "getPersonnelStatisticsDetails")
    public Result<?> getPersonnelStatisticsDetails(TaskStatisticsVo taskStatisticsVo) {

        return testCheckItemsTaskRelService.getPersonnelStatisticsDetails(taskStatisticsVo);
    }


    /**
     * 工时统计-按照授权签字人获取
     *
     * @return
     */
    @GetMapping(value = "getAuthorizedSignatureList")
    public Result<?> getAuthorizedSignatureList(TaskStatisticsVo taskStatisticsVo) {

        return testCheckItemsTaskRelService.getAuthorizedSignatureList(taskStatisticsVo);
    }

    /**
     * 工时统计-按照授权签字人获取-导出
     *
     * @return
     */
    @GetMapping(value = "getAuthorizedSignatureListExport")
    public void getAuthorizedSignatureListExport(TaskStatisticsVo taskStatisticsVo, HttpServletResponse response) throws IOException {

        BufferedOutputStream bos = null;
        String fileName = "工时统计-按照授权签字人获取-导出" + DateUtil.formatDate(new Date());
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(fileName + ".xlsx", "UTF-8"));
        InputStream inputStream = testCheckItemsTaskRelService.getAuthorizedSignatureListExport(taskStatisticsVo);
        ServletOutputStream outputStream = response.getOutputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        bos = new BufferedOutputStream(outputStream);
        byte[] buff = new byte[2048];
        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
            bos.flush();
        }
        bos.close();
    }


    /**
     * 工时统计-按照授权签字人获取_详情
     *
     * @return
     */
    @GetMapping(value = "getAuthorizedSignatureListDetails")
    public Result<?> getAuthorizedSignatureListDetails(TaskStatisticsVo taskStatisticsVo) {

        return testCheckItemsTaskRelService.getAuthorizedSignatureListDetails(taskStatisticsVo);
    }

    /**
     * 工时统计-按照授权签字-统计总工时
     *
     * @return
     */
    @GetMapping(value = "getAuthorizedSignatureHours")
    public Result<?> getAuthorizedSignatureHours(TaskStatisticsVo taskStatisticsVo) {

        return testCheckItemsTaskRelService.getAuthorizedSignatureHours(taskStatisticsVo);
    }

//    @RequestMapping("batchAllottedTime")
//    public void BatchAllottedTime() {
//        List<Long> taskIds = taskMapper.selectTaskIds();
//        System.out.println("taskIds + size" + taskIds.size());
//        for (Long taskId : taskIds) {
//            testCheckItemsTaskRelService.endTaskAllottedTime(taskId);
//            System.out.println("taskId = = " + taskId);
//
//        }
//    }

    /**
     * 导出积分统计表
     * @param bean
     */
    @PostMapping("exportHours")
    public void exportHours(@RequestBody TaskStatisticsVo bean,HttpServletResponse response){
        if (bean.getStartDate() == null || bean.getStopDate() == null){
            return ;
        }
        List<HourCount> list = testCheckItemsTaskRelService.exportHours(bean);
        List<HourCount> sortList = list.stream()
                .sorted(Comparator.comparing(HourCount::getPid))
                .collect(Collectors.toList());
        InputStream fileStream = MinIoUtil.getFileStream(BucketsConst.controlled_documents, "jifen.xlsx");
        try {
            Workbook workbook = new Workbook(fileStream);
            testCheckItemsTaskRelService.handExcelData(sortList,workbook,bean,response);
        } catch (Exception e) {
            log.error("导出积分统计表失败:{}",e);
        }
    }

}

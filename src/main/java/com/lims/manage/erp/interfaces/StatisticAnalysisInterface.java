package com.lims.manage.erp.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestTaskOrderWorkingHours;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestCheckItemsTaskRelService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.vo.TaskStatisticsVo;
import com.lims.manage.erp.vo.TestTaskOrderWorkingHoursVo;
import com.lims.manage.erp.vo.WorkHourStatisticVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public Result<?> getMyHoursStatisticsSum() {

        return testCheckItemsTaskRelService.getMyHoursStatisticsSum();
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
    public Result<?> getMyHoursStatisticsDetails(Long taskId) {

        return testCheckItemsTaskRelService.getMyHoursStatisticsDetails(taskId);
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
        return testCheckItemsTaskRelService.postAdjustingQuotas(taskOrderWorkingHoursVo.getList());
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

}

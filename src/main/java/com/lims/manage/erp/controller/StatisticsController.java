package com.lims.manage.erp.controller;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.StatisticsService;
import com.lims.manage.erp.vo.AreaStatisticsResultVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.StatisticsParamVo;
import com.lims.manage.erp.vo.TaskStatsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/statistics/")
public class StatisticsController {
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private StatisticsService statisticsService;

    /**
     * 任务查询
     * @param
     * @return
     */
    @RequestMapping("/taskQuery")
    public Result taskQuery(@RequestBody TaskStatsVo taskStatsVo) {
        if (taskStatsVo.getDeptId() == null || taskStatsVo.getPageNum() == null || taskStatsVo.getPageSize() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        return ResultUtil.success("查询任务统计！", statisticsService.taskQuery(taskStatsVo));
    }

    /**
     * 任务查询详情。
     * @param
     * @return
     */
    @RequestMapping("/taskDetails")
    public Result TaskDetails(@Param("taskId")Long taskId) {
        if (taskId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        return ResultUtil.success("查询任务统计！", statisticsService.TaskDetails(taskId));
    }

    /**
     * 任务查询_（Excel表导出）
     * @param taskStatsVo
     * @return
     */
    @RequestMapping("/taskQuery_export")
    public void taskQuery_export(@RequestBody TaskStatsVo taskStatsVo, HttpServletResponse response) throws IOException {
        BufferedOutputStream bos = null;
        if (taskStatsVo.getDeptId() == null || taskStatsVo.getPageNum() == null || taskStatsVo.getPageSize() == null) {
            log.info("任务查询_（Excel表导出）\t"+"缺少必填参数");
        }
        PageInfo list = statisticsService.taskQuery(taskStatsVo);
        String fileName = "任务统计结果";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename="
                + new String(fileName.getBytes("gbk"), "iso_8859_1") + ".xls");
        InputStream inputStream = statisticsService.exportPersonDetails(list);
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
     * 区域产值统计
     * @param paramVo
     * @return
     */
    @RequestMapping("/areaStatistics")
    public Result areaStatistics(@RequestBody StatisticsParamVo paramVo) {
        if (paramVo == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        if(paramVo.getPageNum() == null || paramVo.getPageSize() == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), "缺少分页参数！");
        }
        return ResultUtil.success("区域产值统计查询成功！", statisticsService.areaStatistics(paramVo));
    }

    @RequestMapping("/areaStatisticsExport")
    public void areaStatisticsExport(@RequestBody StatisticsParamVo paramVo, HttpServletResponse response) throws IOException {
        BufferedOutputStream bos = null;
        List<AreaStatisticsResultVo> list = statisticsService.areaStatisticsExport(paramVo);
        StringBuilder fileName = new StringBuilder("区域产值统计");
        if(paramVo.getBeginDate() != null){
            fileName.append(paramVo.getBeginDate());
        }
        if(paramVo.getEndDate() != null){
            fileName.append("-");
            fileName.append(paramVo.getEndDate());
        }
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename="
                + new String(fileName.toString().getBytes("gbk"), "iso_8859_1") + ".xls");
        InputStream inputStream = statisticsService.areaStatisticsExportFunction(list);
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
}

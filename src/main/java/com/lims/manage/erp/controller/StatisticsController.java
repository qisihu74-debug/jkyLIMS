package com.lims.manage.erp.controller;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.StatisticsService;
import com.lims.manage.erp.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
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
        if (taskStatsVo.getPageNum() == null || taskStatsVo.getPageSize() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        return ResultUtil.success("查询任务统计！", statisticsService.taskQuery1111(taskStatsVo));
    }

    /**
     * 任务查询详情。
     * @param
     * @return
     */
    @RequestMapping("/taskDetails")
    public Result taskDetails(@Param("taskId")Long taskId) {
        if (taskId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        return ResultUtil.success("查询任务统计！", statisticsService.TaskDetails(taskId));
    }

    /**
     * 任务查询_（Excel表导出）
     * @return
     */
    @RequestMapping("/taskQuery_export")
    public void taskQueryExport(HttpServletResponse response) throws IOException {
        BufferedOutputStream bos = null;
        TaskStatsVo taskStatsVo = new TaskStatsVo();

        if(taskStatsVo.getPageNum()==null||taskStatsVo.getPageSize()==null){
            taskStatsVo.setPageNum(0);
            taskStatsVo.setPageSize(0);
        }
        PagingToolVo list = statisticsService.taskQuery1111(taskStatsVo);
        String fileName = "任务统计结果";
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
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
     * 个人工作量统计
     * @param
     * @return
     */
    @RequestMapping("/personal_stats")
    public Result personalStats(@RequestBody PersonalStatsVo personalStatsVo) {
        if (personalStatsVo.getPageNum() == null || personalStatsVo.getPageSize() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        return ResultUtil.success("个人工作量统计！",statisticsService.personalStats(personalStatsVo));
    }

    /**
     * 返回团队信息
     */
    @RequestMapping("/get_all_team")
    public Result getAllAllTeam(){
        return ResultUtil.success(statisticsService.selectAllTeamVo());
    }

    /**
     * 个人工作量统计
     * @param
     * @return
     */
    @RequestMapping("/personal_stats_export")
    public void personalStatsExport(HttpServletResponse response) throws IOException {
        BufferedOutputStream bos = null;
        PersonalStatsVo personalStatsVo = new PersonalStatsVo();
        personalStatsVo.setPageNum(null);
        personalStatsVo.setPageSize(null);
        PageInfo list = statisticsService.personalStats(personalStatsVo);
        String fileName = "个人工作量统计";
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename="
                + new String(fileName.getBytes("gbk"), "iso_8859_1") + ".xls");
        InputStream inputStream = statisticsService.personalStatsExport(list);
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

    /**
     * 区域产值统计--excel导出
     * @param taskSource
     * @param beginDate
     * @param endDate
     * @param response
     * @throws IOException
     */
    @GetMapping("/areaStatisticsExport")
    public void areaStatisticsExport(String taskSource,String beginDate,String endDate, HttpServletResponse response) throws IOException {
        StatisticsParamVo paramVo = new StatisticsParamVo();
        if(!"null".equals(taskSource) && !"undefined".equals(taskSource)){
            paramVo.setTaskSource(taskSource);
        }
        if(!"null".equals(beginDate)){
            paramVo.setBeginDate(beginDate);
        }
        if(!"null".equals(endDate)){
            paramVo.setEndDate(endDate);
        }
        BufferedOutputStream bos = null;
        List<AreaStatisticsResultVo> list = statisticsService.areaStatisticsExport(paramVo);
        StringBuilder fileName = new StringBuilder("区域产值统计");
        if (paramVo.getTaskSource()!=null) {
            fileName.append("-");
            fileName.append(paramVo.getTaskSource());
        }
        if(paramVo.getBeginDate() != null){
            fileName.append("-");
            fileName.append(paramVo.getBeginDate());
        }
        if(paramVo.getEndDate() != null){
            fileName.append("-");
            fileName.append(paramVo.getEndDate());
        }
//        String encode = URLEncoder.encode(fileName.toString(), "UTF-8");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename="+java.net.URLEncoder.encode(fileName.toString(), "UTF-8")+".xls");
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

    /**
     * 部门产值统计
     * @param paramVo
     * @return
     */
    @RequestMapping("/teamStatistics")
    public Result teamStatistics(@RequestBody StatisticsParamVo paramVo) {
        if (paramVo == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        if(paramVo.getPageNum() == null || paramVo.getPageSize() == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), "缺少分页参数！");
        }
        return ResultUtil.success("部门产值统计查询成功！", statisticsService.teamStatistics(paramVo));
    }

    @GetMapping("/teamStatisticsExport")
    public void teamStatisticsExport(String teamName,String beginDate,String endDate, HttpServletResponse response) throws IOException {
        StatisticsParamVo paramVo = new StatisticsParamVo();
        if(!"null".equals(teamName) && !"undefined".equals(teamName)){
            paramVo.setTaskSource(teamName);
        }
        if(!"null".equals(beginDate)){
            paramVo.setBeginDate(beginDate);
        }
        if(!"null".equals(endDate)){
            paramVo.setEndDate(endDate);
        }
        BufferedOutputStream bos = null;
        List<TeamOutputValueVo> teamOutputValueVos = statisticsService.teamStatisticsExport(paramVo);
        StringBuilder fileName = new StringBuilder("部门产值统计");
        if (paramVo.getTaskSource()!=null) {
            fileName.append("-");
            fileName.append(paramVo.getTaskSource());
        }
        if(paramVo.getBeginDate() != null){
            fileName.append("-");
            fileName.append(paramVo.getBeginDate());
        }
        if(paramVo.getEndDate() != null){
            fileName.append("-");
            fileName.append(paramVo.getEndDate());
        }
//        String encode = URLEncoder.encode(fileName.toString(), "UTF-8");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename="+java.net.URLEncoder.encode(fileName.toString(), "UTF-8")+".xls");
        InputStream inputStream = statisticsService.teamStatisticsExportFunction(teamOutputValueVos);
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
     * 查询区域信息
     * @return
     */
    @GetMapping("/getAreas")
    public Result getAreas() {
        return ResultUtil.success("查询区域信息成功！", statisticsService.getAreas());
    }
}

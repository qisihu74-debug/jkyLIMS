package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.vo.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface StatisticsService {


    TaskStatsVo TaskDetails(Long taskId);

    /**
     * 导出Excel 表
     */
    InputStream exportPersonDetails(PagingToolVo list) throws IOException;

    /**
     * 个人工作量统计
     * @param
     * @return
     */
    PageInfo personalStats(PersonalStatsVo personalStatsVo);

    /**
     * 返回团队信息
     */
    List<TestTeamVo> selectAllTeamVo();

    /**
     * 个人工作量导出Excel 表
     */
    InputStream personalStatsExport(PageInfo list) throws IOException;

    /**
     * 区域产值统计
     * @param paramVo
     * @return
     */
    PageInfo areaStatistics(StatisticsParamVo paramVo);

    /**
     * 区域产值统计--导出
     * @param paramVo
     * @return
     */
    List<AreaStatisticsResultVo> areaStatisticsExport(StatisticsParamVo paramVo);

    /**
     * 生成区域产值统计Excel
     * @param list
     * @return
     * @throws IOException
     */
    InputStream areaStatisticsExportFunction(List<AreaStatisticsResultVo> list) throws IOException;

    /**
     * 部门产值统计
     * @param paramVo
     * @return
     */
    PageInfo teamStatistics(StatisticsParamVo paramVo);

    /**
     * 部门产值统计--excel导出
     * @param paramVo
     * @return
     */
    List<TeamOutputValueVo> teamStatisticsExport(StatisticsParamVo paramVo);

    /**
     * 任务单统计
     * @param taskStatsVo
     * @return
     */
    PagingToolVo taskQuery1111(TaskStatsVo taskStatsVo);

    /**
     * 部门产值统计统计Excel
     * @param list
     * @return
     * @throws IOException
     */
    InputStream teamStatisticsExportFunction(List<TeamOutputValueVo> list) throws IOException;

    /**
     * 查询区域信息
     * @return
     */
    List<LabelValueVo> getAreas();
}

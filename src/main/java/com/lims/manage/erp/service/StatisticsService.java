package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.vo.PersonalStatsVo;
import com.lims.manage.erp.vo.TaskStatsVo;
import com.lims.manage.erp.vo.TestTeamVo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface StatisticsService {

    PageInfo taskQuery(TaskStatsVo taskStatsVo);

    TaskStatsVo TaskDetails(Long taskId);

    /**
     * 导出Excel 表
     */
    InputStream exportPersonDetails(PageInfo list) throws IOException;

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


}

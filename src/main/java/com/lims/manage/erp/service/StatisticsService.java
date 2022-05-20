package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.vo.TaskStatsVo;

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


}

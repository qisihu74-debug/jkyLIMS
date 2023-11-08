package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestCheckItemsTaskRel;
import com.lims.manage.erp.entity.TestTaskOrderWorkingHours;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.TaskStatisticsVo;
import com.lims.manage.erp.vo.WorkHourStatisticVo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
public interface TestCheckItemsTaskRelService extends IService<TestCheckItemsTaskRel> {

    /**
     * 根据条件获取工时统计信息
     *
     * @param paramMap 查询条件
     * @return 工时统计信息列表
     */
    IPage<WorkHourStatisticVo> getWorkHoursList(Page<WorkHourStatisticVo> page, Map<String, Object> paramMap);

    /**
     * 查询我的工时 统计
     *
     * @param taskStatisticsVo
     * @return
     */
    Result getMyHoursStatistics(TaskStatisticsVo taskStatisticsVo);

    /**
     * 工时统计-我的工时-我的总工时
     *
     * @return
     */
    Result getMyHoursStatisticsSum();

    /**
     * 查询我的工时-统计-导出
     *
     * @param taskStatisticsVo
     * @return
     */
    InputStream getMyHoursStatisticsExport(TaskStatisticsVo taskStatisticsVo) throws IOException;

    /**
     * 工时统计-我的工时-调整分配-返回数据
     *
     * @param taskId
     * @return
     */
    Result getMyHoursStatisticsDetails(Long taskId);

    /**
     * 工时统计-我的工时-调整分配
     *
     * @param list
     * @return
     */
    Result postAdjustingQuotas(List<TestTaskOrderWorkingHours> list);


}

package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.HourCount;
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
    Result getMyHoursStatisticsSum(TaskStatisticsVo taskStatisticsVo);

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
    Result getMyHoursStatisticsDetails(Long taskId, String workingHoursId);

    /**
     * 工时统计-我的工时-调整分配
     *
     * @param list
     * @return
     */
    Result postAdjustingQuotas(List<TestTaskOrderWorkingHours> list, String workingHoursId);

    /**
     * 工时统计-按照人员统计
     *
     * @param taskStatisticsVo
     * @return
     */
    Result getPersonnelStatistics(TaskStatisticsVo taskStatisticsVo);

    /**
     * 工时统计-按照人员统计-导出
     *
     * @param taskStatisticsVo
     * @return
     */
    InputStream getPersonnelStatisticsExport(TaskStatisticsVo taskStatisticsVo) throws IOException;

    /**
     * 工时统计-按照人员统计_人员总工时
     *
     * @return
     */
    Result getTotalPersonnelHours(TaskStatisticsVo taskStatisticsVo);


    /**
     * 工时统计-按照人员统计-详情
     *
     * @param taskStatisticsVo
     * @return
     */
    Result getPersonnelStatisticsDetails(TaskStatisticsVo taskStatisticsVo);


    /**
     * 工时统计-按照授权签字人获取
     *
     * @param taskStatisticsVo
     * @return
     */
    Result getAuthorizedSignatureList(TaskStatisticsVo taskStatisticsVo);

    /**
     * 工时统计-按照授权签字人获取
     *
     * @param taskStatisticsVo
     * @return
     */
    InputStream getAuthorizedSignatureListExport(TaskStatisticsVo taskStatisticsVo) throws IOException;

    /**
     * 工时统计-按照授权签字人获取_详情
     *
     * @param taskStatisticsVo
     * @return
     */
    Result getAuthorizedSignatureListDetails(TaskStatisticsVo taskStatisticsVo);

    /**
     * 工时统计-按照授权签字-统计总工时
     *
     * @return
     */
    Result getAuthorizedSignatureHours(TaskStatisticsVo taskStatisticsVo);

    // TODO：2023年12月18日 任务单完成后 分配工时 废弃
    /*    *//**
     * 任务单完成、分配工时
     * @param taskId
     * @return
     *//*
    Boolean endTaskAllottedTime(Long taskId);*/

    /**
     * 报告签发完成、分配工时
     *
     * @param taskId
     * @return
     */
    Boolean endReportAllottedTime(Long taskId);

    /**
     * 报告签发后： 存储工时信息时，处理业务信息
     *
     * @param reportId 报告id
     * @param state    state = 0 报告签发、state = 1 报告驳回
     * @return
     */
    Boolean handleWorkingHours(Long reportId, Integer state);


    Boolean testCommit(List<Long> taskIds);

    /**
     * 工时导出
     * @param bean
     */
    List<HourCount> exportHours(TaskStatisticsVo bean);
}

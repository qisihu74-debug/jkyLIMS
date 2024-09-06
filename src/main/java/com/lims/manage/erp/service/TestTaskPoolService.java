package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestCheckItemsTaskRel;
import com.lims.manage.erp.entity.TestTaskPool;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * <p>
 * 任务单 服务类
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
public interface TestTaskPoolService extends IService<TestTaskPool> {
    /**
     * 任务大厅 展示详情数据
     *
     * @param poolId
     * @return
     */
    Result taskHallDetailsDisplay(Long poolId, Long entrustId);

    /**
     * 任务大厅 - 根据登录人、返回所属团队成员的对应检测项。
     *
     * @param poolId
     * @param entrustId
     * @return
     */
    Result getTaskDetectionItemDetails(Long poolId, Long entrustId, SysUserEntity userInfo);

    /**
     * 任务大厅 领取任务单
     *
     * @param list
     * @return
     */
    Result addTaskCollection(List<SampleItemEntity> list, Long entrustId, SysUserEntity userInfo, Long teamId);

    /**
     * 我的任务 修改任务单
     *
     * @param list
     * @return
     */
    Result updateTaskCollection(List<SampleItemEntity> list, Long entrustId, SysUserEntity userInfo);

    /**
     * 试验检测：任务单判断是否为 new创建。
     * 是：则 判断当前检测项 是否能够操作。
     *
     * @param taskId
     * @param items
     * @return
     */
    Result testDetectionTasks(Long taskId, List<Integer> items, Integer type);

    /**
     * 任务大厅or修改时验证任务单真伪
     * 1、通过通过检测项主键 获取 委托单是否存在
     * 2、查询当前领单人 是否为 授权角色id = 66
     *
     * @param list   任务大厅传递数据
     * @param userId 登录人id
     * @return 抛出error 直接抛出，success 传递 entrustId
     */
    Result verifyTheTaskListStatus(List<SampleItemEntity> list, Long userId);

    /**
     * 任务单领取
     *
     * @param list
     * @param entrustId
     * @param userInfo
     * @return
     */
    Result addNewTicket(List<SampleItemEntity> list, Long entrustId, SysUserEntity userInfo, Long teamId);

    /**
     * 任务单更新
     *
     * @param list
     * @param entrustId
     * @param userInfo
     * @return
     */
    Result updateNewTicket(List<SampleItemEntity> list, Long entrustId, SysUserEntity userInfo, Long teamId);

    /**
     * 验证领取人对应科室信息
     *
     * @param userId
     * @return
     */
    Result verifyClaimBaseConditions(Long userId);

    /**
     * 替换任务单信息
     *
     * @return
     */
    Result informationSubstitution(String taskCode);

}

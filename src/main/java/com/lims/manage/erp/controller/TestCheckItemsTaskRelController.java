package com.lims.manage.erp.controller;


import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.service.TestTaskPoolService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.SampleItemJsonVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@RestController
@RequestMapping("/testCheckItemsTaskRel")
public class TestCheckItemsTaskRelController {

    @Autowired
    private TestTaskPoolService testTaskPoolService;
    @Autowired
    private TaskService taskService;

    /**
     * 任务大厅 展示详情数据
     *
     * @param poolId
     * @param entrustId
     * @return
     */
    @GetMapping("/taskHallDetailsDisplay")
    public Result taskHallDetailsDisplay(Long poolId, Long entrustId) {

        // 登录人
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        // 验证领取人对应科室信息
        Result verifyTeamCollection = testTaskPoolService.verifyClaimBaseConditions(userInfo.getUserId());
        if (verifyTeamCollection.getCode() == null) {
            return verifyTeamCollection;
        }
        List<Long> teamCollection = (List<Long>) verifyTeamCollection.getData();
        userInfo.setTechnicistId(teamCollection.get(0).intValue());
        // 比较任务单创建时间：区分团队信息是否拆分
        Result taskVerificationInformation = taskService.compareTaskListCreationInformation(entrustId, null);

        if (taskVerificationInformation.getData() == null) {
            // 任务单不存在:任务大厅 - 根据登录人、返回所属团队成员的对应检测项。
            return testTaskPoolService.getTaskDetectionItemDetails(poolId, entrustId, userInfo);
        } else {
            // 提示信息
            String promptMessage = (String) taskVerificationInformation.getData();
            if (promptMessage.equals("newTask")) {
                // 我的任务单 返回检测项信息
                return testTaskPoolService.getTaskDetectionItemDetails(poolId, entrustId, userInfo);
            }
        }
        // 检测项信息
        return testTaskPoolService.taskHallDetailsDisplay(poolId, entrustId);
    }

    /**
     * 领取任务单： 1、区分新任务单创建 1.1、新任务单修改 2、旧任务单执行旧操作
     *
     * @param sampleItemJsonVo
     * @return
     */
    @RequestMapping("/taskCollection")
    public Result taskCollection(@RequestBody SampleItemJsonVo sampleItemJsonVo) {
        PageHelper.clearPage();
        List<SampleItemEntity> list = sampleItemJsonVo.getList();
        if (CollectionUtil.isEmpty(list)) {
            return ResultUtil.error("数据不能为空");
        }

        // 登录人
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        // 任务大厅or修改时 验证信息
        Result getValidationStatus = testTaskPoolService.verifyTheTaskListStatus(list, userInfo.getUserId());
        if (getValidationStatus.getCode() == null) {
            return getValidationStatus;
        }
        Long entrustId = (Long) getValidationStatus.getData();
        // 验证领取人对应科室信息
        Result verifyTeamCollection = testTaskPoolService.verifyClaimBaseConditions(userInfo.getUserId());
        if (verifyTeamCollection.getCode() == null) {
            return verifyTeamCollection;
        }
        List<Long> teamCollection = (List<Long>) verifyTeamCollection.getData();
        Long teamId = teamCollection.get(0);

        // 比较任务单创建时间：区分团队信息是否拆分
        Result taskVerificationInformation = taskService.compareTaskListCreationInformation(entrustId, null);

        if (taskVerificationInformation.getData() == null) {
            // 任务单不存在:任务大厅 - 领取
            return testTaskPoolService.addNewTicket(list, entrustId, userInfo, teamId);
        } else {
            // 提示信息
            String promptMessage = (String) taskVerificationInformation.getData();
            if (promptMessage.equals("oldTask")) {
                // 执行 任务单旧操作
                return testTaskPoolService.updateTaskCollection(list, entrustId, userInfo);
            }
            Result NewOrUpdated = taskService.compareTaskListCreationInformation(entrustId, teamId.intValue());
            if (NewOrUpdated.getData() == null) {
                // 任务单不存在:任务大厅 - 领取
                return testTaskPoolService.addNewTicket(list, entrustId, userInfo, teamId);
            }
        }

        // 通过 当前登录人所属团队 及委托单id 查询任务单存在 则修改，否则是更新

        return testTaskPoolService.updateNewTicket(list, entrustId, userInfo, teamId);
    }

    /**
     * 替换任务单信息
     *
     * @return
     */
    @RequestMapping("/informationSubstitution")
    public Result informationSubstitution(String str) {
        String[] strings = str.split(",");
        for (int i = 0; i < strings.length; i++) {
            try {
                testTaskPoolService.informationSubstitution(strings[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}


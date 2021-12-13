package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TaskTestTeamEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.vo.TaskListParamVo;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/task/")
public class TaskController {
    @Autowired
    private TaskService taskService;

    /**
     * 查询任务详情
     *
     * @param taskId
     * @return
     */
    @RequestMapping("/getTaskDetailInfo")
    public Result getTaskDetailInfo(Long taskId) {
        if (taskId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success("查询任务详情成功！", taskService.getTaskDetailInfo(taskId));
        }
    }

    /**
     * 副团长抢单
     * @param taskTestEntity
     * @return
     */
    @PostMapping("postGrabASingle")
    public Result postGrabASingle(@RequestBody TaskTestEntity taskTestEntity)
    {
        if (ShiroUtils.getUserInfo() != null){
            // 抢单人
            Long strLong = ShiroUtils.getUserInfo().getUserId();
            String str1 = String.valueOf(strLong);
            taskTestEntity.setReceiver(str1);
        }
        Boolean flag = taskService.postGrabASingle(taskTestEntity);
        if(flag){
            return ResultUtil.success("抢单成功");
        }
        return ResultUtil.error(678,"抢单失败！");
    }

    /**
     * 返回 团队姓名
     * @return
     */
    @RequestMapping("getTeamUserName")
    public Result getTeamUserName()
    {
        if (ShiroUtils.getUserInfo()!= null){
            // 抢单人
            List<TaskTestTeamEntity> returnList = taskService.getTeamUserName(ShiroUtils.getUserInfo().getUserId());
            if(returnList.isEmpty()){
                return ResultUtil.error(204,"数据为空！");
            }
            return ResultUtil.success(returnList);
        }
        return ResultUtil.error(204,"数据为空！");
    }


    /**
     * 查询任务列表
     *
     * @param paramVo
     * @return
     */
    @RequestMapping("/getTaskList")
    public Result getTaskInfo(@RequestBody TaskListParamVo paramVo) {
        if (paramVo == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success("查询任务列表成功！", taskService.getTaskList(paramVo));
        }
    }

    @RequestMapping("/getSampleList")
    public Result getSampleList(@RequestBody TaskListParamVo paramVo) {
        if (paramVo == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success("查询任务列表成功！", taskService.getTaskList(paramVo));
        }
    }


}

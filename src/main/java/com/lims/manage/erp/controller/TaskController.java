package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.vo.TaskListParamVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

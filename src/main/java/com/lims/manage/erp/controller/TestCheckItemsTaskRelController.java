package com.lims.manage.erp.controller;


import cn.hutool.core.collection.CollectionUtil;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestTaskPoolService;
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

    /**
     * 任务大厅 展示详情数据
     *
     * @param poolId
     * @param entrustId
     * @return
     */
    @GetMapping("/taskHallDetailsDisplay")
    public Result taskHallDetailsDisplay(Long poolId, Long entrustId) {
        return testTaskPoolService.taskHallDetailsDisplay(poolId, entrustId);
    }

    /**
     * 任务大厅 领取任务单
     *
     * @param sampleItemJsonVo
     * @return
     */
    @RequestMapping("/taskCollection")
    public Result taskCollection(@RequestBody SampleItemJsonVo sampleItemJsonVo) {
//    public Result taskCollection(@RequestBody List<SampleItemEntity> list) {
//    public Result taskCollection(@RequestParam("json") String json) {
        List<SampleItemEntity> list = sampleItemJsonVo.getList();
        if (CollectionUtil.isEmpty(list)) {
            return ResultUtil.error("数据不能为空");
        }
        return testTaskPoolService.addTaskCollection(list);
    }

}


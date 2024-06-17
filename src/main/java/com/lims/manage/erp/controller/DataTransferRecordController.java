package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.extension.api.ApiController;
import com.lims.manage.erp.entity.DataTransferRecord;
import com.lims.manage.erp.entity.DataTransferRelation;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DataTransferRecordService;
import com.lims.manage.erp.vo.DataTransferRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 数据流转记录控制层
 *
 * @author: zhq
 * @date: 2024-06-12
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/dataTransfer")
public class DataTransferRecordController extends ApiController {

    @Resource
    private DataTransferRecordService dataTransferRecordService;

    /**
     * 添加流转记录
     *
     * @param dataTransferRecord 流转记录信息
     * @return 添加结果
     */
    @PostMapping(value = "addDataTransfer")
    public Result<?> addDataTransfer(@RequestBody DataTransferRecord dataTransferRecord) {
        dataTransferRecordService.addDataTransfer(dataTransferRecord);
        return ResultUtil.success("流转记录添加成功", null);
    }

    /**
     * 添加流转关系
     *
     * @param dataTransferRelation 流转关系
     * @return 添加结果
     */
    @PostMapping(value = "addTransferRelation")
    public Result<?> addTransferRelation(@RequestBody DataTransferRelation dataTransferRelation) {
        dataTransferRecordService.addTransferRelation(dataTransferRelation);
        return ResultUtil.success("流转关系添加成功", null);
    }

    /**
     * 获取流转记录列表
     *
     * @param dataId 数据id
     * @return 流转记录列表
     */
    @GetMapping(value = "getTransferRecordList")
    public Result<?> getTransferRecordList(@RequestParam("dataId") String dataId) {
        List<DataTransferRecordVo> transferRecordList = dataTransferRecordService.getTransferRecordList(dataId);
        return ResultUtil.success(transferRecordList);
    }


}

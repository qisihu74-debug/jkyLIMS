package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.lims.manage.erp.entity.LabelInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LabelInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 标签信息控制层
 * @author: zhq
 * @date: 2023-01-05
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/labelInfo")
public class LabelInfoController extends ApiController {

    @Resource
    private LabelInfoService labelInfoService;

    /**
     * 获取标签列表
     * @return Result
     */
    @GetMapping(value = "/list")
    public Result<?> list(){
        return ResultUtil.success("获取标签信息列表",labelInfoService.list(Wrappers.<LabelInfo>lambdaQuery().select(LabelInfo::getLabelId,LabelInfo::getLabelContent)));
    }
}

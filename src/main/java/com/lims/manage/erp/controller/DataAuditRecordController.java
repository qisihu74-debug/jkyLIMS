package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.DataInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DataAuditRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 知识审核控制层
 *
 * @author: zhq
 * @date: 2023-01-05
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/dataAudit")
public class DataAuditRecordController extends ApiController {

    @Resource
    DataAuditRecordService dataAuditRecordService;

    /**
     * 知识审核信息列表
     *
     * @param dataInfo 查询条件
     * @param pageNo   页码
     * @param pageSize 页数
     * @return Result<?>
     */
    @GetMapping(value = "pageList")
    public Result<?> pageList(DataInfo dataInfo,
                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<DataInfo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(dataAuditRecordService.pageList(page, dataInfo));
    }
}

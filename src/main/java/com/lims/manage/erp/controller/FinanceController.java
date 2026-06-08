package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.FinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/finance/")
public class FinanceController {

    @Resource
    private FinanceService financeService;

    @GetMapping("summary")
    public Result summary() {
        return ResultUtil.success(financeService.summary());
    }

    @GetMapping("billing/list")
    public Result billingList(Integer pageNum, Integer pageSize, String search) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success(financeService.billingList(pageNum, pageSize, search));
    }
}

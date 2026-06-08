package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.FinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

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

    @PostMapping("billing/calculate")
    public Result calculateBilling(@RequestBody Map<String, Object> payload) {
        try {
            financeService.calculateBilling(payload);
            return ResultUtil.success("操作成功");
        } catch (RuntimeException e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("remittance/add")
    public Result addRemittance(@RequestBody Map<String, Object> payload) {
        try {
            financeService.addRemittance(payload);
            return ResultUtil.success("操作成功");
        } catch (RuntimeException e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("remittance/list")
    public Result remittanceList(Long entrustId) {
        if (entrustId == null) {
            return ResultUtil.error("缺少委托单参数！");
        }
        return ResultUtil.success(financeService.remittanceList(entrustId));
    }

    @PostMapping("remittance/update")
    public Result updateRemittance(@RequestBody Map<String, Object> payload) {
        try {
            financeService.updateRemittance(payload);
            return ResultUtil.success("操作成功");
        } catch (RuntimeException e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("remittance/delete")
    public Result deleteRemittance(@RequestBody Map<String, Object> payload) {
        try {
            financeService.deleteRemittance(payload);
            return ResultUtil.success("操作成功");
        } catch (RuntimeException e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("invoice/add")
    public Result addInvoice(@RequestBody Map<String, Object> payload) {
        try {
            financeService.addInvoice(payload);
            return ResultUtil.success("操作成功");
        } catch (RuntimeException e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("invoice/update")
    public Result updateInvoice(@RequestBody Map<String, Object> payload) {
        try {
            financeService.updateInvoice(payload);
            return ResultUtil.success("操作成功");
        } catch (RuntimeException e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("invoice/ledger")
    public Result invoiceLedger(Integer pageNum, Integer pageSize, String search, String status) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success(financeService.invoiceLedger(pageNum, pageSize, search, status));
    }

    @PostMapping("invoice/status")
    public Result updateInvoiceStatus(@RequestBody Map<String, Object> payload) {
        try {
            financeService.updateInvoiceStatus(payload);
            return ResultUtil.success("操作成功");
        } catch (RuntimeException e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("profit/analysis")
    public Result profitAnalysis(Integer pageNum, Integer pageSize, String search) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success(financeService.profitAnalysis(pageNum, pageSize, search));
    }

    @GetMapping("statement")
    public Result statement(Long entrustId) {
        if (entrustId == null) {
            return ResultUtil.error("缺少委托单参数！");
        }
        return ResultUtil.success(financeService.statement(entrustId));
    }
}

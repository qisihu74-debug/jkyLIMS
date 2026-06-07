package com.lims.manage.erp.controller;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.CmaCapabilityItem;
import com.lims.manage.erp.entity.CmaSyncLog;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.CmaService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cma")
public class CmaController {

    @Resource
    private CmaService cmaService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String standardName,
            @RequestParam(required = false) String standardCode) {
        PageInfo<CmaCapabilityItem> page = cmaService.list(pageNum, pageSize, domain, standardName, standardCode);
        return ResultUtil.success(page);
    }

    @GetMapping("/domains")
    public Result<?> domains() {
        List<String> domains = cmaService.domains();
        return ResultUtil.success(domains);
    }

    @GetMapping("/syncStatus")
    public Result<?> syncStatus() {
        CmaSyncLog log = cmaService.latestSync();
        Map<String, Object> data = new HashMap<>();
        if (log != null) {
            data.put("fileName", log.getFileName());
            data.put("itemCount", log.getItemCount());
            data.put("syncTime", log.getSyncTime());
            data.put("status", log.getStatus());
        }
        return ResultUtil.success(data);
    }

    @PostMapping("/sync")
    public Result<?> sync() {
        new Thread(() -> cmaService.syncFromCma(), "cma-manual-sync").start();
        return ResultUtil.success("同步任务已启动，请稍后查看同步状态");
    }

    @PostMapping("/triggerEnrich")
    public Result<?> triggerEnrich() {
        cmaService.enrichHcnoAsync();
        return ResultUtil.success("hcno补充任务已启动");
    }
}

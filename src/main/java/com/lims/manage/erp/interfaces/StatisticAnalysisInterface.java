package com.lims.manage.erp.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestCheckItemsTaskRelService;
import com.lims.manage.erp.vo.WorkHourStatisticVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 统计分析接口列表
 * @author: zhq
 * @date: 2023-02-01
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/statisticAnalysis")
public class StatisticAnalysisInterface {

    @Resource
    private TestCheckItemsTaskRelService testCheckItemsTaskRelService;

    /**
     * 统计分析-根据条件获取工时统计信息
     *
     * @param paramMap 查询条件
     * @param pageNo   页码
     * @param pageSize 页数
     * @return Result<?>
     */
    @GetMapping(value = "getWorkHoursList")
    public Result<?> getWorkHoursList(@RequestParam Map<String,Object> paramMap,
                                     @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<WorkHourStatisticVo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(testCheckItemsTaskRelService.getWorkHoursList(page, paramMap));
    }

}

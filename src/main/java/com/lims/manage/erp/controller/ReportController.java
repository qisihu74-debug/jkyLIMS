package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.vo.ReportPreserveVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/report/")
public class ReportController {
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private ReportService reportService;

    /**
     * 查询可制作报告任务单列表
     *
     * @return
     */
    @GetMapping("/list")
    public Result getSampleList() {
        return ResultUtil.success("获取可制作报告任务单成功！", reportService.getReportList());
    }

    /**
     * 报告生成--编辑按钮
     *
     * @return
     */
    @GetMapping("/edit")
    public Result edit(Long id) {
        return ResultUtil.success("查询委托单信息成功！", reportService.getReportDetail(id));
    }

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("/preserve")
    public Result preserve(@RequestBody ReportPreserveVo vo) {
        Boolean preserve = reportService.preserve(vo);
        if (preserve) {
            return ResultUtil.success("保存成功！", preserve);
        } else {
            return ResultUtil.error(ResultEnum.PRESERVE_FAIL.getCode(), ResultEnum.PRESERVE_FAIL.getMsg());
        }
    }

    /**
     * 待盖章和历史盖章列表查询
     *
     * @param type
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("sealList")
    public Result sealList(String type, String search, Integer pageNum, Integer pageSize) {
        if (StringUtils.isEmpty(type) || pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少必要的参数！");
        }
        PageInfo pageInfo = reportService.sealList(type, search, pageNum, pageSize);
        return ResultUtil.success(pageInfo);
    }

}

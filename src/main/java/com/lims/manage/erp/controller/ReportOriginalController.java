package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.ReportOriginalEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportOriginalService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 报告，原始记录
 */
@Slf4j
@RestController
@RequestMapping("/reportOriginal/")
public class ReportOriginalController {
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private ReportOriginalService reportOriginalService;

    /**
     * 添加报告
     *
     * @param json
     * @param file
     * @return
     */
    @PostMapping("/addReportOriginal")
    public Result addReportOriginal(@RequestParam("json") String json, MultipartFile file) throws UnsupportedEncodingException {
        if (file == null) {
            return ResultUtil.error("报告模板不能为空！");
        }
        ReportOriginalEntity reportOriginalEntity = JSON.parseObject(json, ReportOriginalEntity.class);
        if (reportOriginalEntity.getCode() == null || reportOriginalEntity.getName() == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        int i = reportOriginalService.addReportOriginal(reportOriginalEntity, file);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (i > 0) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                    + "新增报告原始记录模板成功！", Const.REPORT_ORIGINAL, true);
            return ResultUtil.success("新增报告原始记录模板成功!", i);
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                + "新增报告原始记录模板失败！", Const.REPORT_ORIGINAL, false);
        return ResultUtil.error("新增报告原始记录模板失败");
    }

    /**
     * 查询报告列表
     *
     * @param param
     * @return
     */
    @PostMapping("/getReportList")
    public Result getReportList(@RequestBody ReportOriginalEntity param) {
        if (param.getPageNum() == null || param.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success("查询报告列表成功!", reportOriginalService.getReportList(param));
    }

    /**
     * 修改报告原始记录模板
     *
     * @param json
     * @param file
     * @return
     */
    @PostMapping("/updateReportOriginal")
    public Result updateReportOriginal(@RequestParam("json") String json, MultipartFile file) {
        ReportOriginalEntity reportOriginalEntity = JSON.parseObject(json, ReportOriginalEntity.class);
        if (reportOriginalEntity.getId() == null || reportOriginalEntity.getCode() == null
                || reportOriginalEntity.getName() == null || reportOriginalEntity.getUrl() == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        int i = reportOriginalService.updateReportOriginal(reportOriginalEntity, file);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (i > 0) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                    + "修改报告原始记录模板成功！", Const.REPORT_ORIGINAL, true);
            return ResultUtil.success("修改报告原始记录模板成功!", i);
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                + "修改报告原始记录模板失败！", Const.REPORT_ORIGINAL, false);
        return ResultUtil.error("修改报告原始记录模板失败");
    }

    /**
     * 删除报告模板
     *
     * @param idList
     * @return
     */
    @PostMapping("deleteReportTemplate")
    public Result deleteReportTemplate(@RequestParam("idList") List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ResultUtil.error("请选择要删除的报告模板！");
        }
        boolean save = reportOriginalService.deleteReportTemplate(idList);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (!save) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                    + "删除报告模板" + idList + "失败!", Const.REPORT_ORIGINAL, false);
            return ResultUtil.success("删除报告模板失败!");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                + "删除报告模板" + idList + "成功!", Const.REPORT_ORIGINAL, true);
        return ResultUtil.success("删除报告模板成功！", idList);
    }

    /**
     * 报告模板下拉列表
     *
     * @param param
     * @return
     */
    @GetMapping("/getReportSelectList")
    public Result getReportSelectList(String param) {
        return ResultUtil.success("查询报告下拉列表成功!", reportOriginalService.getReportSelectList(param));
    }

    /**
     * 变更报告原始记录模板
     * @param json
     * @param file
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/changeReportOriginal")
    public Result changeReportOriginal(@RequestParam("json") String json, MultipartFile file) throws UnsupportedEncodingException {
        if (file == null) {
            return ResultUtil.error("报告模板不能为空！");
        }
        ReportOriginalEntity reportOriginalEntity = JSON.parseObject(json, ReportOriginalEntity.class);
        if (reportOriginalEntity.getCode() == null || reportOriginalEntity.getName() == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        int i = reportOriginalService.changeReportOriginal(reportOriginalEntity, file);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (i > 0) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                    + "变更报告原始记录模板成功！", Const.REPORT_ORIGINAL, true);
            return ResultUtil.success("变更报告原始记录模板成功!", i);
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                + "变更报告原始记录模板失败！", Const.REPORT_ORIGINAL, false);
        return ResultUtil.error("变更报告原始记录模板失败");
    }

    /**
     * 查询变更记录列表
     * @param pid
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/getReportRecordList")
    public Result getReportRecordList(Long pid,Integer pageNum,Integer pageSize) {
        if (pid != null&&pageNum != null && pageSize!=null) {
            PageInfo reportRecordList = reportOriginalService.getReportRecordList(pid, pageNum, pageSize);
            return ResultUtil.success("查询报告变更列表成功!",reportRecordList);
        }else {
            return ResultUtil.error("参数为空");
        }
    }
}

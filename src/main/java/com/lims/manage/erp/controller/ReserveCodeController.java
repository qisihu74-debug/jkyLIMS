package com.lims.manage.erp.controller;

import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.ReserveCodeEntity;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DeptService;
import com.lims.manage.erp.service.ReserveCodeService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;

@RestController
@RequestMapping("/reserve/")
public class ReserveCodeController {
    @Autowired
    private ReserveCodeService reserveCodeService;
    @Autowired
    private DeptService deptService;


    /**
     * 新增预留编号
     *
     * @param reserveCodeEntity
     * @return
     */
    @Log(title = "新增预留编号", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result insert(@RequestBody ReserveCodeEntity reserveCodeEntity) {
        return this.reserveCodeService.addReserveCode(reserveCodeEntity);
    }

    /**
     * 批量删除编号
     * @param id
     * @return
     */
    @Log(title = "删除预留编号", businessType = BusinessType.DELETE)
    @GetMapping("/del")
    public Result delete(Long id) {
        if (id != null) {
            return this.reserveCodeService.delete(id);
        } else {
            return ResultUtil.error("请选择要删除的预留编号！");
        }
    }

    /**
     * 编辑预留编号
     *
     * @param reserveCodeEntity
     * @return
     */
    @Log(title = "修改预留编号", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result update(@RequestBody ReserveCodeEntity reserveCodeEntity) {
        return this.reserveCodeService.updateReserveCode(reserveCodeEntity);
    }

    /**
     * 查询预留编号
     *
     * @param reserveCodeEntity
     * @return
     */
    @PostMapping("/list")
    public Result list(@RequestBody ReserveCodeEntity reserveCodeEntity) {
        return this.reserveCodeService.list(reserveCodeEntity);
    }

    /**
     * 查询委托单号下拉列表
     * @param entrustmentNo
     * @return
     */
    @GetMapping("/getEntrustmentNoList")
    public Result getEntrustmentNoList(String entrustmentNo) {
        return this.reserveCodeService.getEntrustmentNoList(entrustmentNo);
    }

    /**
     * 下载模板
     * @param response
     */
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            InputStream inputStream = MinIoUtil.getFileStream("lims-template", "预留报告编号模板.xls");
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("预留报告编号模板.xls", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导入预留编号
     * @param file
     * @return
     */
    @Log(title = "导入预留编号", businessType = BusinessType.IMPORT)
    @RequestMapping(value = "/importTemplate", method = RequestMethod.POST)
    public Result importEquipments(@RequestParam(required = true,name = "file") MultipartFile file) {
        return reserveCodeService.importEquipments(file);
    }

    /**
     * 获取最大报告 编号
     *
     * @return
     */
    @GetMapping("/getMaxReportCode")
    public Result getMaxReportCode() {
        return this.reserveCodeService.getMaxReportCode();
    }

    /**
     * 查询最终报告与中间报告编号列表
     */
    @GetMapping("/alternateReportNumber")
    public Result alternateReportNumber(@RequestParam(value = "oldReportNumber") String oldReportNumber, @RequestParam(value = "newReportNumber") String newReportNumber) {
        Long userId = ShiroUtils.getUserInfo().getUserId();
        //判断人员是否为技术质量部下的人员
        Boolean exist = deptService.checkUserId();
        if (!exist) {
            return ResultUtil.error("非技术质量部成员无权限操作！");
        }
        System.out.println("信息输出 oldReportNumber " + oldReportNumber + "newReportNumber" + newReportNumber);
        return this.reserveCodeService.alternateReportNumber(oldReportNumber, newReportNumber);
    }

}

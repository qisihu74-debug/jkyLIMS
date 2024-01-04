package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.ReserveCodeEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ReserveCodeService;
import com.lims.manage.erp.util.MinIoUtil;
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

    /**
     * 新增预留编号
     *
     * @param reserveCodeEntity
     * @return
     */
    @PostMapping("/add")
    public Result insert(@RequestBody ReserveCodeEntity reserveCodeEntity) {
        return this.reserveCodeService.addReserveCode(reserveCodeEntity);
    }

    /**
     * 批量删除编号
     * @param id
     * @return
     */
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
            InputStream inputStream = MinIoUtil.getFileStream("lims-template", "预留编号模板.xlsx");
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("预留编号模板.xlsx", "UTF-8");
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
    @RequestMapping(value = "/importTemplate", method = RequestMethod.POST)
    public Result importEquipments(@RequestParam(required = true,name = "file") MultipartFile file) {
        return reserveCodeService.importEquipments(file);
    }

    /**
     * 获取最大报告 编号
     * @return
     */
    @GetMapping("/getMaxReportCode")
    public Result getMaxReportCode() {
        return this.reserveCodeService.getMaxReportCode();
    }

}

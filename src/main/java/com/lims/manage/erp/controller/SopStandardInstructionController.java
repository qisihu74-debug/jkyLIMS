package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.SopStandardInstruction;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.SopStandardInstructionService;
import com.lims.manage.erp.util.MinIoUtil;
import io.micrometer.core.instrument.util.StringUtils;
import io.minio.MinioClient;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * SOP标准作业指导书控制层
 */
@RestController
@RequestMapping("sopStandardInstruction")
public class SopStandardInstructionController extends ApiController {

    @Resource
    private SopStandardInstructionService service;


    @GetMapping("/getList")
    public Result getAll(SopStandardInstruction instruction) {
        QueryWrapper<SopStandardInstruction> queryWrapper = new QueryWrapper<>(instruction);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("del_flag", 0);
        return ResultUtil.success(this.service.list(queryWrapper));
    }


    /**
     * 分页查询所有数据
     *
     * @param standardInstruction 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(SopStandardInstruction standardInstruction,
                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<SopStandardInstruction> page = new Page<>(pageNo, pageSize);
        QueryWrapper<SopStandardInstruction> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        if (standardInstruction.getSopId() != null) {
            queryWrapper.like("sop_id", standardInstruction.getSopId());
        }
        if (standardInstruction.getSopName() != null) {
            queryWrapper.like("sop_name", standardInstruction.getSopName());
        }
        if (standardInstruction.getProductId() != null) {
            queryWrapper.like("product_id", standardInstruction.getProductId());
        }
        if (standardInstruction.getPositionId() != null) {
            queryWrapper.like("position_id", standardInstruction.getPositionId());
        }
        if (standardInstruction.getReleaseDate() != null) {
            queryWrapper.apply("datediff(release_date, " +
                    standardInstruction.getReleaseDate() + ") = 0");
        }
        if (standardInstruction.getImplementationDate() != null) {
            queryWrapper.apply("datediff(implementation_date, " +
                    standardInstruction.getImplementationDate() + ") = 0");
        }
        if (standardInstruction.getFacilityId() != null) {
            queryWrapper.like("facility_id", standardInstruction.getFacilityId());
        }
        if (standardInstruction.getRemarks() != null) {
            queryWrapper.like("remarks", standardInstruction.getRemarks());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.service.page(page, queryWrapper));
    }


    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("getById")
    public Result selectOne(String id) {
        if (id != null && id != "") {
            SopStandardInstruction instruction =
                    this.service.getOne(new QueryWrapper<SopStandardInstruction>()
                            .eq("id", id).eq("del_flag", 0));
            if (instruction.getFileUrl() != null) {
                String[] temp = instruction.getFileUrl().split("/");
                instruction.setFileName(temp[temp.length - 1]);
            }

            return ResultUtil.success(instruction);
        } else {
            return ResultUtil.error("参数为空");
        }
    }


    /**
     * 新增数据
     *
     * @param instruction 实体
     * @return 新增结果
     */
    @Log(title = "新增SOP", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result insert(@RequestParam String instruction, MultipartFile file) {
        SopStandardInstruction standardInstruction = JSON.parseObject(instruction, SopStandardInstruction.class);
        if (StrUtil.isEmptyIfStr(standardInstruction)) {
            return ResultUtil.error("数据为空");
        }
        return this.service.addStandardInstruction(standardInstruction, file);
    }


    /**
     * 修改数据
     *
     * @return 修改结果
     */
    @Log(title = "修改SOP", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public Result update(@RequestParam String instruction, MultipartFile file) {
        SopStandardInstruction standardInstruction = JSON.parseObject(instruction, SopStandardInstruction.class);
        if (StrUtil.isEmptyIfStr(instruction)) {
            return ResultUtil.error("数据为空");
        }
        return this.service.updStandardInstruction(standardInstruction, file);
    }


    /**
     * 删除数据
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @Log(title = "删除SOP", businessType = BusinessType.DELETE)
    @PostMapping("/del")
    public Result delete(String id) {
        if (StringUtils.isNotBlank(id)) {
            return this.service.delStandardInstruction(id);
        } else {
            return ResultUtil.error("数据为空");
        }
    }


    /**
     * 预览报告模板
     *
     * @param fileUrl
     * @param response
     */
    @GetMapping("preReportUrl")
    public void previewTemplate(String fileUrl, HttpServletResponse response) {
        MinioClient client = MinIoUtil.minioClient;
        //预览word转pdf
        String[] split = fileUrl.split("\\?");
        String[] strings = split[0].split("/");
        String bluckName = strings[3];
        String objectName = strings[4];
        try {
            client.statObject(bluckName, objectName);
            InputStream inputStream = client.getObject(bluckName, objectName);
            ServletOutputStream outputStream = response.getOutputStream();
            int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            logger.error("预览合并后的报告异常:{}", e);
        }

    }


}

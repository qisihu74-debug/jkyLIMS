package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestInstrumentService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.ExportParamVo;
import com.lims.manage.erp.vo.InstrumentRecordParamVo;
import com.lims.manage.erp.vo.TestInstrumentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * 仪器设备(TestInstrument)表控制层
 *
 * @author makejava
 * @since 2022-02-25 10:05:48
 */
@RestController
@RequestMapping("testInstrument")
@Api(value = "仪器设备管理", tags = {"仪器设备管理"})
public class TestInstrumentController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestInstrumentService testInstrumentService;

    @GetMapping("/getList")
    public Result getAll(TestInstrument testInstrument) {
        QueryWrapper<TestInstrument> queryWrapper = new QueryWrapper<>(testInstrument);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("del_flag", 0);
        return ResultUtil.success(this.testInstrumentService.list(queryWrapper));
    }

    /**
     * 分页查询所有数据
     *
     * @param page           分页对象
     * @param testInstrument 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @ApiOperation("分页查询仪器设备信息")
    public Result selectAll(Page<TestInstrumentVo> page, TestInstrument testInstrument) {
        QueryWrapper<TestInstrument> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("i.del_flag", 0);
        if (testInstrument.getName() != null) {
            queryWrapper.like("i.name", testInstrument.getName());
        }
        queryWrapper.orderByDesc("i.create_time");
        return ResultUtil.success(this.testInstrumentService.getPageList(page, queryWrapper));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    @ApiOperation("根据ID查询仪器设备信息")
    public Result selectOne(@PathVariable Serializable id) {
        if (id != null && id != "") {
            TestInstrument testInstrument = this.testInstrumentService.getOne(new QueryWrapper<TestInstrument>().eq("id", id).eq("del_flag", 0));
            return ResultUtil.success(testInstrument);
        } else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testInstrument 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    @ApiOperation("添加仪器设备")
    public Result insert(@RequestBody TestInstrument testInstrument) {
        if (StrUtil.isEmptyIfStr(testInstrument)) {
            return ResultUtil.error("数据为空");
        }
        return this.testInstrumentService.addInstrument(testInstrument);
    }

    /**
     * 修改数据
     *
     * @param testInstrument 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    @ApiOperation("修改仪器设备")
    public Result update(@RequestBody TestInstrument testInstrument) {
        if (StrUtil.isEmptyIfStr(testInstrument)) {
            return ResultUtil.error("数据为空");
        }
        return this.testInstrumentService.updInstrument(testInstrument);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    @ApiOperation("删除仪器设备")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size() != 0) {
            return this.testInstrumentService.delInstruments(idList);
        } else {
            return ResultUtil.error("数据为空");
        }
    }

    @PostMapping("/getInstrumentRecord")
    public Result getInstrumentRecord(@RequestBody InstrumentRecordParamVo paramVo) {
        if (paramVo == null || paramVo.getPageNum() == null || paramVo.getPageSize() == null || paramVo.getInstrumentId() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        PageInfo instrumentRecord = testInstrumentService.getInstrumentRecord(paramVo);
        return ResultUtil.success(instrumentRecord);
    }

    /**
     * 下载单个设备的使用记录
     *
     * @param paramVo
     * @param response
     */
    @PostMapping("/exportInstrumentRecord")
    public void exportInstrumentRecord(@RequestBody InstrumentRecordParamVo paramVo, HttpServletResponse response) {
        HashMap<String, Object> stringObjectHashMap = testInstrumentService.exportInstrumentRecord(paramVo);
        XLSTransformer transformer = new XLSTransformer();
        InputStream fileStream = MinIoUtil.getFileStream("device-record", "record.xls");
        Workbook workbook;
        try {
            workbook = transformer.transformXLS(fileStream, stringObjectHashMap);
            workbook.setSheetName(0, stringObjectHashMap.get("deviceInfo").toString() + "使用记录");
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode(stringObjectHashMap.get("deviceInfo") + "使用记录.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载多个设备的使用记录
     *
     * @param
     * @param response
     */
    @PostMapping("/batchExportInstrumentRecord")
    public void batchExportInstrumentRecord(@RequestBody ExportParamVo vo, HttpServletResponse response) {
        HashMap<String, Object> stringObjectHashMap = testInstrumentService.batchExportInstrumentRecord(vo.getDeviceId());
        XLSTransformer transformer = new XLSTransformer();
        InputStream fileStream = MinIoUtil.getFileStream("device-record", "records" + vo.getDeviceId().size() + ".xls");
        Workbook workbook;
        try {
            workbook = transformer.transformXLS(fileStream, stringObjectHashMap);
            for (int i = 0; i < vo.getDeviceId().size(); i++) {
                workbook.setSheetName(i, stringObjectHashMap.get("deviceInfo" + i).toString() + "使用记录");
            }
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode(stringObjectHashMap.get("deviceInfo0") + "等仪器的使用记录.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }
}


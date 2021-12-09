package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ProductService;
import com.lims.manage.erp.service.SampleService;
import com.lims.manage.erp.vo.SampleAddParamVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

@Slf4j
@RestController
@RequestMapping("/sample/")
public class SampleController {

    @Autowired
    private SampleService sampleService;
    @Autowired
    private ProductService productService;

    /**
     * 新增样品
     *
     * @param json 样品数据信息
     * @param file 样品照片
     * @return
     */
    @RequestMapping(value = "/addSample", method = RequestMethod.POST)
    public Result getAddSampleData(@RequestParam("json") String json, MultipartFile[] file) {
        SampleAddParamVo samples = JSON.parseObject(json, SampleAddParamVo.class);
        if (samples == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            sampleService.addSampleData(samples, file);
            return ResultUtil.success();
        }
    }

    /**
     * 查询样品信息列表
     *
     * @param paramVo
     * @return
     */
    @RequestMapping("/getSampleList")
    public Result getSampleList(@RequestBody SampleEntity paramVo) {
        if (paramVo == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(sampleService.getSamplePublicInfos(paramVo));
        }
    }

    /**
     * 新增委托时样品的查询列表--不带单位名称
     *
     * @param sampleEntity
     * @return
     */
    @RequestMapping("/getSampleList2")
    public Result getSampleList2(SampleEntity sampleEntity) {
        return ResultUtil.success(sampleService.getSampleDataList(sampleEntity));
    }

    /**
     * 查询样品组基本信息
     *
     * @param insertFlag
     * @return
     */
    @RequestMapping("/getSampleGroupInfo")
    public Result getSampleGroupInfo(String insertFlag) {
        return ResultUtil.success(sampleService.getSampleGroupInfo(insertFlag));
    }

    /**
     * 样品的查询列表--带单位名称
     *
     * @param paramVo
     * @return
     */
    @RequestMapping("/getSampleDetail")
    public Result getSampleDetail(@RequestBody SampleEntity paramVo) {
        return ResultUtil.success(sampleService.selectSampleList2(paramVo));
    }

    /**
     * 样品基本信息--修改
     * @param
     * @return
     */
    @RequestMapping(value="updateSample", method= RequestMethod.POST)
    public Result updateSampleData(@RequestBody SampleEntity sampleEntity) {
        if(sampleEntity == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }else{
            return ResultUtil.success(sampleService.updateSampleInfo(sampleEntity));
        }
    }

    /**
     * 下载样品标签
     *
     * @param sampleId
     * @return
     */
    @RequestMapping("/downloadSampleTag")
    public void downloadSampleTag(Integer sampleId, HttpServletResponse response) {
        String fileName = "样品标签.xlsx";
        Workbook sampleTagInfo = sampleService.getSampleTagInfo(sampleId);
        response.reset();
        response.setContentType("application/x-msdownload");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            OutputStream outputStream = response.getOutputStream();
            sampleTagInfo.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询产品所有的检测项
     *
     * @param productId
     * @return
     */
    @RequestMapping("/getAllItem")
    public Result getAllItemByProductId(Integer productId) {
        if (productId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(productService.getAllItemByProductId(productId));
        }
    }


}

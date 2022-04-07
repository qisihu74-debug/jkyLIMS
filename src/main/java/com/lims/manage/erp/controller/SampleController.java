package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ProductService;
import com.lims.manage.erp.service.SampleService;
import com.lims.manage.erp.service.TestSampleEntityService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.SampleAddParamVo;
import com.lims.manage.erp.vo.SampleDetailAddVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.SamplesAddVo;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sample/")
public class SampleController {

    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private ProductService productService;
    @Autowired
    private TestSampleEntityService testSampleEntityService;

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
        log.debug("样品新增参数:{}", json);
        if (samples == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            sampleService.addSampleData(samples, file);
            return ResultUtil.success("保存样品成功！");
        }
    }

    /**
     * 查询样品信息列表
     *
     * @param paramVo
     * @return
     */
    @RequestMapping("/getSampleList1")
    public Result getSampleList(@RequestBody SampleDetailVo paramVo) {
        if (paramVo == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(sampleService.getSamplePublicInfos(paramVo));
        }
    }

    /**
     * 查询样品信息列表--分页
     *
     * @param paramVo
     * @return
     */
    @RequestMapping("/getSampleList")
    public Result getSampleList1(@RequestBody SampleDetailVo paramVo) {
        if (paramVo.getPageNum() == null || paramVo.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        if (paramVo == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }

        return ResultUtil.success(sampleService.getSamplePublicInfos2(paramVo));
    }

    /**
     * 新增委托时样品的查询列表--不带单位名称
     *
     * @param sampleEntity
     * @return
     */
    @RequestMapping("/getSampleList2")
    public Result getSampleList2(@RequestBody SampleEntity sampleEntity) {
        if (sampleEntity.getPageNum() == null || sampleEntity.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数！");
        }

        return ResultUtil.success(sampleService.getSampleDataList2(sampleEntity));
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
     *
     * @param
     * @return
     */
    @RequestMapping(value = "updateSample", method = RequestMethod.POST)
    public Result updateSampleData(@RequestBody SampleEntity sampleEntity) {
        if (sampleEntity == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
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
        SampleDetailVo sampleTagInfo = sampleService.getSampleTagInfo(sampleId);
        HashMap<String, SampleDetailVo> result = Maps.newHashMap();
        StringBuilder fileName = new StringBuilder("");
        if (sampleTagInfo != null) {
            fileName.append(sampleTagInfo.getSampleCode());
            fileName.append("样品标签.xlsx");
            result.put("result", sampleTagInfo);
        }
        XLSTransformer transformer = new XLSTransformer();
        InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-template.xlsx");
        Workbook workbook = null;
        try {
            workbook = transformer.transformXLS(fileStream, result);
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            String fileName2 = URLEncoder.encode(fileName.toString(), "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName2);
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException | InvalidFormatException e) {
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
    //---------------------------------------2022年4月6日样品管理修改-----------------------------------------------

    /**
     * 样品管理--样品签收--新增样品
     *
     * @param samples
     * @return
     */
    @RequestMapping(value = "/addSamples", method = RequestMethod.POST)
    public Result addSamples(@RequestBody SamplesAddVo samples) {
        if (samples == null || CollectionUtils.isEmpty(samples.getSamples())) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            Integer integer = testSampleEntityService.batchInsertSample(samples.getSamples());
            if(integer>0){
                return ResultUtil.success("添加样品成功！", integer);
            }else{
                return ResultUtil.error("添加样品失败，请联系管理员！");
            }
        }
    }

    /**
     * 样品查询打印列表
     * @param sampleEntity
     * @return
     */
    @RequestMapping("/querySampleList")
    public Result querySampleList(@RequestBody TestSampleEntity sampleEntity) {
        if (sampleEntity.getPageNum() == null || sampleEntity.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success(testSampleEntityService.querySampleList(sampleEntity));
    }

    /**
     * 查询样品详情
     * @param id
     * @return
     */
    @GetMapping("/sampleDetail")
    public Result sampleDetail(Integer id) {
        if (id == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        return ResultUtil.success(testSampleEntityService.sampleDetail(id));
    }

}

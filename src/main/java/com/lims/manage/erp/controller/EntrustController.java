package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.vo.CheckItemParamVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.SampleAddParamVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/entrust/")
public class EntrustController {

    @Autowired
    private EntrustService entrustService;

    /**
     * 新增委托
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/addEntrust")
    public Result addEntrust(@RequestParam("json") String json, MultipartFile file){
        EntrustAddVo entrust = JSON.parseObject(json,EntrustAddVo.class);
        Boolean isSuccess = entrustService.addEntrust(entrust,file);
        if(isSuccess){
            return ResultUtil.success();
        }else{
            return ResultUtil.error(678,"新增委托失败！");
        }
    }

    /**
     *
     * @return
     */
    @GetMapping("get_Basics")
    public Result ReturnBasicsData() {
        return ResultUtil.success(entrustService.returnEntrustData());
    }

    /**
     *
     * @param companyId
     * @return
     */
    @GetMapping("get_entrusted_unit")
    public Result methodDispay(Integer companyId) {
        List<TestCustomerJsonEntity> collectList = entrustService.returnTestCustomerEntityList(companyId);
        if(collectList.isEmpty()){
            return ResultUtil.error(201,"用户信息不存在");
        }
        return ResultUtil.success(collectList);
    }

    /**
     * 新增客户
     * @param testCompanyEntity
     * @return
     */
    @PostMapping("add_new_company")
    public Result methodPost(@RequestBody TestCompanyJsonEntity testCompanyEntity) {
        boolean BooleStatus  = entrustService.addCompanyData(testCompanyEntity);
        if(BooleStatus){
            return ResultUtil.success();
        }
        return ResultUtil.error(201,"增加数据失败");
    }

    /**
     *
     * @param sampleEntity
     * @return
     */
    @RequestMapping("get_Sample")
    public Result ReturnSampleData(SampleEntity sampleEntity) {
        return ResultUtil.success(entrustService.getSampleDataList(sampleEntity));
    }

    /**
     * 查询产品所有的检测项
     * @param productId
     * @return
     */
    @RequestMapping("/getAllItem")
    public Result getAllItemByProductId(Integer productId){
        if(productId == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }else{
            return ResultUtil.success(entrustService.getAllItemByProductId(productId));
        }
    }

    /**
     * 查询检测项详情：检测项名称，检测项方法，规格型号，检测依据
     * @param itemIds
     * @return
     */
    @RequestMapping("/getItemDetail")
    public Result getItemDetail(@RequestBody CheckItemParamVo itemIds){
        if(itemIds == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }else{
            return ResultUtil.success(entrustService.getCheckItemInfoVo(itemIds.getIds()));
        }
    }
    /**
     * 样品基本信息--查询产品
     * @param productName
     * @return
     */
    @RequestMapping("/get_sample_product_name")
    public Result getAllItemByProductName(String productName){
        List<LabelValueVo> dataList = entrustService.selectProductList(productName);
        if(dataList.isEmpty()){
            return ResultUtil.error(ResultEnum.DATA_IS_NULL.getCode(),ResultEnum.DATA_IS_NULL.getMsg());
        }
       return ResultUtil.success(dataList);
    }

    /**
     * 样品基本信息--保存
     * @param
     * @return
     */
    @RequestMapping(value="add_sample", method= RequestMethod.POST)
//    public Result getAddSampleData(@RequestBody SampleAddParamVo samples, @RequestParam("file")MultipartFile file) {
    public Result getAddSampleData(@RequestParam("json") String json,MultipartFile[] file) {
        SampleAddParamVo samples = JSON.parseObject(json, SampleAddParamVo.class);
//        System.out.println("产品信息："+samples.toString());
//        for (int i = 0; i < file.length; i++) {
//            System.out.println("样品文件信息"+file[i].getOriginalFilename());
//        }
        entrustService.addSampleData(samples,file);
        return ResultUtil.success();
//        if(samples == null){
//            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
//        }else{
//
//        }
    }

    /**
     * 样样品的基本信息-图片信息保存
     * @param testSampleJsonEntity
     * @return
     */
    @RequestMapping(value="sample_add_picture", method= RequestMethod.POST)
    public Result getAddSamplePhotoData(TestSampleJsonEntity testSampleJsonEntity,MultipartHttpServletRequest uploadFile) {

        System.out.println("接收信息处理"+testSampleJsonEntity);
        return null;
//        InputStream inputStream = null;
//
//        MultipartFile file = uploadFile.getFile("uploadFile1");
//        try {
//            inputStream = file.getInputStream();
//        }catch (IOException e){
//            System.out.println("文件获取异常:{}"+e);
//        }
//        return null;
    }

    /**
     * 查询历史委托
     * @param entrustHistoryEntity
     * @return
     */
    @RequestMapping("/get_entrust_history")
    public Result getEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity){
        return ResultUtil.success(entrustService.getEntrustHistoryList(entrustHistoryEntity));
    }

    /**
     * 查询历史委托信息详情
     * @param entrustmentId
     * @return
     */
    @RequestMapping("/get_entrust_history_detail")
    public Result getEntrustHistoryDetail(Long entrustmentId){
        return ResultUtil.success(entrustService.getEntrustHistoryDetail(entrustmentId));
    }

    /**
     * 查询产品判定依据
     * @param productId
     * @return
     */
    @RequestMapping("/getJudgeBasis")
    public Result getJudgeBasis(Integer productId){
        if(productId == null ){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }else{
            return ResultUtil.success(entrustService.getJudges(productId));
        }
    }
}

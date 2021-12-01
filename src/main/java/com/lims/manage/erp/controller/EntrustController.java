package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TestSampleJsonEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.vo.CheckItemParamVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.commons.lang.StringUtils;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/entrust/")
public class EntrustController {

    @Autowired
    private EntrustService entrustService;

    @RequestMapping("/addEntrust")
    public Result addEntrust(@RequestBody EntrustAddVo entrust){
        Boolean isSuccess = entrustService.addEntrust(entrust);
        if(isSuccess){
            return ResultUtil.success();
        }else{
            return ResultUtil.error(678,"新增委托失败！");
        }
    }
    @GetMapping("get_Basics")
    public Result ReturnBasicsData()
    {
        return ResultUtil.success(entrustService.returnEntrustData());
    }
    @GetMapping("get_entrusted_unit")
    public Result methodDispay(Integer companyId)
    {
        List<TestCustomerJsonEntity> collectList = entrustService.returnTestCustomerEntityList(companyId);
        if(collectList.isEmpty()){
            return ResultUtil.error(201,"用户信息不存在");
        }
            return ResultUtil.success(collectList);
    }
    @PostMapping("add_new_company")
    public Result methodPost(@RequestBody TestCompanyJsonEntity testCompanyEntity)
    {
        boolean BooleStatus  = entrustService.addCompanyData(testCompanyEntity);
        if(BooleStatus){
            return ResultUtil.success();
        }
        return ResultUtil.error(201,"增加数据失败");
    }
    @RequestMapping("get_Sample")
    public Result ReturnSampleData(SampleEntity sampleEntity)
    {
        return ResultUtil.success(entrustService.getSampleDataList(sampleEntity));
    }



    @RequestMapping("/getAllItem")
    public Result getAllItemByProductId(Integer productId){
        if(productId == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }else{
            return ResultUtil.success(entrustService.getAllItemByProductId(productId));
        }
    }

    @RequestMapping("/getItemDetail")
    public Result getItemDetail(@RequestBody CheckItemParamVo itemIds){
        System.out.println("星星："+itemIds.getIds());
        List<Integer> ids = itemIds.getIds();
        for (int i = 0; i < ids.size(); i++) {
            System.out.println("星星1："+ids.get(i));
        }
        return ResultUtil.success(entrustService.getCheckItemInfoVo(itemIds.getIds()));
//        if(itemIds == null){
//            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
//        }else{
//
//        }
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
     * @param testSampleJsonEntity
     * @return
     */
    @RequestMapping(value="add_sample", method= RequestMethod.POST)
    public Result getAddSampleData(@RequestBody TestSampleJsonEntity testSampleJsonEntity) {

        System.out.println("接收信息处理"+testSampleJsonEntity);
        return null;
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





}

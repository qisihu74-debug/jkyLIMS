package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TaskEntity;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.entity.TestSampleJsonEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.CheckItemParamVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.SampleAddParamVo;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/entrust/")
public class EntrustController {

    @Autowired
    private EntrustService entrustService;
    @Autowired
    private LogManagerService logManagerService;

    /**
     * 新增委托
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/addEntrust")
    //@RequiresPermissions("entrust:entrust:addEntrust")
    public Result addEntrust(@RequestParam("json") String json, MultipartFile[] file){
        EntrustAddVo entrust = JSON.parseObject(json,EntrustAddVo.class);
        Boolean isSuccess = entrustService.addEntrust(entrust,file);
        if(isSuccess){
            return ResultUtil.success();
        }else{
            return ResultUtil.error(678,"新增委托失败！");
        }
    }

    /**
     * 修改委托
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/updateEntrust")
//    @RequiresPermissions("entrust:entrust:updateEntrust")
    public Result updateEntrust(@RequestParam("json") String json, MultipartFile[] file){
        EntrustAddVo entrust = JSON.parseObject(json,EntrustAddVo.class);
        Boolean isSuccess = entrustService.updateEntrust(entrust,file);
        if(isSuccess){
            return ResultUtil.success();
        }else{
            return ResultUtil.error(678,"修改委托失败！");
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
//        return ResultUtil.success(entrustService.getSampleDataList(sampleEntity));
        return ResultUtil.success(entrustService.getSampleDataList(sampleEntity));
    }

    @RequestMapping("/getSampleDetail")
    public Result getSampleDetail(@RequestBody SampleEntity paramVo) {
        System.out.println("参数："+paramVo);
        return ResultUtil.success(entrustService.selectSampleList2(paramVo));
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
     * 查询检测项 方法 依据
     * @param id
     * @return
     */
    @RequestMapping("/getItemMethodStandard")
    public Result getItemMethodStandard(Integer itemId){
        if(itemId == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }else{
            return ResultUtil.success(entrustService.getItemMethodStandard(itemId));
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
    public Result getAddSampleData(@RequestParam("json") String json,MultipartFile[] file) {
        SampleAddParamVo samples = JSON.parseObject(json, SampleAddParamVo.class);
        if(samples == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }else{
            entrustService.addSampleData(samples, file);
            return ResultUtil.success();
        }
    }

    /**
     * 样品基本信息--修改
     * @param
     * @return
     */
    @RequestMapping(value="update_sample", method= RequestMethod.POST)
    public Result updateSampleData(@RequestBody SampleEntity sampleEntity) {
        if(sampleEntity == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }else{
            return ResultUtil.success(entrustService.updateSampleInfo(sampleEntity));
        }
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
     * 1、角色过滤 “客户代表”、“市场部业务员”
     * 2、“客户”指定自身委托
     * @param entrustHistoryEntity
     * @return
     */
    @RequestMapping("/get_entrust_history")
    @RequiresPermissions("test:entrust:get_entrust_history")
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

    /**
     * 委托单发布，转为任务
     * @param entity
     * @return
     */
    @PostMapping("publishTask")
    //@RequiresPermissions("entrust:task:publishTask")
    public Result publishTask(@RequestBody TaskEntity entity){
        if (entity.getEntrustmentId() == null){
            return ResultUtil.error(-1,"缺少必要参数");
        }
        //核查委托单位、委托人、委托人联系方式、样品信息、检测项信息是否完整
        EntrustAddVo vo = entrustService.getEntrustHistoryDetail(entity.getEntrustmentId());
        if (StringUtils.isEmpty(vo.getEntrustCompany()) || StringUtils.isEmpty(vo.getEntrustPeople())
                || StringUtils.isEmpty(vo.getEntrustPhone())){
            return ResultUtil.error(-1,"请检查委托人信息是否完整！");
        }
        List<SampleEntity> samples = vo.getSamples();
        if (CollectionUtils.isEmpty(samples)){
            return ResultUtil.error(-1,"请检查委托单样品信息是否完整！");
        }
        if (!CollectionUtils.isEmpty(samples)){
            for (SampleEntity sampleEntity:samples) {
                if (CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVos())){
                    return ResultUtil.error(-1,"请检查委托单样品下检测项信息是否完整！");
                }
            }
        }
        Boolean flag = entrustService.publishTask(entity);
        if (flag){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"账户："+ShiroUtils.getUserInfo().getUsername()+"发布任务成功编号为："+vo.getEntrustmentNo(),
                    Const.ENTRUST_PUBLISH,true);
            return ResultUtil.success("委托发布成功！");
        }else {
            return ResultUtil.error(-1,"委托发布失败！");
        }
    }

    /**
     * 委托单任务待发布列表
     * @param entrustHistoryEntity
     * @return
     */
    @RequestMapping("/releasedList")
    @RequiresPermissions("test:entrust:releasedList")
    public Result getEntrustReleasedList(EntrustHistoryEntity entrustHistoryEntity){
        return ResultUtil.success(entrustService.getEntrustReleasedList(entrustHistoryEntity));
    }

    /**
     * 下载样品标签
     * @param sampleId
     * @return
     */
    @RequestMapping("/downloadSampleTag")
    public ResponseEntity<byte[]> downloadSampleTag(Integer sampleId){
        ResponseEntity<byte[]> sampleTagInfo = entrustService.getSampleTagInfo(sampleId);
        if( sampleTagInfo == null ){
            return new ResponseEntity<byte[]>(null,null, HttpStatus.NOT_FOUND);
        }else{
            return sampleTagInfo;
        }
//        if(sampleId == null ){
//            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
//        }else{
//
//        }
    }

    @RequestMapping("/downloadSampleTag2")
    public Result downloadSampleTag2(Integer sampleId){
        String sampleTagInfo2 = entrustService.getSampleTagInfo2(sampleId);
        return ResultUtil.success(sampleTagInfo2);
    }

    /**
     * 委托单下载
     * @param entrustId
     * @return
     */
    @RequestMapping("downloadEntrust")
    public void downloadEntrust(Long entrustId,HttpServletResponse response){
        String fileName = "BD20210021.docx";
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_entrust_template, fileName);
            //填充数据
            EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
            XWPFDocument document = entrustService.downloadEntrust(detail, object);
            response.reset();
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "attachment;fileName="+"委托单.doxc");
            OutputStream outputStream = response.getOutputStream();
            document.write(outputStream);
            outputStream.close();
        } catch (Exception ex) {
            log.info("导出失败：", ex.getMessage());
        }
    }

}

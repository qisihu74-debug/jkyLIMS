package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
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
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            return ResultUtil.success("成功");
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
     * @param itemId
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
     * 查询历史委托
     * 1、角色过滤 “客户代表”、“市场部业务员”
     * 2、“客户”指定自身委托
     * @param entrustHistoryEntity
     * @return
     */
    @RequestMapping("/get_entrust_history")
//    @RequiresPermissions("test:entrust:get_entrust_history")
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
            response.setCharacterEncoding("UTF-8");
            fileName = URLEncoder.encode(fileName,"UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName="+fileName);
            OutputStream outputStream = response.getOutputStream();
            document.write(outputStream);
            outputStream.close();
        } catch (Exception ex) {
            log.info("导出失败：", ex.getMessage());
        }
    }

}

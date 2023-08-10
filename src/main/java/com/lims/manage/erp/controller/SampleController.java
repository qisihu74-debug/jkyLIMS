package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Lists;
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
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.SampleAddParamVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.SampleOutPutVo;
import com.lims.manage.erp.vo.SamplesAddVo;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    @RequestMapping("/downloadSampleTagOld")
    public void downloadSampleTag(Integer sampleId,Integer number, HttpServletResponse response) {
        if(number==null||number<=0){
            number = 1;
        }
        SampleDetailVo sampleTagInfo = sampleService.getSampleTagInfo(sampleId);
        HashMap<String, SampleDetailVo> result = Maps.newHashMap();
        StringBuilder fileName = new StringBuilder("");
        if (sampleTagInfo != null) {
            // 样品编号格式： 情况1： YP-2022-0095（01~02） 情况2：YP-2022-0096
            String sampleCode = sampleTagInfo.getSampleCode();
            int startNumber = sampleCode.indexOf("（");
            int endNumber = sampleCode.indexOf("）");
            if (startNumber > 0 && endNumber > 0) {
                String[] strings = sampleCode.substring(startNumber + 1, endNumber).split("~");
                Integer maxNumber = Integer.valueOf(strings[0]);
                Integer smallNumber = Integer.valueOf(strings[1]);
                if (maxNumber <= number && number <= smallNumber) {
                    log.info("样品id\t" + sampleId + "编号" + "最大参数\t" + maxNumber + ";最小参数\t" + smallNumber + "取值参数在范围之内。\t" + number);
                    sampleTagInfo.setSampleCode(sampleCode.substring(0, startNumber) + "_" + number);
                } else {
                    log.info("样品id\t" + sampleId + "编号" + "最大参数\t" + maxNumber + ";最小参数\t" + smallNumber + "取值参数不在范围之内。\t" + number);
                    sampleTagInfo.setSampleCode(sampleCode.substring(0, startNumber) + "_" + number + "取值参数不在范围之内。");
                }
            }
            // 处理样品描述信息 Outward 清除两边[]
            sampleTagInfo.setOutward(sampleTagInfo.getOutward().substring(1, sampleTagInfo.getOutward().length() - 1));
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

    @RequestMapping("/downloadSampleTag")
    public void downloadSampleTagZip(Integer sampleId, HttpServletResponse response) {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String location = req.getServletContext().getRealPath("/file/");
        List<HashMap<String, SampleDetailVo>> sampleTagInfoList = sampleService.getSampleTagInfoList(sampleId);
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(response.getOutputStream());
            XLSTransformer transformer = new XLSTransformer();
//            InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-template.xlsx");
            List<String> fileName = Lists.newArrayList();
            for (HashMap<String, SampleDetailVo> stringSampleDetailVoHashMap : sampleTagInfoList) {
                InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-template.xlsx");
                FileOutputStream fileOutputStream = new FileOutputStream(location+stringSampleDetailVoHashMap
                        .get("result").getSampleCode()+"标签.xlsx");
                Workbook workbook = null;
                try {
                    workbook = transformer.transformXLS(fileStream, stringSampleDetailVoHashMap);
                    workbook.write(fileOutputStream);
                    fileName.add(location+stringSampleDetailVoHashMap.get("result").getSampleCode()+"标签.xlsx");
                    fileOutputStream.close();
                } catch (InvalidFormatException e) {
                    e.printStackTrace();
                }
            }
            InputStream input = null;
            for (String filePath :fileName) {
                File file = new File(filePath);
                // 一个文件对应一个ZipEntry
                ZipEntry zipEntry = new ZipEntry(file.getName());
                input = new FileInputStream(file);
                zos.putNextEntry(zipEntry);
                IOUtils.copy(input, zos);
                input.close();
                file.delete();
            }
            zos.close();
            response.flushBuffer();


////            response.reset();
//            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
//            response.setContentType("application/x-msdownload");
//            response.setCharacterEncoding("UTF-8");
//            String fileName2 = URLEncoder.encode("样品标签.zip", "UTF-8");
//            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName2);
//            OutputStream outputStream = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载样品标签
     */
    @RequestMapping("/downloadNewSampleTab")
    public void downloadNewSampleTab(Integer sampleId, HttpServletResponse response) throws IOException {
        if (sampleId == null){
            return;
        }
        //样品标签2023二月份之前的样品标签（按照收样日期）下载为新版本的二维码标签，否则下载老板的样品标签
        // 。
        SampleDetailVo sampleTagInfo = sampleService.getSampleTagInfo(sampleId);
        if (sampleTagInfo != null){
            response.reset();
            //判断样品接收时间
            String receivedDate = sampleTagInfo.getReceivedDate();
            Date dateFromStr = DateUtil.timeFormat(receivedDate);
            SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyyMMdd");
            String str = yyyyMMddHH_NOT_.format(dateFromStr);
            if (Integer.parseInt(str) >= 20230201){
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Content-Disposition", "attachment;fileName=" +  java.net.URLEncoder.encode("样品标签.xlsx", "UTF-8") );
                ServletOutputStream outputStream = sampleService.downloadNewSampleTab(1,sampleId,sampleTagInfo, response);
                outputStream.flush();
                outputStream.close();
            }else {
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                response.setContentType("application/zip");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Content-Disposition", "attachment;fileName=" +  java.net.URLEncoder.encode("样品文件.zip", "UTF-8") );
                ZipOutputStream zipOutputStream = sampleService.packagingWorkbookZip(sampleId, response);
                zipOutputStream.flush();
            }
        }
    }

    /**
     * 下载留样标签
     * @param sampleId
     * @param response
     * @throws IOException
     */
    @RequestMapping("/downloadRetentionTab")
    public void downloadRetentionSampleTab(Integer sampleId, HttpServletResponse response) throws IOException {
        if (sampleId == null){
            return;
        }
        SampleDetailVo sampleTagInfo = sampleService.getSampleTagInfo(sampleId);
        if (sampleTagInfo != null){
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" +  java.net.URLEncoder.encode("样品留样标签.xlsx", "UTF-8") );
            ServletOutputStream outputStream = sampleService.downloadNewSampleTab(2,sampleId,sampleTagInfo, response);
            outputStream.flush();
            outputStream.close();
        }
    }

    /**
     * 留样标签扫码
     * @param sampleId
     * @return
     */
    @RequestMapping("retentionInfo")
    public Result retentionInfo(Integer sampleId){
        if (sampleId == null){
            return ResultUtil.error("缺少参数");
        }
        log.info("留样扫码请求参数:{}",sampleId);
        TestSampleEntity entity = sampleService.sampleInfo(2,sampleId);
        log.info("留样扫码响应结果:{}",JSON.toJSONString(entity));
        return ResultUtil.success(entity);
    }

    /**
     * 下载样品标签  多个或一个都打包成zip格式。
     */
    @RequestMapping("/downloadSamplePackagingZip")
    public void downloadSamplePackagingZip(Integer sampleId, HttpServletResponse response) throws IOException {
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" +  java.net.URLEncoder.encode("样品文件.zip", "UTF-8") );
        ZipOutputStream zipOutputStream = sampleService.packagingWorkbookZip(sampleId, response);
        zipOutputStream.flush();
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
            if (integer > 0) {
                return ResultUtil.success("添加样品成功！", integer);
            } else {
                return ResultUtil.error("添加样品失败，请联系管理员！");
            }
        }
    }

    /**
     * 添加委托查询样品列表
     *
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
     * 样品查询打印列表
     *
     * @param sampleEntity
     * @return
     */
    @RequestMapping("/showSampleList")
    public Result showSampleList(@RequestBody TestSampleEntity sampleEntity) {
        if (sampleEntity.getPageNum() == null || sampleEntity.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数！");
        }

        return ResultUtil.success(testSampleEntityService.showSampleList(sampleEntity));
    }

    /**
     * 查询样品详情
     *
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

    /**
     * 样品修改
     *
     * @param sampleEntity
     * @return
     */
    @RequestMapping("/updateSampleInfo")
    public Result updateSample(@RequestBody TestSampleEntity sampleEntity) {
        if (sampleEntity == null || sampleEntity.getId() == null) {
            return ResultUtil.error("缺少必要参数！");
        }
         //   根据样品ID 和委托单位 id 实现变更样品委托单位
        if(sampleEntity.getId()!=null&&sampleEntity.getCompanyId()!=null){
             if(testSampleEntityService.judgmentSampleUnit(sampleEntity.getId(),sampleEntity.getCompanyId())==false){
                 return ResultUtil.error("样品委托单位变更失败，样品已关联委托单信息！！！");
            }
        }
        int i;
        if(sampleEntity.getSampleType().contains("配合比")){
            i = testSampleEntityService.updateSampleBatch(sampleEntity);
        }else{
            i = testSampleEntityService.updateSample(sampleEntity);
        }
        if (i > 0) {
            return ResultUtil.success("样品信息修改成功！");
        } else {
            return ResultUtil.error("样品信息修改失败！");
        }
    }

    /**
     * 上传样品文件  单个上传。
     */
    @RequestMapping("/uploading/{id}")
    public Result uploading(@PathVariable("id") Integer id, MultipartFile[] file) {
        if (id == null && "".equals(id)) {
            return ResultUtil.error("缺少必填参数！");
        }
        if (file.length == 0) {
            return ResultUtil.error("样品file文件为空！");
        }
        // 处理file文件后缀
        // 常规图片后缀名。
        String[] nameSuffixS = Const.nameSuffixS;
        // true 正常运行。 flase 返回数组中不存在格式。
        Boolean flag=false;
        for (MultipartFile multipartFile : file) {
            String name = multipartFile.getOriginalFilename();
            String[] strings = name.split("\\.");
            String nameSuffix = strings[strings.length - 1];
            for(int i=0;i<nameSuffixS.length;i++){
                if(nameSuffixS[i].equalsIgnoreCase(nameSuffix)){
                    // 变动
                    flag = true;
                }
            }
            if(flag == false){
               return ResultUtil.error("样品文件上传失败 图片后缀不在通用规则中！");
            }
        }
        if (testSampleEntityService.uploading(id, file) == true) {
            return ResultUtil.success("样品文件上传成功");
        }
        return ResultUtil.error("样品文件上传失败");
    }

    /**
     * 删除文件id
     */
    @RequestMapping("/removeding/{id}")
    public Result removeding(@PathVariable("id") Integer id) {
        testSampleEntityService.removeding(id);
        return ResultUtil.success("样品文件删除成功");
    }

    /**
     * 样品管理--样品签收--配合比新增样品
     *
     * @param samples
     * @return
     */
    @RequestMapping("/addMixSamples")
    public Result addMixSamples(@RequestBody SamplesAddVo samples) {
            Integer integer = testSampleEntityService.batchInsertMixSample(samples);
            if (integer > 0) {
                return ResultUtil.success("添加样品成功！", integer);
            } else {
                return ResultUtil.error("添加样品失败，请联系管理员！");
            }
    }

    /**
     * 扫描查询样品流转详情
     * @return
     */
    @RequestMapping("sampleInfo")
    public Result sampleInfo(Integer sampleId, Integer type){
        if (sampleId == null){
            return ResultUtil.error("缺少参数");
        }
        log.info("扫码请求参数:{}",sampleId);
        TestSampleEntity entity = sampleService.sampleInfo(type,sampleId);
        log.info("扫码响应结果:{}",JSON.toJSONString(entity));
        return ResultUtil.success(entity);
    }

    /**
     * 跟新状态
     * @param sampleId
     * @param state
     * @param sampleRetentionPeriod  留样天数  (state =3 留样)
     * @param sampleProcessMode 样品处置方式（state=4处置）
     * @return
     */
    @GetMapping("updateState")
    public Result updateState(Integer sampleId, Integer state, String time,Integer saveTime,
                              Integer sampleRetentionPeriod,String sampleProcessMode,String approver){
        System.out.println("操作扫描样品id：=="+sampleId+"操作状态：=="+state+"操作人：=="+approver+"saveTime:=="+saveTime);
        System.out.println("留样天数: == "+sampleRetentionPeriod + " == 样品处置方式 == " + sampleProcessMode);
        System.out.println("扫码时间:{}"+time);
        log.info("扫码时间:{}",time);
        Date date= null;
        if (StringUtils.isEmpty(time)){
            date = new Date(System.currentTimeMillis());
        }else {
            SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd");
            try {
                date = simpleDateFormat.parse(time);
            }catch (Exception e){
                log.error("时间转换异常:{}",e);
            }
        }
        if (sampleId == null || state == null){
            return ResultUtil.error("缺少参数");
        }
        Integer flag = sampleService.updateState(sampleId,state,date,saveTime,sampleRetentionPeriod,sampleProcessMode,approver);
        if (flag == 0){
            return ResultUtil.success("操作成功");
        }else if (flag == 1){
            return ResultUtil.error("操作失败,可能其它账号已经操作");
        } else{
            return ResultUtil.error("操作失败,样品未绑定");
        }
    }

    /**
     * 配合比可选择导入样品列表
     * @param sampleCode
     * @param companyId
     * @return
     */
    @GetMapping("/importSampleList")
    public Result importSampleList(String sampleCode,Integer companyId,Integer pageNum,Integer pageSize) {
        if (companyId == null) {
            return ResultUtil.error("请先选择委托单位！");
        }
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success(testSampleEntityService.importSampleList(sampleCode,companyId,pageNum,pageSize));
    }

    /**
     * 样品台账列表
     */
    @RequestMapping("/SampleTabList")
    public void SampleTabList() {

    }

    /**
     * 样品台账列表导出
     */
    @RequestMapping("/exportSampleTab")
    public void exportSampleTab(Integer sampleId, HttpServletResponse response) throws IOException {
        if (sampleId == null){
            return;
        }
        SampleDetailVo sampleTagInfo = sampleService.getSampleTagInfo(sampleId);
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" +  java.net.URLEncoder.encode("样品入库登记表.xlsx", "UTF-8") );
        ServletOutputStream outputStream = sampleService.downloadNewSampleTab(0,sampleId,sampleTagInfo, response);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 出入库台账列表
     */
    @RequestMapping("/EntrustSampleTabList")
    public void EntrustSampleTabList() {

    }

    /**
     * 出入库台账列表导出
     */
    @RequestMapping("/exportEntrustSampleTab")
    public void exportEntrustSampleTab(Integer sampleId, HttpServletResponse response) throws IOException {
        if (sampleId == null){
            return;
        }
        SampleDetailVo sampleTagInfo = sampleService.getSampleTagInfo(sampleId);
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" +  java.net.URLEncoder.encode("委托样品出入库登记表.xlsx", "UTF-8") );
        ServletOutputStream outputStream = sampleService.downloadNewSampleTab(0,sampleId,sampleTagInfo, response);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 样品留样列表
     * @param sampleOutPutVo
     * @return
     */
    @PostMapping("/sampleRetentionList")
    public Result sampleRetentionList(@RequestBody SampleOutPutVo sampleOutPutVo ){
        if(org.springframework.util.StringUtils.isEmpty(sampleOutPutVo.getPageNum()) &&
                org.springframework.util.StringUtils.isEmpty(sampleOutPutVo.getPageSize())){
            return ResultUtil.error("缺少必填参数！！！");
        }
        return ResultUtil.success(sampleService.sampleReservedSamplePageInfoList(sampleOutPutVo));
    }

    /**
     * 样品留样列表 导出
     * @param sampleOutPutVo
     * @param response
     */
    @PostMapping("/sampleRetentionExport")
    public void sampleRetentionExport(@RequestBody SampleOutPutVo sampleOutPutVo, HttpServletResponse response ) throws Exception {
        BufferedOutputStream bos = null;
        String fileName = "样品留样";
        String file = fileName+".xls";
        file = URLEncoder.encode(file, "UTF-8");
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;fileName=" + file);
        InputStream inputStream = sampleService.sampleRetentionExport(sampleOutPutVo);
        ServletOutputStream outputStream = response.getOutputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        bos = new BufferedOutputStream(outputStream);
        byte[] buff = new byte[2048];
        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
            bos.flush();
        }
        bos.close();
    }

    /**
     * 返回技术负责人列表
     * @return
     */
    @GetMapping("/getApprover")
    public Result getApprover(){
        return ResultUtil.success(sampleService.getApprover());
    }

    /**
     * 样品留样 更新
     * @param sampleOutPutVo
     */
    @PostMapping("/sampleRetentionUpdate")
    public Result sampleRetentionUpdate(@RequestBody SampleOutPutVo sampleOutPutVo) {
        if(org.springframework.util.StringUtils.isEmpty(sampleOutPutVo.getSampleId()) ||
                org.springframework.util.StringUtils.isEmpty(sampleOutPutVo.getSampleReservedRemrk())){
            return ResultUtil.error("缺少必填参数！！！");
        }
       Boolean status = sampleService.sampleRetentionUpdate(sampleOutPutVo);
        if(!status){
            return ResultUtil.error("更新样品留样备注失败");
        }
        return  ResultUtil.success("更新样品留样成功");
    }

    /**
     * 样品出入库列表
     * @param sampleOutPutVo
     * @return
     */
    @PostMapping("/sampleOutPutList")
    public Result sampleOutPutList(@RequestBody SampleOutPutVo sampleOutPutVo ){
        if(org.springframework.util.StringUtils.isEmpty(sampleOutPutVo.getPageNum()) &&
                org.springframework.util.StringUtils.isEmpty(sampleOutPutVo.getPageSize())){
            return ResultUtil.error("缺少必填参数！！！");
        }
        System.out.print("请求参数 == sampleOutPutVo ="+ sampleOutPutVo);
        return ResultUtil.success(sampleService.sampleRetentionPageInfoList(sampleOutPutVo));
    }

    /**
     * 样品出入库列表 导出
     * @param sampleOutPutVo
     * @param response
     */
    @PostMapping("/sampleOutPutExport")
    public void sampleOutPutExport(@RequestBody SampleOutPutVo sampleOutPutVo, HttpServletResponse response ) throws Exception {
        BufferedOutputStream bos = null;
        String fileName = "样品出入库登记表";
//        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/vnd.ms-excel");
        String file = fileName+".xls";
        file = URLEncoder.encode(file, "UTF-8");
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;fileName=" + file);
        InputStream inputStream = sampleService.sampleOutPutExport(sampleOutPutVo);
        ServletOutputStream outputStream = response.getOutputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        bos = new BufferedOutputStream(outputStream);
        byte[] buff = new byte[2048];
        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
            bos.flush();
        }
        bos.close();
    }

    /**
     * 样品出入库 更新
     * @param sampleOutPutVo
     */
    @PostMapping("/sampleOutPutUpdate")
    public Result sampleOutPutUpdate(@RequestBody SampleOutPutVo sampleOutPutVo) {
        if(org.springframework.util.StringUtils.isEmpty(sampleOutPutVo.getSampleId()) ||
                org.springframework.util.StringUtils.isEmpty(sampleOutPutVo.getSampleOutPutRemrk())){
            return ResultUtil.error("缺少必填参数！！！");
        }
        Boolean status = sampleService.sampleOutPutUpdate(sampleOutPutVo);
        if(!status){
            return ResultUtil.error("更新样品出入库备注失败");
        }
        return  ResultUtil.success("更新样品出入库成功");
    }

}

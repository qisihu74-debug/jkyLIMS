package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
//import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/report/")
public class ReportController {
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private EntrustService entrustService;
    @Autowired
    private ReportApprovalMapper reportApprovalMapper;
//    @Autowired
//    private ReportRecordEntityMapper recordEntityMapper;

    Logger logger = LoggerFactory.getLogger(ReportController.class);

    /**
     * 查询可制作报告任务单列表
     *
     * @return
     */
    @GetMapping("/list1")
    public Result getSampleList1() {
        return ResultUtil.success("获取可制作报告任务单成功！", reportService.getReportList());
    }

    /**
     * 查询可制作报告任务单列表--科室
     *
     * @return
     */
    @GetMapping("/list")
    public Result getSampleList(Integer pageNum,Integer pageSize,String search) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success("获取可制作报告任务单成功！", reportService.makeReport(pageNum,pageSize,search));
    }

    /**
     * 查询可制作报告任务单列表--科室
     *
     * @return
     */
    @GetMapping("/reportDownloadList")
    public Result reportDownloadList(Integer pageNum,Integer pageSize,String search) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success("获取报告下载列表成功！", reportService.reportDownloadList(pageNum,pageSize,search));
    }

    /**
     * 查询出具报告的列表--科室权限
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/reportDownloadListHistory")
    public Result reportDownloadListHistory(String search,Integer pageNum,Integer pageSize) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success("获取出具报告历史列表成功！", reportService.reportDownloadListHistory(search,pageNum,pageSize));
    }


    /**
     * 提交审批
     * @param
     * @return
     */
    @GetMapping("/report_submit")
    public Result getReportSubmit(ReportRecordEntity reportRecordEntity) {
        if (reportRecordEntity.getEntrustmentId() == null || reportRecordEntity.getVerifyer()==null || reportRecordEntity.getIssuer()==null ) {
            return ResultUtil.error(678, "缺少必要参数！");
        }
        //1、 获取提交报告人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error(678, "token已经过期，请退出重新登录");
        }
        String name = reportApprovalMapper.getUserName(userInfo.getUserId());
        if (name == null) {
            return ResultUtil.error(678, "账号未配置使用人");
        }
        reportRecordEntity.setApplicant(name);
        Boolean flag = reportService.getReportSubmit_two(reportRecordEntity);
        if (flag) {
            return ResultUtil.success("提交审批成功");
        }
        return ResultUtil.error("提交审批失败");
    }

    /**
     * 查询报告生成列表--历史查询
     *
     * @param search
     * @return
     */
    @GetMapping("/list_history")
    public Result getlist_history(String search,Integer pageNum,Integer pageSize) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success("获取历史任务单成功！", reportService.getReportList_history(search,pageNum,pageSize));
    }

    /**
     * 查询报告生成列表--历史查询_详情
     *
     * @param id
     * @return
     */
    @GetMapping("/list_history_details")
    public Result getlist_history_details(Long id) {
        return ResultUtil.success("获取历史任务单详情成功！", reportService.getReportList_history_details(id));
    }


    /**
     * 报告生成--编辑按钮
     *
     * @return
     */
    @GetMapping("/edit1")
    public Result edit1(Long id) {
        return ResultUtil.success("查询委托单信息成功！", reportService.getReportDetail(id));
    }

    /**
     * 报告生成--编辑按钮--科室
     *
     * @return
     */
    @GetMapping("/edit")
    public Result edit(Long taskId) {
        return ResultUtil.success("查询任务单详情成功！", reportService.getReportDetail(taskId));
    }

    /**
     * 报告生成--编辑按钮--科室
     *
     * @return
     */
    @GetMapping("/getQuota")
    public Result getQuota(Long taskId) {
        return ResultUtil.success("查询指标信息成功！", reportService.getQuota(taskId));
    }

    /**
     * 查询当前委托单所有检测项
     * @param id
     * @return
     */
    @GetMapping("/detail")
    public Result detail(Long id) {
        return ResultUtil.success("查询委托单信息成功！", reportService.getDetailCheckItem(id));
    }

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("/preserve")
    public Result preserve(@RequestBody ReportPreserveVo vo) {
        Boolean preserve = reportService.preserve(vo);
        if (preserve) {
            return ResultUtil.success("保存成功！", preserve);
        } else {
            return ResultUtil.error(ResultEnum.PRESERVE_FAIL.getCode(), ResultEnum.PRESERVE_FAIL.getMsg());
        }
    }

    /**
     * 待盖章和历史盖章列表查询
     * @param search
     * @param pageNum
     * @param pageSize
     * @param state 1合同待发起,2合同待创建，3合同待签署，4合同待盖章，5合同待下载
     * @return
     */
    @GetMapping("sealList")
    public Result sealList(String search, Integer pageNum, Integer pageSize, String reportType,String state) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少必要的参数！");
        }
        PageInfo pageInfo = reportService.sealList(search, pageNum, pageSize, reportType,state);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 根据任务单id 回显数据
     *
     * @param id
     * @return
     */
    @GetMapping("getDetail")
    public Result getDetail(Long id) {
        if (id == null) {
            return ResultUtil.error("缺少必要的参数！");
        }
        return ResultUtil.success(reportService.getDetail(id));
    }

    /**
     * 报告邮寄编辑
     *
     * @param reportRecordEntity
     * @return
     */
    @PostMapping("saveMessage")
    public Result saveMessage(@RequestBody ReportRecordEntity reportRecordEntity) {
        if (reportRecordEntity.getId() == null) {
            return ResultUtil.error("缺少必要的参数！");
        }
        //1、 获取抢单人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error(678, "token已经过期");
        }
        String name = reportApprovalMapper.getUserName(userInfo.getUserId());
        if (name == null) {
            return ResultUtil.error(678, "账号未配置使用人");
        }
        // 报告操作人
        reportRecordEntity.setReportManager(name);
        // 操作时间
        reportRecordEntity.setOperateTime(new Date());
        Boolean flag = reportService.saveMessage(reportRecordEntity);
        if (flag) {
            return ResultUtil.success("编辑报告信息成功!");
        }
        return ResultUtil.error("编辑报告信息失败!");
    }

    /**
     * 向契约锁发起盖章合同申请
     * @param entrustId
     * @return
     */
    @GetMapping("sealApprove")
    public Result seal(Long entrustId,String title,String fileType) {
        if (entrustId == null) {
            return ResultUtil.error("缺少必要的参数！");
        }
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(fileType)){
            return ResultUtil.error("缺少合同发起参数！");
        }
        Boolean flag = reportService.seal(entrustId,title,fileType);
        if (flag) {
            return ResultUtil.success("向契约锁发起盖章合同申请成功!");
        } else {
            return ResultUtil.error("向契约锁发起盖章合同申请失败！");
        }
    }

    /**
     * 创建合同
     * @param reqBean
     * @return
     */
    @PostMapping("createbycategory")
    public Result createbycategory(@RequestBody QiYueSuoReqBean reqBean) {
        if (reqBean == null){
            return ResultUtil.error("缺少必要的参数");
        }
        QiYueSuoResponse response = reportService.createbycategory(reqBean);
        if (response != null && response.getCode() == 0) {
            return ResultUtil.success("向契约锁发起报告制作申请成功!");
        } else {
            return ResultUtil.error("向契约锁发起报告制作申请失败："+response.getMessage());
        }
    }

    /**
     * 报告合同签署url获取
     * @param reqBean
     * @return
     */
    @PostMapping("signurl")
    public Result signurl(@RequestBody QiYueSuoSeaLBean reqBean){
        if (reqBean == null){
            return ResultUtil.error("缺少必要的参数");
        }
        QiYueSuoResponse response = reportService.signurl(reqBean);
        if (response != null && response.getCode() == 0) {
            return ResultUtil.success("向契约锁发起报告签署url申请成功!");
        } else {
            return ResultUtil.error("向契约锁发起报告签署url申请失败："+response.getMessage());
        }
    }

    /**
     * 契约锁部门列表获取
     * @param tenantType
     * @param companyName
     * @return
     */
    @GetMapping("deptList")
    public Result deptList(String tenantType, String companyName){
        QiYueSuoResponse response = reportService.deptList(tenantType,companyName);
        if (response != null && response.getCode() == 0) {
            return ResultUtil.success(response);
        } else {
            return ResultUtil.error("获取公司在契约锁注册的部门列表失败："+response.getMessage());
        }
    }

    /**
     * 印章列表获取
     * @param category 印章类型PHYSICS("物理签章"),ELECTRONIC("电子签章"),不传默认查询电子章
     * @param companyName
     * @return
     */
    @GetMapping("sealListOfQys")
    public Result sealListOfQys(String category, String companyName,String sealType){
        if (StringUtils.isEmpty(companyName)){
            return ResultUtil.error("缺少必要的参数");
        }
        QiYueSuoResponse response = reportService.sealListOfQys(category,companyName,sealType);
        if (response != null && response.getCode() == 0) {
            return ResultUtil.success(response);
        } else {
            return ResultUtil.error("获取公司在契约锁的印章列表失败："+response.getMessage());
        }
    }

    /**
     * 契约锁报告下载
     * @param contractId
     * @param name
     * @param contact
     * @return
     */
    @GetMapping("downloadQysFile")
    public Result downloadQysFile(Long entrustId, Long contractId,String name,String contact,HttpServletResponse response){
        if (contractId == null || StringUtils.isEmpty(name) || StringUtils.isEmpty(contact)){
            return ResultUtil.error("缺少必要参数");
        }
        ReportRecordEntity bean = reportService.getDetailByEntrustId(entrustId);
        byte[] bytes = reportService.downloadQysFile(entrustId, contractId, name, contact);
        response.reset();
        response.setHeader("Access-Control-Expose-Headers","Content-Disposition");
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        String fileName = bean.getReportCode()+"（"+bean.getSampleName()+"）.zip";
        try {
            response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            outputStream.close();
            outputStream.close();
        }catch (Exception e){
            logger.error("下载契约锁报告文档失败:{}",e);
        }
        return null;
    }

    /**
     * 报告预览
     *
     * @param reportCode
     * @return
     */
    @GetMapping("preview")
    public Result preview(String reportCode, HttpServletResponse response) {
        if (StringUtils.isEmpty(reportCode)) {
            return ResultUtil.error("缺少必要参数！");
        }
        try {
            //根据报告模板url获取文件名
            ReportRecordEntity entity = reportService.getUrlByCode(reportCode);
            String reportName = "";
            String reportUrl = entity.getReportUrl();
            if (StringUtils.isNotEmpty(reportUrl)) {
                reportName = reportUrl.substring(reportUrl.lastIndexOf("/") + 1);
            }
            //查询报告详细信息
            List<ReportRecordDetailEntity> detailEntityList = reportService.getReportDetailByCode(reportCode);
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_report, reportName);
            //填充数据
            Long entrustId = reportService.getEntrustIdByCode(reportCode);
            EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
            String sealUrl = entity.getSealUrl();
            XWPFDocument document = reportService.preview(reportCode, detailEntityList, detail, object, sealUrl.split(","));
            //TODO pdf转换、设置盖章
            response.reset();
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            reportName = URLEncoder.encode(reportName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + reportName);
            OutputStream outputStream = response.getOutputStream();
            document.write(outputStream);
            outputStream.close();
        } catch (Exception ex) {
            log.info("报告预览失败：", ex.getMessage());
        }
        return null;
    }

    /**
     * 预览报告模板
     *
     * @param reportCode
     * @param response
     */
    @RequestMapping("previewTemplate")
    public void previewTemplate(String reportCode, HttpServletResponse response) {
        System.out.println("文件路径：" + reportCode);
        try {
            // 调用statObject()来判断对象是否存在。
            // 如果不存在, statObject()抛出异常,
            // 否则则代表对象存在。
            MinioClient client = MinIoUtil.minioClient;
            client.statObject("report-pdf", reportCode + ".pdf");
            // 获取"myobject"的输入流。
            InputStream in = client.getObject("report-pdf", reportCode + ".pdf");
            //使用response,将pdf文件以流的方式发送的前端浏览器上
            ServletOutputStream outputStream = response.getOutputStream();
            int i = IOUtils.copy(in, outputStream);   // copy流数据,i为字节数
            in.close();
            outputStream.close();
            System.out.println("流已关闭,可预览,该文件字节大小：" + i);
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询产品报告模板
     *
     * @param productId
     * @return
     */
    @GetMapping("/getTemplateListOld")
    public Result getTemplateListOld(String productId) {
        return ResultUtil.success("查询产品报告模板成功！", reportService.getReportTemplateListOld(productId));
    }

    @GetMapping("/getTemplateList")
    public Result getTemplateList(Long id) {
        return ResultUtil.success("查询产品报告模板成功！", reportService.getReportTemplateList(id));
    }

    /**
     * 下载报告
     *
     * @param id
     * @param code
     * @param response
     * @return
     */
    @GetMapping("downloadold")
    public String downloadold(Long id, String code, HttpServletResponse response) {
        //从文件服务器拉取文件
        MinioClient client = MinIoUtil.minioClient;
        String url = "";
        try {
            //先查询委托检测的类别：原材，配合比。
            //是原材的话，调用原材检测的报告生成方法。
            url = reportService.downLoad(client,code,id);
            //是配合比的话，调用配合比报告生成方法。
            //遍历检测项查出有多少报告
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 合并报告下载，支持多个报告模板，支持报告合并
     * @param reqBean
     * @return
     */
    @PostMapping("submitDownLoad")
    public Result submitDownLoad(@RequestBody ReqBean reqBean) {
        if (reqBean.getId() == null || CollectionUtil.isEmpty(reqBean.getList())){
            return null;
        }
        //从文件服务器拉取文件
        MinioClient client = MinIoUtil.minioClient;
        ReportResBean resBean = null;
        if ("原材检测".equals(reqBean.getType())){
            resBean = reportService.submitDownLoad(client, reqBean.getList(), reqBean.getId());
        }else {
            resBean = reportService.submitDownLoadMix(client, reqBean.getList(), reqBean.getId(),reqBean.getMixInfo());
        }

        return ResultUtil.success(resBean);
    }

    /**
     * 合并报告预览，支持多个报告模板，支持报告合并
     * @param json
     * @return
     */
    @RequestMapping("previewDownLoad")
    public void previewDownLoad(@RequestParam("json") String json, HttpServletResponse response) {
        String decode = "";
        String url = "";
        try {
            decode = URLDecoder.decode(json, "UTF-8");
        }catch (Exception e){

        }
        String unescapeJava = StringEscapeUtils.unescapeJava(decode);
        String substring = unescapeJava.substring(1, unescapeJava.length() - 1);
        ReqBean reqBean = JSON.parseObject(substring,ReqBean.class);
       /* if (reqBean.getId() == null || CollectionUtil.isEmpty(reqBean.getList())){
            return null;
        }*/
        //从文件服务器拉取文件
        MinioClient client = MinIoUtil.minioClient;
        ReportResBean resBean = null;
        if ("原材检测".equals(reqBean.getType())){
            resBean = reportService.submitDownLoad(client, reqBean.getList(), reqBean.getId());
        }else {
            resBean = reportService.submitDownLoadMix(client, reqBean.getList(), reqBean.getId(),reqBean.getMixInfo());
        }
        url = resBean.getUrl();
        //预览word转pdf
        String[] split = url.split("\\?");
        String[] strings = split[0].split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        XWPFDocument doc = null;
        try {
            client.statObject(bluckName, fileName);
            InputStream object = client.getObject(bluckName, fileName);
            doc = new XWPFDocument(object);
            //相应pdf
            ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(doc);
            InputStream inputStream = FileAndFolderUtil.parseOut(b1);
            //TODO 设置签名信息
            //设置提醒信息
            response.setCharacterEncoding("UTF-8");
            response.setHeader("alert",java.net.URLEncoder.encode(JSON.toJSONString(resBean.getMap()), "UTF-8"));
            ServletOutputStream outputStream = response.getOutputStream();
            int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
            inputStream.close();
            outputStream.close();
            url = MinIoUtil.upload("report-download", reqBean.getId() + ".pdf", inputStream, "application/octet-stream");
        }catch (Exception e){
            logger.error("预览合并后的报告异常:{}",e);
        }
    }

    @GetMapping("download")
    public String downReport(Long id) {
        //从文件服务器拉取文件
        MinioClient client = MinIoUtil.minioClient;
        String url = "";
        try {
            //先查询委托检测的类别：原材，配合比。
            //是原材的话，调用原材检测的报告生成方法。
            url = reportService.downLoad2(client,id);
            //是配合比的话，调用配合比报告生成方法。
            //遍历检测项查出有多少报告
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //根据委托单ID查询检测项的所属报告
        return url;
    }

    /**
     * 报告邮寄
     * 待邮寄报告列表及已发出报告历史列表查询
     *
     * @param search
     * @param reportType
     * @return
     */
    @GetMapping("sendList")
    public Result sendList(String search, String reportType, Integer pageNum, Integer pageSize, String type) {
        logger.info("分页参数pageNum:{},pageSize:{}", pageNum, pageSize);
        PageInfo pageInfo = reportService.getSendList(search, reportType, pageNum, pageSize, type);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 是否可以审批
     *
     * @param id
     * @return
     */
    @GetMapping("/isApprove")
    public Result isApprove(Long id) {
        return ResultUtil.success(reportService.isApprove(id));
    }

    /**
     * 测试
     *
     * @param entrustId
     */
    @GetMapping("testPdf")
    public void testPdf(Long entrustId) {
        String fileName = "BD20210021.docx";
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_entrust_template, fileName);
            //填充数据
            EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
            XWPFDocument document = entrustService.downloadEntrust(detail, object);
            FileAndFolderUtil.convertDocxToPdf(document, "D:/VPS/11.pdf");
        } catch (Exception e) {
            logger.error("转换失败:{}", e);
        }
    }

    /**
     * 测试
     */
    @GetMapping("test")
    public void test() {
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_entrust_template, "BGLQ21001F.docx");
            XWPFDocument document = new XWPFDocument(object);
            AsposeUtil.word2pdf(document, "D:\\VPS\\22.pdf");
        } catch (Exception e) {
            logger.error("转换失败:{}", e);
        }
    }

    /**
     * 客户上传报告
     * @param reportCode
     * @param verifyer
     * @param issuer
     * @param file
     * @return
     */
    @PostMapping(value = "uploadReport")
    public Result uploadReport(@RequestParam("reportCode") String reportCode,@RequestParam("verifyer") String verifyer,
                               @RequestParam("issuer") String issuer, @RequestParam(required = false,name = "file") MultipartFile file,
                               @RequestParam("code") String code,@RequestParam("conclusion") String conclusion
            ,@RequestParam("additional") String additional,@RequestParam("mixInfo") String mixInfo,@RequestParam("type") String type) {
        if (StringUtils.isEmpty(reportCode) || StringUtils.isEmpty(verifyer) || StringUtils.isEmpty(issuer)){
            return ResultUtil.error("缺少参数！");
        }
        Boolean flag = reportService.uploadReport(reportCode,file,verifyer.split("&")[0],issuer.split("&")[0]
                ,Long.parseLong(verifyer.split("&")[1]),Long.parseLong(issuer.split("&")[1]),code,conclusion,additional,mixInfo,type);
        if (flag) {
            return ResultUtil.success("报告文件上传成功！");
        }else {
            return ResultUtil.error("报告文件上传失败！");
        }
    }

    /**
     * 获取委托单下各个报告模板所需的检测结论和附加声明的文案
     * @param entrustId
     * @return
     */
    @GetMapping("getResult")
    public Result getResut(Long entrustId){
        if (entrustId == null){
            return ResultUtil.error("缺少必要的参数");
        }
        List<ConclusionEntity> list = reportService.getResut(entrustId);
        return ResultUtil.success(list);
    }

    /**
     * 查询配合比检测信息
     * @param entrustId
     * @return
     */
    @GetMapping("getMixSampleInfo")
    public Result getMixSampleInfo(Long entrustId){
        if (entrustId == null){
            return ResultUtil.error("缺少必要的参数");
        }
        TestSampleMixInfoEntity mixSampleInfo = reportService.getMixSampleInfo(entrustId);
        if(mixSampleInfo == null){
            return ResultUtil.error("未找到相关配合比检测信息！");
        }else{
            return ResultUtil.success("查询配合比检测信息成功！",mixSampleInfo);
        }
    }

    /**
     * 下载报告获取url链接
     * @param entrustId
     * @return
     */
    @GetMapping("reportUrl")
    public void reportUrl(Long entrustId,HttpServletResponse response){
        String reportUrl = reportService.reportUrl(entrustId);
        MinioClient client = MinIoUtil.minioClient;
        //预览word转pdf
        String[] split = reportUrl.split("\\?");
        String[] strings = split[0].split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        try {
            client.statObject(bluckName, fileName);
            InputStream inputStream = client.getObject(bluckName, fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
            inputStream.close();
            outputStream.close();
        }catch (Exception e){
            logger.error("预览合并后的报告异常:{}",e);
        }
    }

    @GetMapping("testInsert")
    public void test(String url,Long entrustId) {
        try {
            String s = reportService.insertPicToPdf(url, entrustId);
            System.out.println("============="+s);
        }catch (Exception e){
            logger.error("===");
        }

    }
}

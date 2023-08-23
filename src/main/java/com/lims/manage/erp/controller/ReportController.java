package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.AlertEntity;
import com.lims.manage.erp.entity.ConclusionEntity;
import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.QiYueSuoSeaLBean;
import com.lims.manage.erp.entity.QrCodeAuthRes;
import com.lims.manage.erp.entity.ReportEditReq;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportResBean;
import com.lims.manage.erp.entity.ReqBean;
import com.lims.manage.erp.entity.SealEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestSampleMixInfoEntity;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.AlertService;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.DownloadUtils;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.PDFHelper3;
import com.lims.manage.erp.util.RedisUtil;
import com.lims.manage.erp.util.RedisUtils;
import com.lims.manage.erp.util.ReturnResponse;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.ReportDetailListParamVo;
import com.lims.manage.erp.vo.ReportDetailListVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import com.lims.manage.erp.vo.TeamVo;
import com.zhuozhengsoft.pageoffice.FileSaver;
import com.zhuozhengsoft.pageoffice.OpenModeType;
import com.zhuozhengsoft.pageoffice.PageOfficeCtrl;
import com.zhuozhengsoft.pageoffice.excelwriter.Sheet;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

//import com.lims.manage.erp.mapper.ReportRecordEntityMapper;

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
    @Autowired
    private AlertService alertService;
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;
    @Autowired
    private TaskService taskService;
    @Autowired
    private DownloadUtils downLoad;
    @Autowired
    private RedisUtil redisUtil;
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
    public Result getlist_history_details(Long id,Long taskId) {
        return ResultUtil.success("获取历史任务单详情成功！", reportService.getReportList_history_details(id,taskId));
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
        return ResultUtil.success("查询任务单详情成功！", reportService.getReportDetail0620(taskId));
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

    @PostMapping("/test")
    public Result test(Long id) {
        String max = reportService.max(id);
        return ResultUtil.success("保存成功！", max);
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
    public Result sealList(String search, Integer pageNum, Integer pageSize, String reportType,String state,Integer reportTypeStatus) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少必要的参数！");
        }
        PageInfo pageInfo = reportService.sealList(search, pageNum, pageSize, reportType,state,reportTypeStatus);
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
        // 根据报告id 获取报告类型
        Integer type = null;
        // 调用方法 获取 报告类型
        type = reportService.getReturnReportType(reportRecordEntity.getId());
        if(type == null){
            return ResultUtil.error("编辑报告信息失败! 报告单id不存在");
        }
        reportRecordEntity.setType(String.valueOf(type));
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
//    @GetMapping("sealApprove")
//    public Result seal(Long entrustId,String title,String fileType,String reportType) {
//        if (entrustId == null) {
//            return ResultUtil.error("缺少必要的参数！");
//        }
//        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(fileType)){
//            return ResultUtil.error("缺少合同发起参数！");
//        }
//        Boolean flag = reportService.seal(entrustId,title,fileType,reportType);
//        if (flag) {
//            return ResultUtil.success("向契约锁发起盖章合同申请成功!");
//        } else {
//            return ResultUtil.error("向契约锁发起盖章合同申请失败！");
//        }
//    }

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
        if (CollectionUtils.isEmpty(reqBean.getList())){
            return ResultUtil.error("请选择需要签署的报告");
        }
        List<Long> longs = reqBean.getList();
        List<String> stringList = reportService.getCodeByIds(longs);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<stringList.size();i++) {
            stringBuilder.append(stringList.get(i));
            if (i==stringList.size()-1){
                continue;
            }else {
                stringBuilder.append(",");
            }
        }
        reqBean.setSubject(stringBuilder.toString());
        QiYueSuoResponse response = reportService.createbycategoryBatch(reqBean,stringList);
        if (response != null && response.getCode() == 0) {
//            if (response.getContractId() != null){
//                log.info("开始模拟处理回调业务");
//                reportService.callback(response.getContractId());
//                log.info("回调业务处理完成");
//            }
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
//    @PostMapping("signurl")
//    public Result signurl(@RequestBody QiYueSuoSeaLBean reqBean){
//        if (reqBean == null){
//            return ResultUtil.error("缺少必要的参数");
//        }
//        QiYueSuoResponse response = reportService.signurl(reqBean);
//        if (response != null && response.getCode() == 0) {
//            return ResultUtil.success("向契约锁发起报告签署url申请成功!");
//        } else {
//            return ResultUtil.error("向契约锁发起报告签署url申请失败："+response.getMessage());
//        }
//    }

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
     * 根据报告编号获取签署链接
     * @param reportCode
     * @return
     */
    @GetMapping("getSignUrl")
    public Result getSignUrl(String reportCode){
        if (StringUtils.isEmpty(reportCode)){
            return ResultUtil.error("缺少参数");
        }
        ReportRecordEntity urlByCode = reportService.getUrlByCode(reportCode);
        return ResultUtil.success(urlByCode.getSealUrl());
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
        //TODO 兼容中间报告
        if (bean == null){
            bean = reportService.getDetailByEntrustIdZj(entrustId);
        }
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
    public Result getTemplateList(Long id,Long recordId) {
        return ResultUtil.success("查询产品报告模板成功！", reportService.getReportTemplateList0706(id,recordId));
    }

    /**
     * 合并报告下载，支持多个报告模板，支持报告合并
     * @param reqBean
     * @return
     */
    @PostMapping("submitDownLoad")
    public void submitDownLoad(@RequestBody ReqBean reqBean,HttpServletResponse response) {
        if (reqBean.getId() == null || CollectionUtil.isEmpty(reqBean.getList())){
            return ;
        }
        //从文件服务器拉取文件
        MinioClient client = MinIoUtil.minioClient;
        ReportResBean resBean = null;
        if ("原材检测".equals(reqBean.getType())){
            resBean = reportService.submitDownLoad(client, reqBean.getList(), reqBean.getId(),reqBean.getReportType());
        }else {
            resBean = reportService.submitDownLoadMix(client, reqBean.getList(), reqBean.getId(),reqBean.getMixInfo(),reqBean.getReportType());
        }
        //保存告警信息
        List<AlertEntity> list = Lists.newArrayList();
        Map<String, String> map = resBean.getMap();
        Set<String> set = map.keySet();
        for (String s:set) {
            AlertEntity entity = new AlertEntity();
            entity.setId(GenID.getID());
            entity.setCheckItemName(s);
            entity.setDescrib(map.get(s));
            entity.setEntrustId(reqBean.getId());
            list.add(entity);
        }
        if (CollectionUtils.isNotEmpty(list)){
            alertService.deleteByEntrustId(reqBean.getId());
            alertService.saveBatch(list);
        }
        String url = resBean.getUrl();
        String[] split = url.split("\\?");
        String[] strings = split[0].split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        InputStream fileStream = MinIoUtil.getFileStream(bluckName, fileName);
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/x-msdownload");
        response.setCharacterEncoding("UTF-8");
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(fileStream,outputStream);
            fileStream.close();
            outputStream.close();
        }catch (Exception e){
            logger.error("合并下载报告异常:{}",e);
        }

    }

    /**
     * 合并报告预览
     * @param json
     * @param response
     */
    @RequestMapping("previewDownLoad")
    public void previewDownLoad(@RequestParam("json") String json, HttpServletResponse response) {
        //json = "{\"id\":1658287642955105,\"list\":[{\"url\":\"http://121.89.242.0:9000/file-resources/1653308619762103.docx?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20220523%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20220523T122339Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=36508788f305c2f692143d7467915267abd08f6e340a2838e869d737dae0b361\",\"conclusion\":\"经检测，该土样品,回弹模量均符合JTG 3430-2020中的技术要求。\",\"additional\":\"1.委托人：一禅；2.见证单位：甘肃华路捷公路工程技术咨询有限公司；3.见证人：张瑞涛；4.委托方提供：无 ；\"},{\"url\":\"http://121.89.242.0:9000/file-resources/1649645324684109.docx?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20220411%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20220411T024844Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=621bfddf70c4e5bb952335fb438e58223f8d47677bb38b0c932c602071685008\",\"conclusion\":\"经检测，该土样品,粗粒土和巨粒土最大干密度均符合JTG 3430-2020中的技术要求。\",\"additional\":\"1.委托人：一禅；2.见证单位：甘肃华路捷公路工程技术咨询有限公司；3.见证人：张瑞涛；4.委托方提供：无 ；\"}],\"type\":\"原材检测\",\"mixInfo\":{}}";
        String decode = "";
        String url = "";
        try {
            json = json.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            json = json.replaceAll("\\+", "%2B");
            decode = URLDecoder.decode(json, "UTF-8");
        }catch (Exception e){
            logger.error("处理json参数转码错误:{}",e);
        }
        String unescapeJava = StringEscapeUtils.unescapeJava(decode);
        String substring = unescapeJava.substring(1, unescapeJava.length() - 1);
        ReqBean reqBean = JSON.parseObject(substring,ReqBean.class);
        //从文件服务器拉取文件
        MinioClient client = MinIoUtil.minioClient;
        ReportResBean resBean = null;
        if ("原材检测".equals(reqBean.getType())){
            resBean = reportService.submitDownLoad(client, reqBean.getList(), reqBean.getId(),reqBean.getReportType());
        }else {
            resBean = reportService.submitDownLoadMix(client, reqBean.getList(), reqBean.getId(),reqBean.getMixInfo(),reqBean.getReportType());
        }
        url = resBean.getUrl();
        //预览excel转pdf
        String[] split = url.split("\\?");
        String[] strings = split[0].split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        try {
            String path = qiYueSuoEntity.getAutographPath()+GenID.getID()+".pdf";
            client.statObject(bluckName, fileName);
            InputStream object = client.getObject(bluckName, fileName);
            //相应pdf
            ByteArrayOutputStream b1 = PDFHelper3.excel2pdf2(object,path);
            InputStream inputStream = FileAndFolderUtil.parseOut(b1);
            //TODO 设置签名信息
            //设置提醒信息
            //保存告警信息
            List<AlertEntity> list = Lists.newArrayList();
            Map<String, String> map = resBean.getMap();
            Set<String> set = map.keySet();
            for (String s:set) {
                AlertEntity entity = new AlertEntity();
                entity.setId(GenID.getID());
                entity.setCheckItemName(s);
                entity.setDescrib(map.get(s));
                entity.setEntrustId(reqBean.getId());
                list.add(entity);
            }
            if (CollectionUtils.isNotEmpty(list)){
                alertService.deleteByEntrustId(reqBean.getId());
                alertService.saveBatch(list);
            }
            ServletOutputStream outputStream = response.getOutputStream();
            int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
            inputStream.close();
            outputStream.close();
            //TODO 上传是否保留？
            url = MinIoUtil.upload("report-download", reqBean.getId() + ".pdf", inputStream, "application/octet-stream");
        }catch (Exception e){
            logger.error("预览合并后的报告异常:{}",e);
        }
    }

    /**
     * 查询报告邮寄列表--0623
     * @param search
     * @param reportType
     * @param pageNum
     * @param pageSize
     * @param type
     * @param category
     * @param reportTypeStatus
     * @return
     */
    @GetMapping("sendList")
    public Result sendList(String search, String reportType, Integer pageNum, Integer pageSize, String type,String category,Integer reportTypeStatus) {
        logger.info("分页参数pageNum:{},pageSize:{}", pageNum, pageSize);
        PageInfo pageInfo = reportService.getSendList0623(search, reportType, pageNum, pageSize, type,category,reportTypeStatus);
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
     * 报告合成提交审批保存
     * @param reportCode
     * @param verifyer
     * @param issuer
     * @param file
     * @return
     */
    @PostMapping(value = "uploadReport")
    public Result uploadReport(@RequestParam("reportCode") String reportCode,@RequestParam("inspector") String inspector,@RequestParam("verifyer") String verifyer,
                               @RequestParam("issuer") String issuer, @RequestParam(required = false,name = "file") MultipartFile file,
                               @RequestParam("code") String code,@RequestParam("conclusion") String conclusion
            ,@RequestParam("additional") String additional,@RequestParam("mixInfo") String mixInfo,@RequestParam("type") String type,@RequestParam("reportType") String reportType) {

        logger.debug("发起审批检测人:{},审核人:{},签发人:{}",inspector,verifyer,issuer);
        if (StringUtils.isEmpty(reportCode) || StringUtils.isEmpty(verifyer) || StringUtils.isEmpty(issuer) || org.apache.commons.lang3.StringUtils.isEmpty(inspector)){
            return ResultUtil.error("缺少参数！");
        }
        Boolean flag = reportService.uploadReport(reportCode,file,verifyer.split("&")[0],issuer.split("&")[0]
                ,Long.parseLong(verifyer.split("&")[1]),Long.parseLong(issuer.split("&")[1]),code,conclusion,additional,mixInfo,type,inspector,reportType);
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
    public Result getResut(Long entrustId,Integer reportType){
        if (entrustId == null){
            return ResultUtil.error("缺少必要的参数");
        }
        List<ConclusionEntity> list = reportService.getResut(entrustId,reportType);
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
    public String reportUrl(Long entrustId){

        return reportService.reportUrl(entrustId);
    }

    /**
     * 预览报告
     * @param id
     * @return
     */
    @GetMapping("preReportUrl")
    public void preReportUrl(Long id,HttpServletResponse response){
        String reportUrl = reportService.getUrlById(id);
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

    @PostMapping("reportList")
    public Result reportList(@RequestBody ReportDetailListParamVo paramVo) {
        if (paramVo.getPageNum() == null || paramVo.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        logger.info("分页参数pageNum:{},pageSize:{}", paramVo.getPageNum(), paramVo.getPageSize());
        PageInfo pageInfo = reportService.reportList(paramVo);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 导出报告查询
     * @param paramVo
     * @return
     */
    @PostMapping("exportReportList")
    public void exportReportList(@RequestBody ReportDetailListParamVo paramVo, HttpServletResponse response) {
        paramVo.setPageNum(null);
        paramVo.setPageSize(null);
        List<ReportDetailListVo> list = reportService.reportList(paramVo).getList();
        OutputStream outputStream = null;
        try {
            response.reset();
            response.setContentType("application/msword");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" +  java.net.URLEncoder.encode("检验检测报告发放登记表"+".doc", "UTF-8") );
            outputStream = reportService.exportReportList(list, response);
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            log.error("检验检测报告发放登记表导出失败:{}",e);
        }
    }

    /**
     * 中间报告列表
     * @param pageNum
     * @param pageSize
     * @param search
     * @return
     */
    @GetMapping("/middleReportList")
    public Result getSampleListmiddleReportList(Integer pageNum,Integer pageSize,Integer state,String search) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success("获取可制作中间报告列表成功！", reportService.middleReportList(pageNum,pageSize,state,search));
    }

    /**
     * 中间报告制作
     * @param taskId
     * @return
     */
    @GetMapping("/middleReportDetail")
    public Result middleReportDetail(Integer taskFlowId,Long taskId) {
        if(taskId == null || taskFlowId == null){
            return ResultUtil.error("缺少必要的参数!");
        }
        return ResultUtil.success("查询中间报告详情成功！", reportService.getMiddleReportDetail(taskFlowId,taskId));
    }

    @PostMapping("/middleReportPreserve")
    public Result middleReportPreserve(@RequestBody ReportPreserveVo vo) {
        Boolean preserve = reportService.middleReportPreserve(vo);
        if (preserve) {
            return ResultUtil.success("保存成功！", preserve);
        } else {
            return ResultUtil.error(ResultEnum.PRESERVE_FAIL.getCode(), ResultEnum.PRESERVE_FAIL.getMsg());
        }
    }

    /**
     * 中间报告历史修改详情接口
     * @param taskFlowId
     * @param taskId
     * @param recordId
     * @return
     */
    @GetMapping("/middleReportEdit")
    public Result middleReportEdit(Integer taskFlowId,Long taskId,Long recordId) {
        if(taskId == null || taskFlowId == null || recordId == null){
            return ResultUtil.error("缺少必要的参数!");
        }
        return ResultUtil.success("查询中间报告详情成功！", reportService.middleReportEdit(taskFlowId,taskId,recordId));
    }

    /**
     * 中间报告修改保存接口
     * @param vo
     * @return
     */
    @PostMapping("/middleReportUpdate")
    public Result middleReportUpdate(@RequestBody ReportPreserveVo vo) {
        Boolean preserve = reportService.middleReportUpdate(vo);
        if (preserve) {
            return ResultUtil.success("修改成功！", preserve);
        } else {
            return ResultUtil.error(ResultEnum.PRESERVE_FAIL.getCode(), ResultEnum.PRESERVE_FAIL.getMsg());
        }
    }

    /**
     * 设置物理用章
     * @param sealEntity
     * @return
     */
    @PostMapping("category")
    public Result category(@RequestBody SealEntity sealEntity){
        if (CollectionUtils.isEmpty(sealEntity.getId())){
            return ResultUtil.error("请选择需要操作的数据");
        }
        Boolean flag = reportService.category(sealEntity);
        if (flag){
            return ResultUtil.success("操作成功！");
        }else {
            return ResultUtil.error("网络异常");
        }
    }

    @GetMapping("/withdrewReport")
    public Result withdrewReport(Long recordId,Long taskId) {
        if(taskId == null || recordId == null){
            return ResultUtil.error("缺少必要的参数!");
        }
        Boolean aBoolean = reportService.withdrawReport(recordId, taskId);
        if(aBoolean){
            return ResultUtil.success("撤回报告成功！",aBoolean);
        }else{
            return ResultUtil.error("报告已发起审批，撤回报告失败!");
        }
    }

    /**
     * 查询盖章列表
     * @param search
     * @param reportType
     * @param sealType
     * @return
     */
    @GetMapping("historyList")
    public Result historyList(String search, String reportType, String sealType,Integer pageNum,Integer pageSize,Long startDate,Long endDate){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数");
        }
        if (startDate != null){
            startDate = DateUtil.getDayStartMs(startDate);
        }
        if (endDate != null){
            endDate = DateUtil.getDayEndMs(endDate);
        }
        PageInfo<ReportRecordEntity> pageInfo = reportService.historyList(search,reportType,sealType,pageNum,pageSize,startDate,endDate);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 导出数据
     * @param reportCode
     * @param reportType
     * @param sealType
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("exportRecords")
    public void exportRecords(String reportCode, String reportType, String sealType,String startDate,String endDate,HttpServletResponse response){
        if ("null".equals(reportType)){
            reportType = null;
        }
        if ("null".equals(sealType)){
            sealType = null;
        }
        if ("null".equals(startDate)){
            startDate = null;
        }
        if ("null".equals(endDate)){
            endDate = null;
        }
        Long s = null;
        Long e = null;
        if (startDate != null){
            s = Long.parseLong(startDate);
            s = DateUtil.getDayStartMs(s);
        }
        if (endDate != null){
            e = Long.parseLong(endDate);
            e = DateUtil.getDayEndMs(e);
        }
        String time1 = "";
        String time2 = "";
        String desc = "";
        if (s != null){
            time1 = DateUtil.getDayString(s);
        }
        if (e != null){
            time2 = DateUtil.getDayString(e);
        }
        if (StringUtils.isNotEmpty(time1)){
            if (time1.equals(time2)){
                desc = "("+time1+"）";
            }else {
                desc = "("+time1 + "~" + time2+"）";
            }
        }
        byte[] bytes = reportService.exportRecords(reportCode, reportType, sealType, s, e);
        //响应数据流
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.reset();
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" +  java.net.URLEncoder.encode("报告盖章登记表"+ desc+".xlsx", "UTF-8") );
            outputStream.write(bytes);
        }catch (Exception e1){
            logger.error("导出盖章历史失败:{}",e1);
        }

    }

    /**
     * 获取报告盖章人员
     * @return
     */
    @GetMapping("getSealer")
    public Result getSealer(){
        List<TestTeam> list = reportService.getSealer();
        return ResultUtil.success(list);
    }

    /**
     * 报告合并获取检测人列表
     * @param search
     * @return
     */
    @GetMapping("inspectorList")
    public Result inspectorList(String search){
        List<String> list = reportService.inspectorList(search);
        return ResultUtil.success(list);
    }

    /**
     * 知识管理模块下：报告模板、原始记录模板下载
     * @param url
     * @param response
     */
    @GetMapping("templateDownLoad")
    public void templateDownLoad(String url,HttpServletResponse response){
        if (org.apache.commons.lang3.StringUtils.isEmpty(url)){
            return;
        }
        String[] strings = url.split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        InputStream fileStream = MinIoUtil.getFileStream(bluckName, fileName);
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/x-msdownload");
        response.setCharacterEncoding("UTF-8");
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(fileStream,outputStream);
            fileStream.close();
            outputStream.close();
        }catch (Exception e){
            logger.error("合并下载报告异常:{}",e);
        }
    }

    /**
     * 报告在线制作
     * @param map
     * @param request
     * @return
     */
    @GetMapping("onlineEdit")
    public ModelAndView onlineEdit(Long taskId,Integer reportType,Integer sampleId,
                                   Map<String, Object> map, HttpServletRequest request){
        //json="{ \"reportComplete\": \"1\",   \"taskId\": \"4595967135304210\",   \"taskFlowId\": \"\",   \"reportType\": \"0\",   \"sampleId\": \"15288\" }";
        if (taskId==null || reportType==null || sampleId==null){
            return new ModelAndView("error");
        }
        ReportEditReq reportEditReq = new ReportEditReq();
        reportEditReq.setTaskId(taskId);
        reportEditReq.setReportType(reportType);
        reportEditReq.setSampleId(sampleId);
        //根据参数委托相关信息
        Long entrustId = taskService.getEntrustIdByTaskId(reportEditReq.getTaskId());
        reportEditReq.setEntrustId(entrustId);
        EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
        String localPath = reportService.handlerReportMessage(detail,reportEditReq,qiYueSuoEntity.getAutographPath());
        //设置服务页面
        PageOfficeCtrl poCtrl = new PageOfficeCtrl(request);
        poCtrl.setServerPage(request.getContextPath() + "/poserver.zz");
        //禁止拷贝文档内容到外部
        poCtrl.setDisableCopyOnly(true);
        //poCtrl.setCustomToolbar(false);
        com.zhuozhengsoft.pageoffice.excelwriter.Workbook wb = new com.zhuozhengsoft.pageoffice.excelwriter.Workbook();
        //解除excel隐藏sheet指定需要隐藏sheet
        try {
            Workbook workbook = new Workbook(localPath);
            int count = workbook.getWorksheets().getCount();
            for (int i=0; i<count; i++){
                String name = workbook.getWorksheets().get(i).getName();
                name = name.replaceAll(" ", "");
                if ("报告第1页,报告第2页,报告第3页,指标选择".contains(name)){
                    workbook.getWorksheets().get(i).setVisible(true);
                    //设置当工作表只读时，是否允许用户手动调整行列。
                    wb.openSheet(workbook.getWorksheets().get(i).getName()).setAllowAdjustRC(true);
                    //如果值为true，处于可编辑的Sheet将变成只读。如果值为false，处于只读的Sheet将变成可编辑。
                    wb.openSheet(workbook.getWorksheets().get(i).getName()).setReadOnly(false);
                }else {
                    workbook.getWorksheets().get(i).setVisible(false);
                }
            }
            workbook.save(localPath, SaveFormat.XLSX);
        } catch (Exception e) {
            logger.error("加载需要编辑的报告文件失败:{}",e);
        }
        poCtrl.setWriter(wb);
        //添加自定义按钮
        poCtrl.addCustomToolButton("保存", "Save", 1);
        poCtrl.addCustomToolButton("打印", "PrintFile()", 6);
        poCtrl.addCustomToolButton("全屏/还原", "IsFullScreen()", 4);
        poCtrl.addCustomToolButton("关闭", "CloseFile()", 21);
        //设置操作栏按钮
        poCtrl.getRibbonBar().setTabVisible("TabHome", true);//开始
        poCtrl.getRibbonBar().setTabVisible("TabFormulas", false);//公式
        poCtrl.getRibbonBar().setTabVisible("TabInsert", false);//插入
        poCtrl.getRibbonBar().setTabVisible("TabData", false);//数据
        poCtrl.getRibbonBar().setTabVisible("TabReview", false);//审阅
        poCtrl.getRibbonBar().setTabVisible("TabView", false);//视图
        //设置处理文件保存的请求方法
        poCtrl.setSaveFilePage("saveOnlineReport");
        logger.info("处理前路径:{}",localPath);
        //加载文档
        if (localPath.indexOf(":\\") < 0){
            localPath = "file://"+localPath;
        }
        logger.info("处理后路径:{}",localPath);
        poCtrl.webOpen(localPath, OpenModeType.xlsSubmitForm, "user");
        //删除临时文件
        FileAndFolderUtil.delete(localPath);
        map.put("pageoffice", poCtrl.getHtmlCode("PageOfficeCtrl1"));
        map.put("params",JSON.toJSONString(reportEditReq));
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(reportEditReq));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("entrustId"+"="+jsonObject.get("entrustId"));
        stringBuilder.append(",");
        stringBuilder.append("reportType"+"="+jsonObject.get("reportType"));
        stringBuilder.append(",");
        stringBuilder.append("sampleId"+"="+jsonObject.get("sampleId"));
        stringBuilder.append(",");
        stringBuilder.append("taskId"+"="+jsonObject.get("taskId"));
        map.put("params1",stringBuilder.toString());
        //设置模板引擎的html模板
        ModelAndView mv = new ModelAndView("excel");
        return mv;
    }

    /**
     * 报告在线编辑提交
     * @param bean
     * @return
     */
    @PostMapping("submitEditReport")
    public Result submitEditReport(@RequestBody ReportEditReq bean){
        if (bean.getTaskId() == null || bean.getReportType()==null || CollectionUtils.isEmpty(bean.getSampleIds())){
            return ResultUtil.error("缺少参数");
        }
        Boolean flag = reportService.submitEditReport(bean);
        if (flag){
            return ResultUtil.success("提交成功");
        }else {
            return ResultUtil.error("提交失败，请检查该委托是否存在未完成的中间报告");
        }
    }


    /**
     * 报告制作保存
     * @param request
     * @param response
     */
    @RequestMapping("saveOnlineReport")
    public ModelAndView saveOnlineReport(HttpServletRequest request, HttpServletResponse response){
        //获取文件
        FileSaver fs = new FileSaver(request, response);
        String json = fs.getFormField("params");
        if (org.apache.commons.lang.StringUtils.isEmpty(json)){
            return new ModelAndView("error");
        }
        ReportEditReq reportEditReq = JSON.parseObject(json,ReportEditReq.class);
        Long entrustId = taskService.getEntrustIdByTaskId(reportEditReq.getTaskId());
        reportEditReq.setEntrustId(entrustId);
        if (reportEditReq.getTaskId() == null || reportEditReq.getReportType() == null){
            return new ModelAndView("error");
        }
        FileInputStream fileStream = fs.getFileStream();
        fs.close();
        Boolean flag = reportService.saveOnlineReport(fileStream,reportEditReq);
        if (flag){
            return new ModelAndView("success");
        }else {
            return new ModelAndView("error");
        }
    }

    /**
     * 线下报告合并
     * @param reportCode
     * @param inspector
     * @param verifyer
     * @param issuer
     * @param file
     * @return
     */
    @PostMapping(value = "offlineReportMerge")
    public Result offlineReportMerge(@RequestParam("reportCode") String reportCode,@RequestParam("inspector") String inspector,@RequestParam("verifyer") String verifyer,
                               @RequestParam("issuer") String issuer, @RequestParam(required = false,name = "file") MultipartFile file
            ,@RequestParam("reportCompleteTime") String reportCompleteTime, @RequestParam("time") String requestDate
            , @RequestParam("sampleName") String sampleName,@Param("taskId") Long taskId,@Param("taskCode") String taskCode) {
        if (reportCompleteTime == null || file == null || StringUtils.isEmpty(inspector) || StringUtils.isEmpty(verifyer) || StringUtils.isEmpty(issuer)){
            return ResultUtil.error("缺少参数");
        }
        logger.debug("发起审批检测人:{},审核人:{},签发人:{}",inspector,verifyer,issuer);
        if (StringUtils.isEmpty(reportCode) || StringUtils.isEmpty(verifyer) || StringUtils.isEmpty(issuer) || org.apache.commons.lang3.StringUtils.isEmpty(inspector)){
            return ResultUtil.error("缺少参数！");
        }
        Boolean flag = reportService.offlineReportMerge(reportCode,file,verifyer.split(",")[0],issuer.split(",")[0]
                ,Long.parseLong(verifyer.split(",")[1]),Long.parseLong(issuer.split(",")[1]),inspector);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        Date date1 = null;
        try {
            if (reportCompleteTime.length()>10){
                reportCompleteTime = reportCompleteTime.substring(0,10);
            }
            date = format.parse(reportCompleteTime);
            date1 = format.parse(requestDate);
        } catch (ParseException e) {
            logger.error("时间格式转换错误:{}",e);
        }
        if (flag) {
            //更新报告上盖章的时间
            Date date2 = new Date(System.currentTimeMillis());
            reportService.updateTime(reportCode,date,date1,sampleName,taskId,taskCode,date2);
            return ResultUtil.success("报告文件上传成功！");
        }else {
            return ResultUtil.error("报告文件上传失败！");
        }
    }

    /**
     * 在线报告合并
     * @param reportCode
     * @return
     */
    @GetMapping("/onlineReportMerge")
    public void onlineReportMerge(String reportCode,HttpServletResponse response){
        if (StringUtils.isEmpty(reportCode)){
            return ;
        }
        //根据报告编号合并委托下所用样品的报告模板包含首页、编辑报告页码和填充报告编号
        String url = reportService.handlerReportMerge(reportCode,qiYueSuoEntity.getAutographPath());
        MinioClient client = MinIoUtil.minioClient;
        String[] strings = url.split("\\/");
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

    /**
     * 保存在线合并报告
     * @param reportCode
     * @param inspector
     * @param verifyer
     * @param issuer
     * @return
     */
    @RequestMapping("onlineReportMergeSave")
    public Result onlineReportMergeSave(String reportCode,String inspector,String verifyer, String issuer,
                                        String reportCompleteTime, String requestDate, String sampleName
            ,Long taskId, String taskCode){
        if (StringUtils.isEmpty(inspector) || StringUtils.isEmpty(verifyer) || StringUtils.isEmpty(issuer)
                ||StringUtils.isEmpty(sampleName) ||StringUtils.isEmpty(requestDate)){
            return ResultUtil.error("缺少参数");
        }
        logger.debug("发起审批检测人:{},审核人:{},签发人:{}",inspector,verifyer,issuer);
        Boolean flag = reportService.onlineReportMergeSave(reportCode,verifyer.split(",")[0],issuer.split(",")[0]
                ,Long.parseLong(verifyer.split(",")[1]),Long.parseLong(issuer.split(",")[1]),inspector);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        Date date1 = null;
        try {
            if (reportCompleteTime.length()>10){
                reportCompleteTime = reportCompleteTime.substring(0,10);
            }
            date = format.parse(reportCompleteTime);
            date1 = format.parse(requestDate);
        } catch (ParseException e) {
            logger.error("时间格式转换错误:{}",e);
        }
        if (flag) {
            Date date2 = new Date(System.currentTimeMillis());
            reportService.updateTime(reportCode,date,date1,sampleName,taskId,taskCode,date2);
            return ResultUtil.success("报告文件上传成功！");
        }else {
            return ResultUtil.error("报告文件上传失败！");
        }
    }

    /**
     * 在线报告制作列表
     * @param pageNum
     * @param pageSize
     * @param search
     * @return
     */
    @GetMapping("/onlineReportList")
    public Result onlineReportList(Integer pageNum,Integer pageSize,String search) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        return ResultUtil.success("获取在线制作报告任务单列表成功！", reportService.onlineMakeReport(pageNum,pageSize,search));
    }

    /**
     * 查询任务单下可制作报告样品列表
     * @param entrustId
     * @param taskId
     * @return
     */
    @GetMapping("/getMakeReportSampleInfos")
    public Result getMakeReportSampleInfos(Long entrustId,Long taskId) {
        if (entrustId == null || taskId == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        return ResultUtil.success("查询可制作报告样品列表成功！", reportService.makeReportSampleInfos(entrustId,taskId));
    }

    /**
     * 根据报告编号获取委托下最后一个检测项的完成时间
     * @param reportCode
     * @return
     */
    @GetMapping("getReportCompleteTime")
    public Result getReportCompleteTime(String reportCode){
        if (StringUtils.isEmpty(reportCode)){
            return ResultUtil.error("缺少参数");
        }
        Date date = reportService.getReportCompleteTime(reportCode);
        return ResultUtil.success(date);
    }

    /**
     * 报告防伪二维码扫码
     * @param reportCode
     * @return
     */
    @GetMapping("qrCodeAuth")
    public Result qrCodeAuth(String reportCode){
        if (StringUtils.isEmpty(reportCode)){
            return ResultUtil.error("无效二维码");
        }
        QrCodeAuthRes qrCodeAuthRes = entrustService.qrCodeAuth(reportCode);
        return ResultUtil.success(qrCodeAuthRes);
    }
}

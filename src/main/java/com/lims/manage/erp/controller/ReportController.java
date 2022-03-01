package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.SealReqEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
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
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    private ReportRecordEntityMapper recordEntityMapper;

    Logger logger = LoggerFactory.getLogger(ReportController.class);

    /**
     * 查询可制作报告任务单列表
     *
     * @return
     */
    @GetMapping("/list")
    public Result getSampleList() {
        return ResultUtil.success("获取可制作报告任务单成功！", reportService.getReportList());
    }

    /**
     * 提交审批
     *
     * @param id
     * @return
     */
    @GetMapping("/report_submit")
    public Result getReportSubmit(Long id) {
        if (id == null) {
            return ResultUtil.error(678, "缺少必要参数！");
        }
        // 查询是否提交审批
        ReportRecordEntity reportData = recordEntityMapper.getReportEntrust(id);
        if (reportData == null) {
            return ResultUtil.error(678, "参数错误！");
        }
        if (reportData.getReportCompleteTime() != null) {
            return ResultUtil.error(678, "报告已提交审批！");
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
        Boolean flag = reportService.getReportSubmit(id, name);
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
    public Result getlist_history(String search) {
        return ResultUtil.success("获取历史任务单成功！", reportService.getReportList_history(search));
    }

    /**
     * 查询报告生成列表--历史查询_详情
     *
     * @param id
     * @return
     */
    @GetMapping("/list_history_details")
    public Result getlist_history_details(Long id) {
        return ResultUtil.success("获取历史任务单成功！", reportService.getReportList_history_details(id));
    }


    /**
     * 报告生成--编辑按钮
     *
     * @return
     */
    @GetMapping("/edit")
    public Result edit(Long id) {
        return ResultUtil.success("查询委托单信息成功！", reportService.getReportDetail(id));
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
     *
     * @param type
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("sealList")
    public Result sealList(String type, String search, Integer pageNum, Integer pageSize, String reportType) {
        if (StringUtils.isEmpty(type) || pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少必要的参数！");
        }
        PageInfo pageInfo = reportService.sealList(type, search, pageNum, pageSize, reportType);
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
    public Result seal(Long entrustId) {
        if (entrustId == null) {
            return ResultUtil.error("缺少必要的参数！");
        }
        Boolean flag = reportService.seal(entrustId);
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
    public Result signurl(@RequestBody QiYueSuoReqBean reqBean){
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
     * 契约锁报告下载
     * @param contractId
     * @param name
     * @param contact
     * @return
     */
    @GetMapping("downloadQysFile")
    public Result downloadQysFile(Long entrustId, Long contractId,String name,String contact){
        if (contractId == null || StringUtils.isEmpty(name) || StringUtils.isEmpty(contact)){
            return ResultUtil.error("缺少必要参数");
        }
        reportService.downloadQysFile(entrustId,contractId,name,contact);
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
    @GetMapping("/getTemplateList")
    public Result getTemplateList(String productId) {
        return ResultUtil.success("查询产品报告模板成功！", reportService.getReportTemplateList(productId));
    }

    /**
     * 下载报告
     *
     * @param id
     * @param code
     * @param response
     * @return
     */
    @GetMapping("download")
    public String downReport(Long id, String code, HttpServletResponse response) {
        //从文件服务器拉取文件
        MinioClient client = MinIoUtil.minioClient;
        String url = "";
        try {
        url = reportService.downLoad(client,code,id);
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
            document.close();
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
            document.close();
        } catch (Exception e) {
            logger.error("转换失败:{}", e);
        }
    }


}

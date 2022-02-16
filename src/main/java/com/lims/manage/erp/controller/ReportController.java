package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
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
     * 盖章
     *
     * @param entity
     * @return
     */
    @PostMapping("seal")
    public Result seal(@RequestBody SealReqEntity entity) {
        if (StringUtils.isEmpty(entity.getList())) {
            return ResultUtil.error("缺少必要的参数！");
        }
        String[] split = entity.getList().split(",");
        List<String> list = Arrays.asList(split);
        Boolean flag = reportService.seal(list, entity.getId());
        if (flag) {
            return ResultUtil.success("获取印章成功!");
        } else {
            return ResultUtil.error("获取印章失败！");
        }
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
        ReportRecordEntity reportRecordEntity = reportService.selectByEntrustId(id);
        //从文件服务器拉取文件
        MinioClient client = MinIoUtil.minioClient;
        XWPFDocument doc = null;
        String url = null;
        try {
            client.statObject("report-word", code + ".docx");
            InputStream object = client.getObject("report-word", code + ".docx");
            doc = new XWPFDocument(object);
            //写入数据
            List<ReportRecordDetailEntity> checkItemList = reportService.getCheckInfoByRecordId(reportRecordEntity.getId());
            if (CollectionUtils.isNotEmpty(checkItemList)) {
                //处理表格
                Iterator<XWPFTable> it = doc.getTablesIterator();
                //表格索引
                int i = 1;
                //获取表格信息
                while (it.hasNext()) {
                    XWPFTable table = it.next();
                    List<XWPFTableRow> rows = table.getRows();
                    //存放表头信息
                    EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);
                    if (i == 1) {
                        rows.get(4).getCell(1).removeParagraph(0);
                        rows.get(4).getCell(1).setText(entrustHistoryDetail.getEntrustCompany());
                        rows.get(4).getCell(3).removeParagraph(0);
                        rows.get(4).getCell(3).setText(entrustHistoryDetail.getProjectName());
                        rows.get(5).getCell(1).removeParagraph(0);
                        rows.get(5).getCell(1).setText(entrustHistoryDetail.getProjectPart());
                        //样品信息
                        SampleEntity sampleEntity = entrustHistoryDetail.getSamples().get(0);
                        rows.get(6).getCell(1).removeParagraph(0);
                        rows.get(6).getCell(1).setText("样品名称：" + (sampleEntity.getSampleName() == null ? "——" : sampleEntity.getSampleName())
                                + "样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode())
                                + "样品数量：" + (sampleEntity.getQuantityPerGroup() == null ? "——" : sampleEntity.getQuantityPerGroup())
                                + "样品状态：" + (sampleEntity.getOutward() == null ? "——" : sampleEntity.getOutward())
                                + "收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
                        //检测依据
                        String checkBasis = reportService.getCheckBasis(id);
                        rows.get(7).getCell(1).removeParagraph(0);
                        rows.get(7).getCell(1).setText(checkBasis.equals("") ? "——" : checkBasis);
                        //判定依据
                        String judgeBasis = reportService.getJudgeBasis(id);
                        rows.get(7).getCell(3).removeParagraph(0);
                        rows.get(7).getCell(3).setText(judgeBasis.equals("") ? "——" : judgeBasis);
                        //检测日期
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                        rows.get(8).getCell(1).removeParagraph(0);
                        rows.get(8).getCell(1).setText(sdf.format(entrustHistoryDetail.getAcceptanceDate()) + "~"
                                + sdf.format(reportRecordEntity.getReportCompleteTime() == null ? new Date() : reportRecordEntity.getReportCompleteTime())
                        );
                        //主要仪器
                        String equipment = reportService.getEquipment(id);
                        rows.get(9).getCell(1).removeParagraph(0);
                        rows.get(9).getCell(1).setText(equipment.equals("") ? "——" : equipment);
                        //委托编号
                        rows.get(10).getCell(1).removeParagraph(0);
                        rows.get(10).getCell(1).setText(entrustHistoryDetail.getEntrustmentNo() + "");
                        //检测类别
                        rows.get(10).getCell(3).removeParagraph(0);
                        rows.get(10).getCell(3).setText(entrustHistoryDetail.getCheckPurpose());
                        //批号
                        rows.get(11).getCell(1).removeParagraph(0);
                        rows.get(11).getCell(1).setText(sampleEntity.getBatchNumber() == null ? "——" : sampleEntity.getBatchNumber());
                        //生产厂家
                        rows.get(11).getCell(3).removeParagraph(0);
                        rows.get(11).getCell(3).setText(sampleEntity.getManufacturer() == null ? "——" : sampleEntity.getManufacturer());
                        //规格等级
                        rows.get(12).getCell(1).removeParagraph(0);
                        rows.get(12).getCell(1).setText(sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs());
                        //代表数量
                        rows.get(12).getCell(3).removeParagraph(0);
                        rows.get(12).getCell(3).setText(sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration());
                    }
                    //存放检测数据
                    for (ReportRecordDetailEntity item : checkItemList) {
                        int page = Integer.parseInt(item.getCoordinate().split(",")[0]);
                        int row = Integer.parseInt(item.getCoordinate().split(",")[1]);
                        int column = Integer.parseInt(item.getCoordinate().split(",")[2]);
                        if (i == page) {
                            rows.get(row).getCell(column + 1).removeParagraph(0);
                            rows.get(row).getCell(column + 1).setText(item.getSpecsContent());
                            rows.get(row).getCell(column + 2).removeParagraph(0);
                            rows.get(row).getCell(column + 2).setText(item.getCheckResult());
                            rows.get(row).getCell(column + 3).removeParagraph(0);
                            rows.get(row).getCell(column + 3).setText(item.getJudgeResult());
                        }
                    }
                    i++;
                }
            }
            ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(doc);
            //盖章
            String sealUrl = reportRecordEntity.getSealUrl();
            List<ImagePro> imagePros = Lists.newArrayList();
            if (!StringUtils.isEmpty(sealUrl) || sealUrl != null) {
                String[] split = sealUrl.split(",");
                for (int i = 0; i < split.length; i++) {
                    ImagePro pro = new ImagePro(100 * (i + 1), 100, 15F, split[i]);
                    imagePros.add(pro);
                }
            }
            InputStream inputStream1 = new ByteArrayInputStream(b1.toByteArray());
            ByteArrayOutputStream b2 = new ByteArrayOutputStream();
            ByteArrayOutputStream b3 = ImageToPdfUtils.writeToPdf4(inputStream1, b2, imagePros);
            InputStream inputStream = FileAndFolderUtil.parseOut(b3);
            url = MinIoUtil.upload("report-download", reportRecordEntity.getReportCode() + ".pdf", inputStream, "application/octet-stream");
            reportService.updateReportUrl(reportRecordEntity.getId(), url);

//            ServletOutputStream outputStream = response.getOutputStream();
//            FileAndFolderUtil.parseIn(inputStream)

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

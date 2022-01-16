package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.flowable.common.engine.impl.util.CollectionUtil;
import org.jodconverter.DocumentConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
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
    private DocumentConverter converter;  //用于转换

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
    public Result sealList(String type, String search, Integer pageNum, Integer pageSize) {
        if (StringUtils.isEmpty(type) || pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少必要的参数！");
        }
        PageInfo pageInfo = reportService.sealList(type, search, pageNum, pageSize);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 盖章
     *
     * @param list
     * @param id
     * @return
     */
    @PostMapping("seal")
    public Result seal(@RequestParam("list") List<String> list, @RequestParam("id") Long id) {
        if (CollectionUtils.isEmpty(list)) {
            return ResultUtil.error("缺少必要的参数！");
        }
        Boolean flag = reportService.seal(list, id);
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
        MinioClient client = MinIoUtil.minioClient;
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
            Map<String, Object> map = new HashMap<>();
            //查询报告详细信息
            List<ReportRecordDetailEntity> detailEntityList = reportService.getReportDetailByCode(reportCode);
//            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_report, reportName);
            //填充数据
            Long entrustId = reportService.getEntrustIdByCode(reportCode);
            EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
            String sealUrl = entity.getSealUrl();
            XWPFDocument document = reportService.preview(detailEntityList, detail, object, sealUrl.split(","));
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
        MinioClient client = MinIoUtil.minioClient;
        try {
            // 调用statObject()来判断对象是否存在。
            // 如果不存在, statObject()抛出异常,
            // 否则则代表对象存在。
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

    @PostMapping("download")
    public String downReport(Long id,String code, HttpServletResponse response) {
        ReportRecordEntity reportRecordEntity = reportService.selectByEntrustId(id);
        //从文件服务器拉取文件
        MinioClient client = MinIoUtil.minioClient;
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String location = req.getServletContext().getRealPath("/file/");
        long template = System.currentTimeMillis();
        String templateTemp = location + template + ".docx";
        try {
            client.statObject("report-word", code + ".docx");
            InputStream in = client.getObject("report-word", code + ".docx");
            OutputStream out = new FileOutputStream(templateTemp);
            IOUtils.copy(in, out);
            in.close();
            out.close();
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
        //写入数据
        List<ReportRecordDetailEntity> checkItemList = reportService.getCheckInfoByRecordId(reportRecordEntity.getId());
        XWPFDocument doc = null;
        try {
            OPCPackage pack = POIXMLDocument.openPackage(templateTemp);
            doc = new XWPFDocument(pack);
            if (CollectionUtil.isNotEmpty(checkItemList)) {
                //处理表格
                Iterator<XWPFTable> it = doc.getTablesIterator();
                //表格索引
                int i = 1;
                //获取表格信息
                while (it.hasNext()) {
                    XWPFTable table = it.next();
                    List<XWPFTableRow> rows = table.getRows();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        //生成写入数据后的文档
        String wordTemp = location + reportRecordEntity.getReportCode() + ".docx";
        try {
            FileOutputStream fopts = new FileOutputStream(wordTemp);
            doc.write(fopts);
            fopts.close();
        } catch (Exception e) {
            logger.error("word文档生成异常:{}", e);
        }
        //将word转换成pdf
        File file = new File(wordTemp);//需要转换的文件
        String pdfPath = location; //pdf文件生成保存的路径
        long pdfName = System.currentTimeMillis();
        String fileType = ".pdf"; //pdf文件后缀
        String pdfTemp = pdfPath + reportRecordEntity.getReportCode() + fileType;  //将这三个拼接起来,就是我们最后生成文件保存的完整访问路径了
        try {
            File newFile = new File(location);//转换之后文件生成的地址
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            //文件转换
            converter.convert(file).to(new File(pdfTemp)).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //将PDF文件上传至文件服务器
        try {
            client.putObject("report-download", reportRecordEntity.getReportCode() + fileType, pdfTemp);
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
        //下载pdf文件
//        try {
//            minioClient.statObject("report-download", pdfName+fileType);
//            InputStream in = minioClient.getObject("report-download", pdfName+fileType);
//            ServletOutputStream outputStream = response.getOutputStream();
//            int i = IOUtils.copy(in, outputStream);
//            in.close();
//            outputStream.close();
//            System.out.println("流已关闭,可下载,该文件字节大小："+i);
//        } catch (MinioException e) {
//            System.out.println("Error occurred: " + e);
//        } catch (XmlPullParserException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        }
        String url = null;
        try {
            url = client.presignedGetObject("report-download", reportRecordEntity.getReportCode() + fileType, 60 * 60 * 24);
            System.out.println(url);
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
        //删除临时文件
        File templateFile = new File(templateTemp);
        templateFile.delete();
        File wordFile = new File(wordTemp);
        wordFile.delete();
        File pdfFile = new File(pdfTemp);
        pdfFile.delete();
        return url;
    }
}

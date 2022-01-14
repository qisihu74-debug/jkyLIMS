package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
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
     * @param list
     * @param id
     * @return
     */
    @GetMapping("seal")
    public Result seal(@RequestParam("list") List<String> list,Long id){
        if (CollectionUtils.isEmpty(list)){
            return ResultUtil.error("缺少必要的参数！");
        }
        Boolean flag = reportService.seal(list,id);
        if (flag){
            return ResultUtil.success("获取印章成功!");
        }else {
            return ResultUtil.error("获取印章失败！");
        }
    }

    /**
     * 报告预览
     * @param type
     * @param reportCode
     * @return
     */
    @GetMapping("preview")
    public Result preview(String type, String reportCode, HttpServletResponse response){
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(reportCode)){
            return ResultUtil.error("缺少必要参数！");
        }
        //TODO 根据报告模板url获取文件名
        //查询报告信息
        //获取文件名称
        String fileName = "";
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_report, fileName);
            //TODO 填充数据
            Map<String,Object> map = new HashMap<>();
            //EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
            XWPFDocument document = reportService.preview(map, object);
            response.reset();
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            fileName = URLEncoder.encode(fileName,"UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName="+fileName);
            OutputStream outputStream = response.getOutputStream();
            document.write(outputStream);
            outputStream.close();
        } catch (Exception ex) {
            log.info("报告预览失败：", ex.getMessage());
        }
        return null;
    }
}

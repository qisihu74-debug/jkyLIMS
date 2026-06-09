package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.CustomerAccountEntity;
import com.lims.manage.erp.mapper.CustomerPortalMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.CustomerPortalAuthSupport;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.CustomerReportVo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/customer/report")
public class CustomerReportController {

    @Autowired
    private CustomerPortalAuthSupport customerAuth;
    @Autowired
    private CustomerPortalMapper customerPortalMapper;

    @GetMapping("/list")
    public Result list(HttpServletRequest request, @RequestParam(required = false) String keyword) {
        CustomerAccountEntity account = customerAuth.requireCustomer(request);
        if (account == null) return customerAuth.unauth();
        if (account.getBindCompanyId() == null) {
            return ResultUtil.error(403, "请先完成历史客户认领后再查询报告");
        }
        List<CustomerReportVo> list = customerPortalMapper.selectCustomerReports(account.getBindCompanyId(), keyword);
        return ResultUtil.success(list);
    }

    @GetMapping("/download")
    public void download(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestParam Long reportId) throws Exception {
        CustomerAccountEntity account = customerAuth.requireCustomer(request);
        if (account == null) {
            response.sendError(401, "客户登录已过期，请重新登录");
            return;
        }
        if (account.getBindCompanyId() == null) {
            response.sendError(403, "请先完成历史客户认领后再下载报告");
            return;
        }
        CustomerReportVo report = customerPortalMapper.selectCustomerReportDownload(reportId, account.getBindCompanyId());
        if (report == null || StringUtils.isBlank(report.getDownloadUrl())) {
            response.sendError(404, "报告不存在或无权访问");
            return;
        }
        streamMinioFile(report, response);
    }

    private void streamMinioFile(CustomerReportVo report, HttpServletResponse response) throws Exception {
        String url = report.getDownloadUrl().trim();
        URI uri = URI.create(url);
        String path = uri.getPath();
        if (StringUtils.isBlank(path) || "/".equals(path)) {
            response.sendRedirect(url);
            return;
        }
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        int slash = cleanPath.indexOf('/');
        if (slash <= 0 || slash >= cleanPath.length() - 1) {
            response.sendRedirect(url);
            return;
        }
        String bucket = cleanPath.substring(0, slash);
        String fileName = cleanPath.substring(slash + 1);
        String downloadName = StringUtils.defaultIfBlank(report.getReportCode(), "report") + suffix(fileName);
        InputStream fileStream = MinIoUtil.getFileStream(bucket, fileName);
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(downloadName, "UTF-8"));
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        OutputStream outputStream = response.getOutputStream();
        IOUtils.copy(fileStream, outputStream);
        fileStream.close();
        outputStream.close();
    }

    private String suffix(String fileName) {
        if (StringUtils.isBlank(fileName)) return ".pdf";
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) return ".pdf";
        return fileName.substring(index);
    }
}

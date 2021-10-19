package com.stu.manage.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.stu.manage.demo.entity.InvoiceEntity;
import com.stu.manage.demo.filter.PassToken;
import com.stu.manage.demo.result.Result;
import com.stu.manage.demo.result.ResultUtil;
import com.stu.manage.demo.util.BaiduOrcUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.controller
 * @desc
 * @date 2021/10/15 14:30
 * @Copyright © 河南交科院
 */
@RestController
@Slf4j
@RequestMapping("/orc/")
public class OrcController {
    @Value("${orc.client.id}")
    private String clientId;
    @Value("${orc.client.secret}")
    private String clientSecret;
    @Value("${orc.auth.host}")
    private String authHost;
    @Value("${orc.invoice.url}")
    private String invoiceUrl;
    @Value("${orc.invoice.verification.url}")
    private String invoiceVerificationUrl;
    @Value("${orc.from.url}")
    private String formUrl;

    /**
     * 获取增值税发票信息
     * @param uploadFile
     * @return
     */
    @PassToken
    @RequestMapping(value="getOrcMessage", method= RequestMethod.POST)
    @ResponseBody
    public Result getOrcMessage(MultipartHttpServletRequest uploadFile) {
        MultipartFile file = uploadFile.getFile("file");
        if (file == null){
            return ResultUtil.error(-1,"文件参数名称不正确");
        }
        Boolean flag = null;
        try {
            InputStream in = file.getInputStream();
            String ocrResult = BaiduOrcUtils.getocrByInputStream(in,clientId,clientSecret,authHost,invoiceUrl);
            Map resultMap = JSONObject.parseObject(ocrResult,Map.class);
            String invoiceString = JSONObject.toJSONString(resultMap.get("words_result"));
            InvoiceEntity invoice = JSONObject.parseObject(invoiceString,InvoiceEntity.class);
            //请求接口校验发票真伪 https://aip.baidubce.com/rest/2.0/ocr/v1/vat_invoice_verification
            if (invoice != null){
                flag = BaiduOrcUtils.checkInvoice(invoice,clientId,clientSecret,authHost,invoiceVerificationUrl);
            }
            if (flag != null){
                invoice.setFlag(flag);
            }else {
                invoice.setMessage("发票真伪验证失败");
            }
            return ResultUtil.success(invoice);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析Excel表格图片
     * @param uploadFile
     * @return
     */
    @PassToken
    @RequestMapping(value="getExcelMessage", method= RequestMethod.POST)
    @ResponseBody
    public Result getExcelMessage(MultipartHttpServletRequest uploadFile) {
        if (uploadFile == null){
            return ResultUtil.error(-1,"请上传需要识别的Excel图片");
        }
        MultipartFile file = uploadFile.getFile("uploadFile");
        Boolean flag = null;
        try {
            InputStream in = file.getInputStream();
            String ocrResult = BaiduOrcUtils.getocrByInputStream(in,clientId,clientSecret,authHost,formUrl);
            Map resultMap = JSONObject.parseObject(ocrResult,Map.class);
            String invoiceString = JSONObject.toJSONString(resultMap.get("words_result"));
            System.out.println("========");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}

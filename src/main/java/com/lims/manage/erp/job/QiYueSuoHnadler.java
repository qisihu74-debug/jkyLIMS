package com.lims.manage.erp.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.http.HttpClientUtil;
import com.lims.manage.erp.http.HttpResponse;
import com.lims.manage.erp.http.QiYueSuoResponse;
import io.micrometer.core.ipc.http.HttpSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.job
 * @desc
 * @date 2022/2/23 10:09
 * @Copyright © 河南交科院
 */
@Slf4j
@Component
public class QiYueSuoHnadler {
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;

    /**
     * 根据文件类型创建合同文档
     * @param file (必传) pdf报告文件
     * @param title (必传)名称
     * @param fileType (必传)文件类型：doc, docx, txt, pdf, png, gif, jpg, jpeg, tiff, html, rtf, xls, txt
     * @param waterMarks （非必传）水印，json格式字符串；如：[{"content":"水印1","fontSize":"30","location":"UPPER_LEFT","imageBase64":"/9j/4AAQSkZJRgABAQEASABIAAD/4gxY"},{"content":"水印2","fontSize":"30","location":"LOWER_LEFT"}]
     * @param width （非必传）宽（文件尺寸，单位：mm）
     * @param height （非必传）高（文件尺寸，单位：mm）
     * @return
     */
    public QiYueSuoResponse creatFile(File file,String title,String fileType,
                                  String waterMarks,Float width,Float height){
        //请求参数设置
        Map<String, String> params = new HashMap<>();
        params.put("title",title);
        params.put("fileType",fileType);
        //文件参数设置
        Map<String, File> files = new HashMap<>();
        files.put("file",file);
        //设置请求头
        Map<String, String> headers = getHeaders();
        //请求契约锁接口（请求方式由契约锁接口约定）
        String url = qiYueSuoEntity.getUrl() + qiYueSuoEntity.getCreateInterface();
        Pair<Integer, String> stringPair = HttpClientUtil.postFormIncludeFile(url, params, files, headers);
        log.debug("响应信息:{}", JSON.toJSONString(stringPair));
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(stringPair));
        QiYueSuoResponse response = null;
        if (stringPair.getKey() == 200){
            response = JSONObject.parseObject(jsonObject.get(200).toString(), QiYueSuoResponse.class);
        }
        return response;
    }

    /**
     * 契约锁创建文档信息
     * @param reqBean
     * @return
     */
    public QiYueSuoResponse createbycategory(QiYueSuoReqBean reqBean) {
        //设置请求头
        Map<String, String> headers = getHeaders();
        //请求契约锁接口（请求方式由契约锁接口约定）
        String url = qiYueSuoEntity.getUrl() + qiYueSuoEntity.getAddInterface();
        //TODO 如果是盖多个章，需要action结构里多个对象，不能将多个参数放入sealIds
        //Pair<Integer, String> stringPair = HttpClientUtil.postJson(url, JSON.toJSONString(reqBean), headers);
        QiYueSuoResponse deptListOfQYS = getDeptListOfQYS("COMPANY", "河南省交通科学技术研究院有限公司");
        //TODO 获取到部门列表设置sealOwner参数

        //TODO 获取印章列表获取sealIds参数

        String boday = "{\n" +
                " \"subject\": \"郭家林测试\",\n" +
                " \"categoryId\": \"2934717410113839636\",\n" +
                " \"send\": true,\n" +
                " \"documents\": [\"2936115323254304790\"],\n" +
                " \"tenantName\": \"河南省公路工程试验检测中心有限公司\",\n" +
                " \"creatorName\": \"郭家林\",\n" +
                " \"creatorContact\": \"18337165257\",\n" +
                " \"signatories\": [{\n" +
                "  \"tenantType\": \"COMPANY\",\n" +
                "  \"tenantName\": \"河南省公路工程试验检测中心有限公司\",\n" +
                "  \"serialNo\": \"1\",\n" +
                "  \"actions\": [{\n" +
                "   \"type\": \"CORPORATE\",\n" +
                "   \"name\": \"企业签章\",\n" +
                "   \"serialNo\": \"1\",\n" +
                "   \"sealIds\": \"[2934033400316387595]\",\n" +
                "   \"actionOperators\": [{\n" +
                "    \"operatorName\": \"郭家林\",\n" +
                "    \"operatorContact\": \"18337165257\"\n" +
                "\n" +
                "   }]\n" +
                "  }]\n" +
                " }]\n" +
                "}";
        Pair<Integer, String> stringPair = HttpClientUtil.postJson(url, boday, headers);
        log.debug("契约锁创建文档响应信息:{}", JSON.toJSONString(stringPair));
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(stringPair));
        QiYueSuoResponse response = null;
        if (stringPair.getKey() == 200){
            response = JSONObject.parseObject(jsonObject.get(200).toString(), QiYueSuoResponse.class);
        }
        return response;
    }

    /**
     * 获取契约锁报告签署url地址
     * @param reqBean
     * @return
     */
    public QiYueSuoResponse signurl(QiYueSuoReqBean reqBean) {
        //设置请求头
        Map<String, String> headers = getHeaders();
        //请求契约锁接口（请求方式由契约锁接口约定）
        String url = qiYueSuoEntity.getUrl() + qiYueSuoEntity.getSignInterface();
        //Pair<Integer, String> stringPair = HttpClientUtil.postJson(url, JSON.toJSONString(reqBean), headers);
        String boday = "{\n" +
                "    \"contractId\": 2936126231317803041,\n" +
                "    \"tenantName\": \"河南省公路工程试验检测中心有限公司\",\n" +
                "    \"tenantType\": \"COMPANY\",\n" +
                "    \"contact\":\"18337165257\",\n" +
                "    \"receiverName\":\"郭家林\"\n" +
                "   \n" +
                "}";
        Pair<Integer, String> stringPair = HttpClientUtil.postJson(url, boday, headers);
        log.debug("契约锁获取报告签署响应信息:{}", JSON.toJSONString(stringPair));
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(stringPair));
        QiYueSuoResponse response = null;
        if (stringPair.getKey() == 200){
            response = JSONObject.parseObject(jsonObject.get(200).toString(), QiYueSuoResponse.class);
        }
        return response;
    }

    /**
     * 契约锁报告下载
     * @param contractId
     * @param name
     * @param contact
     */
    public byte[] downloadQysFile(Long contractId, String name, String contact) {
        //设置请求头
        Map<String, String> headers = getHeaders();
        //请求契约锁接口（请求方式由契约锁接口约定）
        String url = qiYueSuoEntity.getUrl() + qiYueSuoEntity.getDownloadInterface();

        //设置请求参数
        Map<String,String> map = new HashMap();
        map.put("contractId",contractId+"");
        map.put("name",name);
        map.put("contact",contact);
        byte[] inputStream = HttpClientUtil.getZip(url,map,headers);
        return inputStream;
    }

    /**
     * 获取契约锁部门列表
     * @param tenantType
     * @param companyName
     */
    public QiYueSuoResponse getDeptListOfQYS(String tenantType, String companyName) {
        //设置请求头
        Map<String, String> headers = getHeaders();
        //请求契约锁接口（请求方式由契约锁接口约定）
        String url = qiYueSuoEntity.getUrl() + qiYueSuoEntity.getDeptInterface();
        //设置请求参数
        Map<String,String> map = new HashMap();
        map.put("tenantType",tenantType);
        map.put("companyName",companyName);
        Pair<Integer, String> stringPair = HttpClientUtil.get(url,map,headers);
        log.debug("契约锁获取部门信息列表成功:{}", JSON.toJSONString(stringPair));
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(stringPair));
        QiYueSuoResponse response = null;
        if (stringPair.getKey() == 200){
            response = JSONObject.parseObject(jsonObject.get(200).toString(), QiYueSuoResponse.class);
        }
        return response;
    }

    /**
     * 获取契约锁部门印章列表
     * @param category 印章类型PHYSICS("物理签章"),ELECTRONIC("电子签章"),不传默认查询电子章
     * @param companyName
     */
    public QiYueSuoResponse sealList(String category, String companyName) {
        //设置请求头
        Map<String, String> headers = getHeaders();
        //请求契约锁接口（请求方式由契约锁接口约定）
        String url = qiYueSuoEntity.getUrl() + qiYueSuoEntity.getListInterface();
        //设置请求参数
        Map<String,String> map = new HashMap();
        map.put("category",category);
        map.put("companyName",companyName);
        Pair<Integer, String> stringPair = HttpClientUtil.get(url,map,headers);
        log.debug("获取契约锁本公司印章列表成功:{}", JSON.toJSONString(stringPair));
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(stringPair));
        QiYueSuoResponse response = null;
        if (stringPair.getKey() == 200){
            response = JSONObject.parseObject(jsonObject.get(200).toString(), QiYueSuoResponse.class);
        }
        return response;
    }

    public Map<String,String> getHeaders(){
        //headers参数设置（MD5进行加密，token加密秘钥 Md5(appToken+appSercert+timestamp+nonce)timestamp当前系统时间不允许偏差超过15分钟、nonce取值UUID）
        Map<String,String> headers = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder(100);
        stringBuilder.append(qiYueSuoEntity.getAppToken());
        stringBuilder.append(qiYueSuoEntity.getAppSecret());
        stringBuilder.append(0);
        String md5Str = DigestUtils.md5DigestAsHex(stringBuilder.toString().getBytes());
        headers.put("x-qys-accesstoken",qiYueSuoEntity.getAppToken());
        headers.put("x-qys-timestamp","0");
        headers.put("x-qys-signature",md5Str);
        return headers;
    }

}

package com.lims.manage.erp.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.ActionOperators;
import com.lims.manage.erp.entity.Actions;
import com.lims.manage.erp.entity.Location;
import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.QiYueSuoSeaLBean;
import com.lims.manage.erp.http.HttpClientUtil;
import com.lims.manage.erp.http.QiYueSuoResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @desc
     * @return
     */
    //公路工程综合甲级专用章 2934033400316387595
    //实验室认可（CNAS）专用章 2937178885881422636
    //计量认证（CMA）专用章 2937188764910183324
    //检验检测专用章（室内试验） 2937191218674492340
    //检验检测专用章（外业检测） 2937192180793304003
        /*{
            "tenantName": "河南省公路工程试验检测中心有限公司",
                "subject": "ZX-2022-JC-0053",
                "ordinal": true,
                "categoryId": "2934717410113839636",
                "send": "true",
                "entrustId": 1651131063868112,
                "documents": [
            "2961597865911407472"
    ],
            "creatorName": "郭家林",
                "creatorContact": "18337165257",
                "extraSign": false,
                "mustSign": true,
                "autoCreateCounterSign" : true,
                "signatories": [
            {
                "tenantType": "COMPANY",
                    "tenantName": "河南省公路工程试验检测中心有限公司",
                    "actions": [
                {
                    "type": "CORPORATE",
                        "name": "公路工程综合甲级专用章",
                        "serialNo": "1",
                        "sealId" : 2934033400316387595,
                        "locations":[{
                    "actionOperators": [{
                        "operatorContact": "18337165257",
                                "operatorName": "郭家林"
                    }],
                    "documentId":"2961597865911407472",
                            "rectType":"SEAL_CORPORATE",
                            "page":1,
                            "offsetX":0.03,
                            "offsetY":0.002,
                            "actionName":"企业签章10"
                }]
                },
                {
                    "type": "CORPORATE",
                        "name": "计量认证（CNS）专用章",
                        "serialNo": "1",
                        "sealId": 2937178885881422636,
                        "locations":[{
                    "actionOperators": [{
                        "operatorContact": "18337165257",
                                "operatorName": "郭家林"
                    }],
                    "documentId":"2961597865911407472",
                            "rectType":"SEAL_CORPORATE",
                            "page":1,
                            "offsetX":0.08,
                            "offsetY":0.002,
                            "actionName":"企业签章11"
                }]
                },
                {
                    "type": "CORPORATE",
                        "name": "检验检测专用章（外业检测）",
                        "serialNo": "1",
                        "sealId": 2937192180793304003,
                        "locations":[{
                    "actionOperators": [{
                        "operatorContact": "18337165257",
                                "operatorName": "郭家林"
                    }],
                    "documentId":"2961597865911407472",
                            "rectType":"ACROSS_PAGE",
                            "page":0,
                            "offsetY":0.5,
                            "actionName":"企业签章21"
                }]
                }
            ]
            }
    ]
        }*/
    @SneakyThrows
    public QiYueSuoResponse createbycategory(QiYueSuoReqBean reqBean) {
        //设置用印流程id
        reqBean.setCategoryId(qiYueSuoEntity.getCategoryId());
        //设置请求头
        Map<String, String> headers = getHeaders();
        //请求契约锁接口（请求方式由契约锁接口约定）
        String url = qiYueSuoEntity.getUrl() + qiYueSuoEntity.getAddInterface();
        //处理参数支持每个印章固定位置
        List<Actions> actions = reqBean.getSignatories().get(0).getActions();
        //剔除最后一个action盖章动作，改为根据印章列表不同印章固定盖章位置
        Actions lastAction = actions.get(actions.size() - 1);
        actions.remove(actions.size()-1);
        List<String> documents = reqBean.getDocuments();
        for (int i = 0; i < actions.size(); i++) {
            //签署位置  检测： ，审核：，批准：
            if (i==0){
                List<Location> locations = Lists.newArrayList();
                for (String s:documents){
                    Location location = new Location();
                    location.setRectType("SEAL_PERSONAL");
                    location.setPage(-1);
                    location.setDocumentId(s);
                    location.setActionName("检测人签字");
                    location.setKeyword("检测：");
                    location.setOffsetX(-0.05);
                    location.setOffsetY(-0.01);
                    locations.add(location);
                }
                actions.get(i).setLocations(locations);
            }
            if (i==1){
                List<Location> locations = Lists.newArrayList();
                for (String s:documents){
                    Location location = new Location();
                    location.setRectType("SEAL_PERSONAL");
                    location.setPage(-1);
                    location.setDocumentId(s);
                    location.setActionName("检测记录人签字");
                    location.setKeyword("检测：");
                    location.setOffsetX(0.05);
                    location.setOffsetY(-0.01);
                    locations.add(location);
                }
                actions.get(i).setLocations(locations);
            }
            if (i==2){
                List<Location> locations = Lists.newArrayList();
                for (String s:documents){
                    Location location = new Location();
                    location.setRectType("SEAL_PERSONAL");
                    location.setPage(-1);
                    location.setDocumentId(s);
                    location.setActionName("审核人签字");
                    location.setKeyword("审核：");
                    location.setOffsetX(-0.05);
                    location.setOffsetY(-0.01);
                    locations.add(location);
                }
                actions.get(i).setLocations(locations);
            }
            if (i==3){
                List<Location> locations = Lists.newArrayList();
                for (String s:documents){
                    Location location = new Location();
                    location.setRectType("SEAL_PERSONAL");
                    location.setPage(-1);
                    location.setDocumentId(s);
                    location.setActionName("批准人签字");
                    location.setKeyword("批准：");
                    location.setOffsetX(-0.05);
                    location.setOffsetY(-0.01);
                    locations.add(location);
                }
                actions.get(i).setLocations(locations);
            }
        }
        //设置不同印章的位置
        ObjectMapper objectMapper = new ObjectMapper();
        long[] numbers = objectMapper.readValue(lastAction.getSealIds(), new TypeReference<long[]>() {});
        List<ActionOperators> actionOperators = lastAction.getActionOperators();
        String operatorName = actionOperators.get(0).getOperatorName();
        String operatorContact = actionOperators.get(0).getOperatorContact();
        for (Long sealId :numbers){
            List<ActionOperators> list = Lists.newArrayList();
            ActionOperators operators = new ActionOperators();
            operators.setOperatorName(operatorName);
            operators.setOperatorContact(operatorContact);
            list.add(operators);
            Actions newActions = new Actions();
            newActions.setActionOperators(list);
            newActions.setName(lastAction.getName());
            newActions.setType(lastAction.getType());
            newActions.setSerialNo("5");
            newActions.setSealId(sealId);
            //如果印章是CMA盖章位置在第一页左上角，如果印章是CNAS印章位置在第一页上面中间位置，
            //如果印章是综合甲级印章位置在第一页的右上角，如果是室内印章盖章位置在第一页的下面中间位置
            // 和报告每页含有关键字（日期：）的位置和每一页右边中间位置需要盖骑缝章
            //公路工程综合甲级专用章 2934033400316387595
            //实验室认可（CNAS）专用章 2937178885881422636
            //计量认证（CMA）专用章 2937188764910183324
            //检验检测专用章（室内试验） 2937191218674492340
            List<Location> locations = Lists.newArrayList();
            for (String s:documents){
                if ("2937188764910183324".equals(sealId.toString())){
                    Location location = new Location();
                    location.setRectType("SEAL_CORPORATE");
                    location.setPage(1);
                    location.setDocumentId(s);
                    location.setOffsetX(0.1);
                    location.setOffsetY(0.85);
                    locations.add(location);
                }else if ("2937178885881422636".equals(sealId.toString())){
                    Location location = new Location();
                    location.setRectType("SEAL_CORPORATE");
                    location.setPage(1);
                    location.setDocumentId(s);
                    location.setOffsetX(0.4);
                    location.setOffsetY(0.85);
                    locations.add(location);
                }else if ("2934033400316387595".equals(sealId.toString())){
                    Location location = new Location();
                    location.setRectType("SEAL_CORPORATE");
                    location.setPage(1);
                    location.setDocumentId(s);
                    location.setOffsetX(0.8);
                    location.setOffsetY(0.88);
                    locations.add(location);
                }else if ("2937191218674492340".equals(sealId.toString())){
                    Location location = new Location();
                    location.setRectType("SEAL_CORPORATE");
                    location.setPage(1);
                    location.setDocumentId(s);
                    location.setOffsetX(0.35);
                    location.setOffsetY(0.12);
                    locations.add(location);
                    Location keyLocation = new Location();
                    keyLocation.setRectType("SEAL_CORPORATE");
                    keyLocation.setDocumentId(s);
                    keyLocation.setKeyword("日期：");
                    keyLocation.setOffsetX(-0.1);
                    keyLocation.setOffsetY(0.02);
                    keyLocation.setKeywordIndex(0);
                    locations.add(keyLocation);
                    Location qf = new Location();
                    qf.setRectType("ACROSS_PAGE");
                    qf.setDocumentId(s);
                    qf.setOffsetY(0.5);
                    locations.add(qf);
                }else {
                    log.debug("未定义的印章类型:{}",sealId);
                    continue;
                }
                newActions.setLocations(locations);
            }
            actions.add(newActions);
        }
        log.debug("请求契约锁参数:{}",JSON.toJSONString(reqBean));
        Pair<Integer, String> stringPair = HttpClientUtil.postJson(url, JSON.toJSONString(reqBean), headers);
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
    public QiYueSuoResponse signurl(QiYueSuoSeaLBean reqBean) {
        //设置请求头
        Map<String, String> headers = getHeaders();
        //请求契约锁接口（请求方式由契约锁接口约定）
        String url = qiYueSuoEntity.getUrl() + qiYueSuoEntity.getSignInterface();
        Pair<Integer, String> stringPair = HttpClientUtil.postJson(url, JSON.toJSONString(reqBean), headers);
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

    /**
     * 设置契约锁请求头
     * @return
     */
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

    /**
     * 进行原始记录操作
     *
     * @param reqBean
     * @return
     */
    public QiYueSuoResponse createbyTestcategory(QiYueSuoReqBean reqBean) {
        //设置用印流程id
        reqBean.setCategoryId(qiYueSuoEntity.getCategoryId());
//        reqBean.setCategoryId(null);
        //设置请求头
        Map<String, String> headers = getHeaders();
        //请求契约锁接口（请求方式由契约锁接口约定）
        String url = qiYueSuoEntity.getUrl() + qiYueSuoEntity.getAddInterface();
//        String url = null;
        //处理参数支持每个印章固定位置
        List<Actions> actions = reqBean.getSignatories().get(0).getActions();
        for (int i = 0; i < actions.size(); i++) {
            //签署位置  检测： ，审核：，批准：
            List<String> documents = reqBean.getDocuments();
            if (i == 0) {
                List<Location> locations = Lists.newArrayList();
                for (String s : documents) {
                    Location location = new Location();
                    location.setRectType("SEAL_PERSONAL");
                    location.setPage(0);
                    location.setDocumentId(s);
                    location.setActionName("检测人签字");
                    location.setKeyword("检测：");
                    location.setOffsetX(-0.01);
                    location.setOffsetY(-0.01);
                    location.setKeywordIndex(0);
                    locations.add(location);
                }
                actions.get(i).setLocations(locations);
            }
            if (i==1){
                List<Location> locations = Lists.newArrayList();
                for (String s:documents){
                    Location location = new Location();
                    location.setRectType("SEAL_PERSONAL");
                    location.setPage(0);
                    location.setDocumentId(s);
                    location.setActionName("记录人签字");
                    location.setKeyword("记录：");
                    location.setOffsetX(-0.01);
                    location.setOffsetY(-0.01);
                    location.setKeywordIndex(0);
                    locations.add(location);
                }
                actions.get(i).setLocations(locations);
            }
            if (i==2){
                List<Location> locations = Lists.newArrayList();
                for (String s:documents){
                    Location location = new Location();
                    location.setRectType("SEAL_PERSONAL");
                    location.setPage(0);
                    location.setDocumentId(s);
                    location.setActionName("复核人签字");
                    location.setKeyword("复核：");
                    location.setOffsetX(-0.01);
                    location.setOffsetY(-0.01);
                    location.setKeywordIndex(0);
                    locations.add(location);
                }
                actions.get(i).setLocations(locations);
            }
        }
        log.debug("请求契约锁参数:{}",JSON.toJSONString(reqBean));
        Pair<Integer, String> stringPair = HttpClientUtil.postJson(url, JSON.toJSONString(reqBean), headers);
        log.debug("契约锁创建文档响应信息:{}", JSON.toJSONString(stringPair));
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(stringPair));
        QiYueSuoResponse response = null;
        if (stringPair.getKey() == 200){
            response = JSONObject.parseObject(jsonObject.get(200).toString(), QiYueSuoResponse.class);
        }
        return response;
    }

}

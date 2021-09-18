package com.stu.manage.demo.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.dingtalkcrm_1_0.models.GetOfficialAccountContactsResponse;
import com.aliyun.dingtalkcrm_1_0.models.GetOfficialAccountContactsResponseBody;
import com.stu.manage.demo.entity.CrmEntity;
import com.stu.manage.demo.service.CrmService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.job
 * @desc
 * @date 2021/9/18 9:28
 * @Copyright © 河南交科院
 */
@Component
@Slf4j
public class CrmJob {
    Logger logger = LoggerFactory.getLogger(CrmJob.class);
    private static final String index = "1";
    @Autowired
    private CrmService crmService;
    @Value("${dingtalk.token_url}")
    private String tokenUrl;
    @Value("${dingtalk.app_key}")
    private String appKey;
    @Value("${dingtalk.app_secret}")
    private String appsecret;

    @Async("syncExecutor")
    @Scheduled(cron="0/30 * * * * ? ")
    public void sync(){
        //获取当前最大nextToken
        Boolean flag = true;
        List<CrmEntity> list = new ArrayList<>();
        String nextToken = crmService.getMaxIndex();
        if (StringUtils.isEmpty(nextToken)){
            nextToken = index;
        }
        //获取crm客户信息
        GetOfficialAccountContactsResponse response = null;
        while (flag){
            try {
                response = crmService.getCustomer(tokenUrl, appKey, appsecret,nextToken);
            }catch (Exception e){
                logger.error("获取crm数据失败:{}",e);
            }
            if(response ==null){
                flag =false;
            }else {
                //解析数据加入列表
                GetOfficialAccountContactsResponseBody body = response.getBody();
                if (body != null){
                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(body));
                    if (jsonObject.get("nextToken") != null){
                        HashSet<CrmEntity> list1 = getList(jsonObject);
                        list.addAll(list1);
                        //更新nextToken
                        nextToken = jsonObject.get("nextToken").toString();
                    }else {
                        HashSet<CrmEntity> list1 = getList(jsonObject);
                        for (CrmEntity entity :list1) {
                            CrmEntity crmEntity = crmService.checkExist(entity.getInstanceId());
                            if (crmEntity == null){
                                list.add(entity);
                            }
                        }
                        flag = false;
                    }
                }
            }

        }
        if (!CollectionUtils.isEmpty(list)){
            crmService.BatchSave(list);
        }
    }

    /**
     * 组装数据
     * @param jsonObject
     * @return
     */
    private HashSet<CrmEntity> getList(JSONObject jsonObject){
        HashSet<CrmEntity> list = new HashSet<>();
        List<JSONObject> values = (List<JSONObject>) jsonObject.get("values");
        if (!CollectionUtils.isEmpty(values)){
            for (JSONObject object:values) {
                List<JSONObject> contacts = (List<JSONObject>) object.get("contacts");
                for (JSONObject object1:contacts) {
                    CrmEntity entity = new CrmEntity();
                    entity.setUserId(object.get("userId").toString());
                    JSONObject name = (JSONObject) object1.get("data");
                    entity.setContactName(name.get("contact_name") == null?"":name.get("contact_name").toString());
                    JSONObject data = (JSONObject) object1.get("extendData");
                    entity.setContactUnionId(data.get("contactUnionId") == null?"":data.get("contactUnionId").toString());
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = null;
                    try {
                        date = format.parse(object1.get("createTime").toString());
                    } catch (ParseException e) {
                        logger.error("crm数据日期转换异常:{}",e);
                    }
                    entity.setCreateTime(date);
                    entity.setCreatorUserId(object1.get("creatorUserId") == null?"":object1.get("creatorUserId").toString());
                    entity.setInstanceId(object1.get("instanceId") == null?"":object1.get("instanceId").toString());
                    try {
                        date = format.parse(object1.get("modifyTime").toString());
                    } catch (ParseException e) {
                        logger.error("crm数据日期转换异常:{}",e);
                    }
                    entity.setModifyTime(date);
                    entity.setNextToken(jsonObject.get("nextToken") == null?"":jsonObject.get("nextToken").toString());
                    list.add(entity);
                }
            }
        }
        return list;
    }
}

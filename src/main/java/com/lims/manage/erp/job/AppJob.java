package com.lims.manage.erp.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lims.manage.erp.entity.JsonRootBean;
import com.lims.manage.erp.service.AppService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.job
 * @desc
 * @date 2021/11/22 10:48
 * @Copyright © 河南交科院
 */
@Component
public class AppJob {
    Logger logger = LoggerFactory.getLogger(AppJob.class);
    @Autowired
    private AppService service;

    /**
     * 定时拉取数据
     */
    //@Async("syncExecutor")
    //@Scheduled(cron="*/5 * * * * ?")
    public void sync(){
        int num = service.getIndex();
        JSONObject dataAllMap = null;
        if (num < 31749){
            try {
                String url = "https://www.nssi.org.cn/cssn/front/standard/selectStandardListByCond";
                String cookie = "__jsluid_s=b880ab25b9c9ab27cc50e8b8ec9a2b5f; JSESSIONID=D61263B5168CF48822F47F3AF3E13220; Hm_lvt_bc93e3a5e15e3913035aef1581181e88=1706151236,1706686483; token=; uname=; account_id=; user_id=; relogin_name=; user_type=; Hm_lpvt_bc93e3a5e15e3913035aef1581181e88=1706686518";
                String postData = "page="+num;

                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
                con.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
                con.setRequestProperty("Connection", "keep-alive");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                con.setRequestProperty("Cookie", cookie);
                con.setRequestProperty("Origin", "https://www.nssi.org.cn");
                con.setRequestProperty("Referer", "https://www.nssi.org.cn/nssi/front/listpage.jsp");
                con.setRequestProperty("Sec-Fetch-Dest", "empty");
                con.setRequestProperty("Sec-Fetch-Mode", "cors");
                con.setRequestProperty("Sec-Fetch-Site", "same-origin");
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                con.setRequestProperty("sec-ch-ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"");
                con.setRequestProperty("sec-ch-ua-mobile", "?0");
                con.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");

                con.setDoOutput(true);
                con.getOutputStream().write(postData.getBytes("UTF-8"));

                int responseCode = con.getResponseCode();
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonObject = JSONObject.parseObject(response.toString());
                dataAllMap = (JSONObject) jsonObject.get("dataAllMap");
                List<JsonRootBean> list = (List<JsonRootBean>) dataAllMap.get("standars");
                for (JsonRootBean bean :list){
                    if (StringUtils.isEmpty(bean.getId())){
                        bean.setId(GenID.getUUID());
                    }
                }
                if (list != null){
                    service.saveBatch(list);
                    service.updateIndex(num+1);
                }
            } catch (Exception e) {
                String toJSONString = JSON.toJSONString(dataAllMap.get("standars"));
                List<JsonRootBean> list = JSONArray.parseArray(toJSONString,JsonRootBean.class);
                for (JsonRootBean bean :list){
                    if (StringUtils.isEmpty(bean.getId())){
                        bean.setId(GenID.getUUID());
                    }
                }
                if (list != null){
                    service.saveBatch(list);
                    service.updateIndex(num+1);
                }
            }
        }
    }
}

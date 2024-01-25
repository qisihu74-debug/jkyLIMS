package com.lims.manage.erp.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lims.manage.erp.entity.JsonRootBean;
import com.lims.manage.erp.http.HttpClientUtil;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.service.AppService;
import com.lims.manage.erp.service.DingUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.util.http.parser.Vary;
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
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


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
    
}

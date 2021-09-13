package com.stu.manage.demo.http;

import com.stu.manage.demo.util.SpringContextUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * HttpClient工具类
 * @author drs
 */
public class HttpClientUtil {

    private static Logger log = LoggerFactory.getLogger(HttpClientUtil.class);
    private static HttpClientOperate httpClientOperate = SpringContextUtils.getBean(HttpClientOperate.class);


    /**
     *
     * @param url
     * @return
     */
    public static Pair<Integer,String> get(String url) {
        return get(url,null,null);
    }

    /**
     *
     * @param url
     * @return
     */
    public static Pair<Integer,String> get(String url,Map<String, String> params) {
        return get(url,params,null);
    }

    /**
     *
     * @param url
     * @return
     */
    public static Pair<Integer,String> get(String url,Map<String, String> params,Map<String, String> headerMap) {
        return httpClientOperate.doGet(url,params,headerMap);
    }

    /**
     *
     * @param url
     * @param body
     * @return
     */
    public static Pair<Integer,String> postJson(String url, String body) {
        return postJson(url, body,null);
    }


    /**
     *
     * @param url
     * @param body
     * @return
     */
    public static Pair<Integer,String> postJson(String url, String body, Map<String,String> headers) {
        return httpClientOperate.doPostJson(url,body,headers);
    }

    /**
     *
     * @param url
     * @param params
     * @return
     */
    public static Pair<Integer,String> postForm(String url, Map<String, String> params) {
        return postForm(url, params,null);
    }


    /**
     *
     * @param url
     * @param params
     * @param headers
     * @return
     */
    public static Pair<Integer,String> postForm(String url, Map<String, String> params, Map<String,String> headers) {
        return httpClientOperate.doPostForm(url,params,headers);
    }

}

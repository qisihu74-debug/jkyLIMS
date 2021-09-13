package com.stu.manage.demo.http;

import com.stu.manage.demo.util.SpringContextUtils;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HttpClient
 *
 * @author gjl
 */
@Component("httpClientOperate")
public class HttpClientOperate {

    private static final Logger log = LoggerFactory.getLogger(HttpClientOperate.class);

    private CloseableHttpClient getHttpClient() {
        return SpringContextUtils.getBean(CloseableHttpClient.class);
    }

    public RequestConfig getRequestConfig() {
        return SpringContextUtils.getBean(RequestConfig.class);
    }

    /**
     * get请求
     *
     * @param url
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public Pair<Integer, String> doGet(String url, Map<String, String> params, Map<String, String> headerMap) {
        CloseableHttpResponse response = null;
        HttpEntity httpEntity = null;
        //请求
        try {
            //设置参数
            URIBuilder uriBuilder = new URIBuilder(url);
            if (params != null) {
                for (Map.Entry<String,String> entry: params.entrySet()) {
                    uriBuilder.setParameter(entry.getKey(), entry.getValue());
                }
            }
            url = uriBuilder.build().toString();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(getRequestConfig());
            //设置HeaderMap
            //header 参数
            if (!CollectionUtils.isEmpty(headerMap)) {
                for (SingletonMap.Entry<String,String> entry: headerMap.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            response = this.getHttpClient().execute(httpGet);
            String content = "";
            httpEntity = response.getEntity();
            content = EntityUtils.toString(httpEntity, "UTF-8");
            log.debug("content={}", content);
            return new ImmutablePair<>(response.getStatusLine().getStatusCode(), content);
        } catch (Exception e) {
            log.error("get请求出错:{}", e.getMessage());
            return new ImmutablePair<>(-1, null);
        } finally {
            try {
                if (httpEntity != null) {
                    EntityUtils.consume(httpEntity);
                }
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {
                log.error("关闭HTTP连接时报错:", e);
            }
        }
    }

    /**
     * 有参post请求-form表单
     *
     * @param url
     * @param params
     * @param headerMap
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public Pair<Integer, String> doPostForm(String url, Map<String, String> params, Map<String, String> headerMap) {
        CloseableHttpResponse response = null;
        HttpEntity httpEntity = null;
        try {
            // 创建http POST请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(getRequestConfig());
            if (params != null) {
                // 设置2个post参数，一个是scope、一个是q
                List<NameValuePair> parameters = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String, String> entry:params.entrySet()) {
                    parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                // 构造一个form表单式的实体
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
                // 将请求实体设置到httpPost对象中
                httpPost.setEntity(formEntity);
            }
            //header 参数
            if (!CollectionUtils.isEmpty(headerMap)) {
                for (Map.Entry<String,String> entry : headerMap.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            response = this.getHttpClient().execute(httpPost);
            httpEntity = response.getEntity();
            String res = EntityUtils.toString(httpEntity, "UTF-8");
            log.debug("res={}", res);
            return new ImmutablePair<>(response.getStatusLine().getStatusCode(), res);
        } catch (Exception e) {
            log.error("post请求出错:{}", e.getMessage());
            return new ImmutablePair<>(-1, null);
        } finally {
            try {
                if (httpEntity != null) {
                    EntityUtils.consume(httpEntity);
                }
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {
                log.error("关闭HTTP连接时报错:", e);
            }
        }
    }

    /**
     * Post请求-JSON提交
     *
     * @param url
     * @param json
     * @param headerMap
     * @return
     */
    public Pair<Integer, String> doPostJson(String url, String json, Map<String, String> headerMap) {
        // 创建http POST请求
        HttpEntity httpEntity = null;
        HttpPost httpPost = new HttpPost(url);
        //header 参数
        if (!CollectionUtils.isEmpty(headerMap)) {
            for (Map.Entry<String, String> entry:headerMap.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }
        httpPost.setConfig(getRequestConfig());
        StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient cl = this.getHttpClient();
            response = cl.execute(httpPost);
            httpEntity = response.getEntity();
            response.getStatusLine().getStatusCode();
            String res = EntityUtils.toString(httpEntity, "UTF-8");
            if (log.isDebugEnabled()) {
                log.debug("res={}", res);
            }
            return new ImmutablePair<>(response.getStatusLine().getStatusCode(), res);
        } catch (Exception e) {
            log.error("post json请求出错:", e);
            return new ImmutablePair<>(-1, null);
        } finally {
            try {
                if (httpEntity != null) {
                    EntityUtils.consume(httpEntity);
                }
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {
                log.error("关闭HTTP连接时报错", e);
            }
        }
    }

}

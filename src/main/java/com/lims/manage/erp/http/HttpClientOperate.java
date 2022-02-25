package com.lims.manage.erp.http;

import com.lims.manage.erp.util.SpringContextUtils;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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

    /**
     * 发送post请求form表单参数带File对象
     * @param url
     * @param file
     * @param params
     * @param headers
     * @return
     */
    public Pair<Integer, String> doPostFormIncludeFile(String url, File file, Map<String, String> params, Map<String, String> headers) {
        //创建HttpClient 请求
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建 HttpPost
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(getRequestConfig());
        //创建上传文件的表单
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        //添加普通参数
        params.forEach((k, v) ->entityBuilder.addTextBody(k, v));
        //添加上传的文件参数
        file = new File("C:\\Users\\Administrator\\Downloads\\BGLQ23002F.pdf");
        entityBuilder.addPart("file", new FileBody(file, ContentType.MULTIPART_FORM_DATA,file.getName()));
        //添加headers
        headers.forEach((k, v) ->httpPost.addHeader(k,v));
        //设置编码
        //entityBuilder.setCharset(Charset.forName("UTF-8"));
        HttpEntity httpEntity = entityBuilder.build();
        httpPost.setEntity(httpEntity);
        //执行post 请求
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            httpEntity = response.getEntity();
            String res = EntityUtils.toString(httpEntity, Consts.ISO_8859_1);
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

    /**
     * 发送post请求form表单参数带File对象
     * @param url
     * @param params
     * @param files
     * @param headers
     * @return
     */
    public Pair<Integer, String> httpPost(String url, Map<String, String> params, Map<String, File> files,Map<String, String> headers) {
        //HttpPost请求实体
        HttpPost httpPost = new HttpPost(url);
        //使用工具类创建 httpClient
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse resp = null;
        String respondBody = null;
        try {
            //设置请求超时时间和 sockect 超时时间
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000000).build();
            httpPost.setConfig(requestConfig);
            //附件参数需要用到的请求参数实体构造器
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            if (!CollectionUtils.isEmpty(files)) {
                files.forEach((name, file) -> {
                    //附件参数,name对应请求参数的key值，file为文件
                    //添加文件参数，分隔符号会被自动设置，我们无须关注
                    multipartEntityBuilder.addBinaryBody(name,file);
                });
            }
            if (!CollectionUtils.isEmpty(params)) {
                params.forEach((key, value) -> {
                    //此处的字符串参数会被设置到请求体Query String Parameters中
                    multipartEntityBuilder.addTextBody(key, value);
                });
            }
            //添加headers
            headers.forEach((k, v) ->httpPost.addHeader(k,v));
            HttpEntity httpEntity = multipartEntityBuilder.build();
            //将请求参数放入 HttpPost 请求体中
            //使用 httpEntity 后 Content-Type会自动被设置成 multipart/form-data
            httpPost.setEntity(httpEntity);
            //执行发送post请求
            httpPost.setProtocolVersion(HttpVersion.HTTP_1_1);
            resp = client.execute(httpPost);
            byte[] byteData = EntityUtils.toByteArray(resp.getEntity());
            String s = new String(byteData,Charset.forName("ISO-8859-1"));
            respondBody = EntityUtils.toString(resp.getEntity());
        } catch (IOException | ParseException e) {
            //日志信息及异常处理
            String msg = "执行HTTP响应时抛出异常，需要关注";
            log.error(msg, e);
        } finally {
            if (resp != null) {
                try {
                    //关闭请求
                    resp.close();
                } catch (IOException e) {
                    log.error("关闭HTTP响应时抛出异常，需要关注", e);
                }
            }
        }
        return new ImmutablePair<>(resp.getStatusLine().getStatusCode(), respondBody);
    }
}

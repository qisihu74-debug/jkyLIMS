package com.stu.manage.demo.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.util
 * @desc
 * @date 2021/10/15 15:51
 * @Copyright © 河南交科院
 */
public class BaiduOrcUtils {
    /**
     * 获取权限token
     * @return 返回示例：
     * {
     * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
     * "expires_in": 2592000
     * }
     */
    public static String getAuth() {
        // 官网获取的 API Key 更新为你注册的
        String clientId = "i4a66fhj3eWkke3YS9cnthNf";
        // 官网获取的 Secret Key 更新为你注册的
        String clientSecret = "bYq5lcteHIGifAKllIAUdGFGU54fdyiw";
        return getAuth(clientId, clientSecret);
    }

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    public static String getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            InputStream inputStream = connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

            JSONObject jsonObject = JSONObject.parseObject(result);
            String access_token = jsonObject.getString("access_token");
            return access_token;
        } catch (Exception e) {
            System.err.printf("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }

    /**
     * 图文识别
     * @param in
     * @return
     */
    public static String getocrByInputStream(InputStream in){
        byte[] fileByte = getFileBytes(in);// 获取图片字节数组
        String base64UrlencodedImg = base64Urlencode(fileByte);// 编码
        return sendOcr(base64UrlencodedImg);// 发送给百度进行文字识别
    }

    /**
     * 传入base64 + UrlEncode 编码后的图片
     * 得到图片解析结果字符串（百度返回的Json）
     * @param base64UrlencodedImg
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String sendOcr(String base64UrlencodedImg){
        String token = getAuth();
        CloseableHttpClient httpclient = HttpClients.createMinimal();
        HttpPost post = new HttpPost("https://aip.baidubce.com/rest/2.0/ocr/v1/vat_invoice?access_token="+token);
        Header header = new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
        post.setHeader(header);
        try {
            HttpEntity entity = new StringEntity("image=" + base64UrlencodedImg);
            post.setEntity(entity);
            CloseableHttpResponse response = httpclient.execute(post);
            InputStream in = response.getEntity().getContent();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1000];
            int n;
            while ((n = in.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            in.close();
            bos.close();
            byte[] buffer = bos.toByteArray();

            return new String(buffer,"utf-8");
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片转字节数组
     * @param in 图片本地路径
     * @return 图片字节数组
     */
    private static byte[] getFileBytes(InputStream in){
        byte[] buffer = null;
        try {
            // File file = new File(filePath);
            // FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1000];
            int n;
            while ((n = in.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            in.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 对字节数组进行base64编码与url编码
     * @param b
     * @return
     */
    private static String base64Urlencode(byte[] b) {
        byte[] base64Img = Base64.getEncoder().encode(b);
        try {
            String base64UrlencodedImg = URLEncoder.encode(new String(base64Img), "utf-8");
            return base64UrlencodedImg;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * InputStream 转String
     * @param is
     * @return
     */
    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "/n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}

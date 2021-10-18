package com.stu.manage.demo.util;

import com.alibaba.fastjson.JSONObject;
import com.stu.manage.demo.entity.InvoiceEntity;
import com.stu.manage.demo.http.BaiduHttpUtil;
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
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     * @param clientId - 百度云官网获取的 API Key
     * @param clientSecret - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    public static String getAuth(String clientId, String clientSecret,String authHost) {
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + clientId
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + clientSecret;
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
    public static String getocrByInputStream(InputStream in,String clientId,String clientSecret,String authHost,String invoiceUrl){
        byte[] fileByte = getFileBytes(in);// 获取图片字节数组
        String base64UrlencodedImg = base64Urlencode(fileByte);// 编码
        return sendOcr(base64UrlencodedImg,clientId,clientSecret,authHost,invoiceUrl);// 发送给百度进行文字识别
    }

    /**
     * 传入base64 + UrlEncode 编码后的图片
     * 得到图片解析结果字符串（百度返回的Json）
     * @param base64UrlencodedImg
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String sendOcr(String base64UrlencodedImg,String clientId,String clientSecret,String authHost,String invoiceUrl){
        String token = getAuth(clientId,clientSecret,authHost);
        CloseableHttpClient httpclient = HttpClients.createMinimal();
        HttpPost post = new HttpPost(invoiceUrl+token);
        Header header = new BasicHeader("Content-Type","application/x-www-form-urlencoded");
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
     * 校验发票真伪
     * @param invoice
     * @param clientId
     * @param clientSecret
     * @param authHost
     * @param invoiceVerificationUrl
     * @return
     */
    public static Boolean checkInvoice(InvoiceEntity invoice, String clientId, String clientSecret,
                                       String authHost, String invoiceVerificationUrl) {
        String token = getAuth(clientId,clientSecret,authHost);
        //设置参数
        String time = invoice.getInvoiceDate().replace("年", "").replace("月", "").replace("日", "");
        String param = "invoice_code=" + invoice.getInvoiceCode() + "&invoice_num=" + invoice.getInvoiceNum()
                + "&invoice_date=" + time + "&check_code=" + invoice.getCheckCode().substring(invoice.getCheckCode().length()-6)
                + "&invoice_type=" + "client_credentials" + "&total_amount=" + invoice.getAmountInFiguers();
        try {
            String post = BaiduHttpUtil.post(invoiceVerificationUrl, token, param);
            JSONObject jsonObject = JSONObject.parseObject(post);
            System.out.println("===");
        }catch(Exception e){
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

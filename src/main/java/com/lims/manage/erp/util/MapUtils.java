package com.lims.manage.erp.util;


import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2021/12/10 14:22
 * @Copyright © 河南交科院
 */
public class MapUtils {
    /**
     * 百度地图请求秘钥
     */
    private static final String KEY = "D5Dxj5YjpYW3CPcZqAKIRQxDroTZfrlg";
    /**
     * 返回值类型
     */
    private static final String OUTPUT = "json";
    /**
     * 根据地名获取经纬度
     */
    private static final String GET_LNG_LAT_URL = "http://api.map.baidu.com/geocoder/v2/";
    /**
     * 根据经纬度获取地名
     */
    private static final String GET_ADDRESS_URL = "http://api.map.baidu.com/geocoder/v2/";



    /**
     * 根据经纬度获得省市区信息
     * @param lng 纬度
     * @param lat 经度
     * @param coordtype 经纬度坐标系
     * @return
     */
    public static Map<String, String> getCityByLonLat(double lng, double lat, String coordtype) {
        String location = lat + "," + lng;
        Map<String, String> params = new HashMap<>();
        params.put("location", location);
        params.put("coordtype", coordtype);
        try {
            //拼装url
            String url = joinUrl(params, OUTPUT, KEY, GET_ADDRESS_URL);
            JSONObject result = JSONObject.parseObject(JSONObject.parseObject(JSONObject.parseObject(HttpClientUtils.doGet(url)).
                    getString("result")).getString("addressComponent"));
            Map<String, String> area = new HashMap<>();
            area.put("province", result.getString("province"));
            area.put("city", result.getString("city"));
            area.put("district", result.getString("district"));
            return area;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 拼接url字符串
     * @param params
     * @param output
     * @param key
     * @param url
     * @return
     * @throws IOException
     */
    private static String joinUrl(Map<String, String> params, String output, String key, String url) throws IOException {
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(url);

        int index = 0;
        Set<Map.Entry<String, String>> entrys = params.entrySet();
        for (Map.Entry<String, String> param : entrys) {
            // 判断是否是第一个参数
            if (index == 0) {
                baseUrl.append("?");
            } else {
                baseUrl.append("&");
            }
            baseUrl.append(param.getKey()).append("=").append(URLEncoder.encode(param.getValue(), "utf-8"));
            index++;
        }
        baseUrl.append("&output=").append(output).append("&ak=").append(key);

        return baseUrl.toString();
    }
}

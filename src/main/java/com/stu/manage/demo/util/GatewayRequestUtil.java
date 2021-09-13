
package com.stu.manage.demo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.xxpt.gateway.shared.client.http.ExecutableClient;
import com.alibaba.xxpt.gateway.shared.client.http.PostClient;
import com.stu.manage.demo.result.GatewayResult;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 宜搭网关服务请求工具
 *
 * @Author: weimeng(shanyu)
 * @Date: 2019/2/28 下午2:50
 */

public class GatewayRequestUtil {
    /**
     * 请求网关
     *
     * @param param 请求参数
     * @param url   网关地址
     * @return 请求结果
     */
    public static GatewayResult baseRequest(Map<String, String> param, String url) {
        try {
            PostClient postClient = ExecutableClient.getInstance().newPostClient(url);
            if (!CollectionUtils.isEmpty(param)) {
                for (Map.Entry<String, String> entry : param.entrySet()) {
                    postClient.addParameter(entry.getKey(), entry.getValue());
                }
            }

            String result = postClient.post();
            return JSON.parseObject(result, GatewayResult.class);
        } catch (Throwable e) {
            e.printStackTrace();
            GatewayResult result = new GatewayResult();
            result.setSuccess(false);
            return result;
        }
    }
}
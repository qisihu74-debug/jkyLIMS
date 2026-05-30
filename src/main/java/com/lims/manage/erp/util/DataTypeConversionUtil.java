package com.lims.manage.erp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2024-09-11 14:29
 * @Copyright © 河南交科院
 */
public class DataTypeConversionUtil {
    private static final Logger logger = LoggerFactory.getLogger(DataTypeConversionUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将JSON格式的字符串转换为Map对象。
     * @param jsonString JSON格式的字符串
     * @return 转换后的Map对象
     */
    public static Map<String, Object> getStringToMap(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class));
        } catch (IOException e) {
            logger.error("Error parsing JSON string to Map: {}", jsonString, e);
            return null;
        }
    }
}

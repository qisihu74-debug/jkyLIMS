package com.lims.manage.erp.util;

import org.apache.commons.lang.RandomStringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.util
 * @desc
 * @date 2021/8/30 9:49
 * @Copyright © 河南交科院
 */
public class GenID {
    static AtomicInteger atomicInteger = new AtomicInteger(100);

    public synchronized static long getID() {
        int increment = atomicInteger.getAndIncrement();
        String s = null;
        if(increment<999999) {
            s = System.currentTimeMillis() + "" + increment;
        }else {
            atomicInteger.set(0);
            increment = atomicInteger.getAndIncrement();
            s = System.currentTimeMillis() + "" + increment;
        }
        long id = Long.parseLong(s);
        return id;
    }

    public static String getUUID() {
        String s = UUID.randomUUID().toString();

        return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)
                + s.substring(19, 23) + s.substring(24);
    }

    /**
     * 设置String类型 依据时间生成的主键
     * @return
     */
    public static String getOrderNum() {
        //时间（精确到毫秒）
        DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String localDate = LocalDateTime.now().format(ofPattern);
        //3位随机数
        String randomNumeric = RandomStringUtils.randomNumeric(3);
        String orderNum = localDate + randomNumeric;
        return orderNum;
    }

    public static void main(String[] args) {
        System.out.println(GenID.getUUID().length());
    }
}

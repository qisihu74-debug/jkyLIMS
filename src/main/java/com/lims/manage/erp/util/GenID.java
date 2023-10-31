package com.lims.manage.erp.util;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
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
    private static final long EPOCH = 1659323575416L; //开始时间,固定一个小于当前时间的毫秒数
    private static final int max12bit = 4095;
    private static final long max41bit= 1099511627775L;
    private static String machineId = "" ; // 机器ID
    private static AtomicInteger counter = new AtomicInteger(0);
    public synchronized static long getID() {
        long time = System.currentTimeMillis() - EPOCH  + max41bit;
        // 二进制的 毫秒级时间戳
        String base = Long.toBinaryString(time);

        // 序列数
        String randomStr = StringUtils.leftPad(Integer.toBinaryString(new Random().nextInt(max12bit)),12,'0');
        if(StringUtils.isNotEmpty(machineId)){
            machineId = StringUtils.leftPad(machineId, 10, '0');
        }

        //拼接
        String appendStr = base + machineId + randomStr;
        // 转化为十进制 返回
        BigInteger bi = new BigInteger(appendStr, 2);
        return  Long.valueOf(bi.toString());
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

    public static void main(String[] args) throws InterruptedException {
        for (int i=0;i<10000;i++) {
            Thread.sleep(1000);
            System.out.println(GenID.getID());
            i++;
        }

    }

    /**
     * int id生成器
     * @return
     */
    public static int generateId() {
        return counter.incrementAndGet();

    }
}

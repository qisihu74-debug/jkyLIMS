package com.lims.manage.erp.util;

import org.apache.commons.lang.RandomStringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
    private static AtomicInteger atomicInteger = new AtomicInteger(0);
    private static int ID_LENGTH = 16;

    public synchronized static long getID() {
        //  生成最大4位随技术
        int i2 = ThreadLocalRandom.current().nextInt(9999);
        String timeStr = String.valueOf(System.currentTimeMillis());
        // 取出时间串前面相同的部分
        timeStr = timeStr.substring(5);
        // 递增生成最大9999的递增ID
        if (atomicInteger.get() == 9999) {
            atomicInteger.set(0);
        }
        int i1 = atomicInteger.getAndIncrement();
        String id = timeStr.concat(String.valueOf(i2)).concat(i1+"");
        // 严格控制ID长度，如果过长 从最前面截取
        if (id.length() > ID_LENGTH) {
            // 计算多了多少位
            int surplusLenth = id.length() - ID_LENGTH;
            id = id.substring(surplusLenth);
        }
        return Long.valueOf(id);
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
}

package com.stu.manage.demo.crm;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.crm
 * @desc
 * @date 2021/9/22 17:31
 * @Copyright © 河南交科院
 */
public class TST {
    public static void main(String[] args) {
        String code = "9f7c9f9145d148678296a1d2c62b2511";
        char[] chars = code.toCharArray();
        if ((chars.length & 1) != 0) {
            System.out.println("err");
        }else {
            System.out.println("ok");
        }
    }
}

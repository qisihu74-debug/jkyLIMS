package com.lims.manage.erp.crm;

import java.util.Random;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.crm
 * @desc
 * @date 2021/9/22 17:31
 * @Copyright © 河南交科院
 */
public class TST {
    public static void main(String[] args) throws Exception {
        for (int i =0;i<100;i++){
           System.out.println("============: "+genRandomNum());
        }
    }

    public static String genRandomNum() {
        int maxNum = 36;
        int i;
        int count = 0;
        char[] str = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        StringBuffer pwd = new StringBuffer("");
        Random r = new Random();
        while (count < 8) {
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
        }
        return pwd.toString();
    }
}

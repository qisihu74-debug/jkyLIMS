package com.lims.manage.erp.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.util
 * @desc
 * @date 2021/9/23 9:25
 * @Copyright © 河南交科院
 */
public class DESUtils {
    /**
     * 加密
     *
     * @param inStr     需要加密的内容
     * @param secretKey 密钥（长度必须为24位）
     * @return 加密后的数据
     */

    public static String encrypt(String inStr, String secretKey) {
        SecretKey deskey = new SecretKeySpec(secretKey.getBytes(), "DESede");
        Cipher cipher;
        String outStr = null;
        try {
            cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.ENCRYPT_MODE, deskey);
            outStr = byte2hex(cipher.doFinal(inStr.getBytes()));
        } catch (Exception e) {
            System.err.println("3DES加密异常"+ e.getMessage());
        }
        return outStr;

    }

    /**
     * 解密
     *
     * @param inStr     需要解密的内容
     * @param secretKey 密钥（长度必须为24位）
     * @return 解密后的数据
     */

    public static String decrypt(String inStr, String secretKey) {
        SecretKey deskey = new SecretKeySpec(secretKey.getBytes(), "DESede");
        Cipher cipher;
        String outStr = null;
        try {
            cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.DECRYPT_MODE, deskey);
            outStr = new String(cipher.doFinal(hex2byte(inStr)));
        } catch (Exception e) {
            System.err.println("3DES解密异常"+e.getMessage());
        }
        return outStr;

    }

    /**
     * 转化为16进制字符串方法
     *
     * @param digest 需要转换的字节组
     * @return 转换后的字符串
     */

    private static String byte2hex(byte[] digest) {

        StringBuffer hs = new StringBuffer();

        String stmp = "";

        for (int n = 0; n < digest.length; n++) {

            stmp = Integer.toHexString(digest[n] & 0XFF);

            if (stmp.length() == 1) {

                hs.append("0" + stmp);

            } else {

                hs.append(stmp);

            }

        }

        return hs.toString().toUpperCase();

    }

    /**
     * 十六进转二进制
     *
     * @param hexStr 待转换16进制字符串
     * @return 二进制字节组
     */

    public static byte[] hex2byte(String hexStr) {

        if (hexStr == null)

            return null;

        hexStr = hexStr.trim();

        int len = hexStr.length();

        if (len == 0 || len % 2 == 1)

            return null;

        byte[] digest = new byte[len / 2];

        try {

            for (int i = 0; i < hexStr.length(); i += 2) {

                digest[i / 2] = (byte) Integer.decode("0x" + hexStr.substring(i, i + 2)).intValue();

            }
            return digest;

        } catch (Exception e) {

            return null;

        }

    }

    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }

    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }


}

package com.lims.manage.erp.util;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * @ClassName： AESUtil.java
 * @ClassPath： com.tansci.util.AESUtil.java
 * @Description： AES对称加解密工具类
 * @Author： tanyp
 * @Date： 2024/4/18 12:00
 **/
public class AESUtils {

    /**
     * @Description： 加密
     **/
    public static String encrypt(String content, String key) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"));
        byte[] b = cipher.doFinal(content.getBytes("utf-8"));
        return Hex.encodeHexString(b);
    }

    /**
     * @Description： 解密
     **/
    public static String decrypt(String encryptStr, String key) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"));
        byte[] encrypttBytes = Hex.decodeHex(encryptStr);
        byte[] decryptBytes = cipher.doFinal(encrypttBytes);
        return new String(decryptBytes);
    }

    public static void main(String[] args) {
        String content = "https://hntri.lims.design/?cardNum=123456789&deviceId=2001";
        String key = "MEqLCnG2Q0IfauMDbZq1lP46uP4BHsiv";

        String encrypt = null;
        try {
            encrypt = encrypt(content, key);
            System.out.println("加密后的数据:"+encrypt);
            String decrypt = decrypt(encrypt, key);
            System.out.println("解密后的数据:"+decrypt);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

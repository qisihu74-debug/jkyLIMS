package com.lims.manage.erp.crm;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.crm
 * @desc
 * @date 2024-03-05 10:08
 * @Copyright © 河南交科院
 */
public class AesUtil {
    public static void main(String[] args) throws Exception {
        //秘钥key 加解密使用
        String key = "KnxXcadsri6l1Q490SVnQA==";
        byte[] decodedKey = Base64.getDecoder().decode(key);
        // 生成密钥
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        // 加密
        String plainText = "https://hntri.lims.design?cardNum=123456789&deviceId=2001&taskCode=N1001";
        String encryptedText = encrypt(plainText, secretKey);
        System.out.println("加密后的文本：" + encryptedText);
        // 解密
        String decryptedText = decrypt(encryptedText, secretKey);
        System.out.println("解密后的文本：" + decryptedText);
    }

    /**
     * 加密
     * @param plainText
     * @param secretKey
     * @return
     * @throws Exception
     */
    private static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        //使用Cipher类的getInstance方法获取一个AES加密算法的实例。这里指定了加密算法为"AES"，
        // 但没有明确指定AES的工作模式（例如AES/ECB/PKCS5Padding）。
        Cipher cipher = Cipher.getInstance("AES");
        //使用init方法初始化cipher对象，设置其为加密模式（Cipher.ENCRYPT_MODE）并使用给定的secretKey
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        //将plainText字符串转换为UTF-8编码的字节数组。
        //使用cipher对象的doFinal方法加密这些字节，并将加密后的字节存储在encryptedBytes字节数组中
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        //使用Java的Base64编码器将加密后的字节数组encryptedBytes转换为Base64编码的字符串。
        //返回这个Base64编码的字符串
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 解密
     * @param encryptedText
     * @param secretKey
     * @return
     * @throws Exception
     */
    private static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}


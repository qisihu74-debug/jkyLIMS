package com.stu.manage.demo.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.util
 * @desc
 * @date 2021/8/27 15:44
 * @Copyright © 河南交科院
 */
public class CryptoUtil {
    public static Logger logger = LoggerFactory.getLogger(CryptoUtil.class);
    public static Key DEFAULT_KEY = null;

    public static final String DEFAULT_SECRET_KEY = "1qaz2wsx3edc$RFV%TGB^YHN&UJM";

    public static final String DES = "DES";

    static {
        DEFAULT_KEY = obtainKey(DEFAULT_SECRET_KEY);
    }

    /**
     * 获得key
     **/
    public static Key obtainKey(String key) {
        String charset = "utf-8";
        if (key == null) {
            return DEFAULT_KEY;
        }
        KeyGenerator generator = null;
        try {
            generator = KeyGenerator.getInstance(DES);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bytes = null;
        try {
            bytes = key.getBytes(charset);
        }catch (UnsupportedEncodingException e){
            logger.error("编码不支持的异常:{}",e);
        }

        generator.init(new SecureRandom(bytes));
        Key key1 = generator.generateKey();
        generator = null;
        return key1;
    }

    /**
     * 加密<br>
     * String明文输入,String密文输出
     */
    public static String encode(String str) {
        return encode(null, str);
    }

    /**
     * 加密<br>
     * String明文输入,String密文输出
     */
    public static String encode(String key, String str) {
        byte[] bytes = null;
        try {
            bytes = str.getBytes("utf-8");
        }catch (UnsupportedEncodingException e){
            logger.error("编码不支持的异常:{}",e);
        }
        return Base64.encodeBase64URLSafeString(obtainEncode(key, bytes));
    }

    /**
     * 解密<br>
     * 以String密文输入,String明文输出
     */
    public static String decode(String str) {
        return decode(null, str);
    }

    /**
     * 解密<br>
     * 以String密文输入,String明文输出
     */
    public static String decode(String key, String str) {
        return new String(obtainDecode(key, Base64.decodeBase64(str)));
    }

    /**
     * 加密<br>
     * 以byte[]明文输入,byte[]密文输出
     */
    private static byte[] obtainEncode(String key, byte[] str) {
        byte[] byteFina = null;
        Cipher cipher;
        try {
            Key key1 = obtainKey(key);
            cipher = Cipher.getInstance(DES);
            cipher.init(Cipher.ENCRYPT_MODE, key1);
            byteFina = cipher.doFinal(str);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cipher = null;
        }
        return byteFina;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * 解密<br>
     * 以byte[]密文输入,以byte[]明文输出
     */
    private static byte[] obtainDecode(String key, byte[] str) {
        Cipher cipher;
        byte[] byteFina = null;
        try {
            Key key1 = obtainKey(key);
            cipher = Cipher.getInstance(DES);
            cipher.init(Cipher.DECRYPT_MODE, key1);
            byteFina = cipher.doFinal(str);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cipher = null;
        }
        return byteFina;
    }
}

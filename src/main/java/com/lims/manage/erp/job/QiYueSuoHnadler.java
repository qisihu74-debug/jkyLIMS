package com.lims.manage.erp.job;

import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.http.HttpClientUtil;
import com.lims.manage.erp.http.HttpResponse;
import com.lims.manage.erp.util.GenID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.job
 * @desc
 * @date 2022/2/23 10:09
 * @Copyright © 河南交科院
 */
@Slf4j
@Component
public class QiYueSuoHnadler {
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;

    /**
     * 根据文件类型创建合同文档
     * @param file (必传) pdf报告文件
     * @param title (必传)名称
     * @param fileType (必传)文件类型：doc, docx, txt, pdf, png, gif, jpg, jpeg, tiff, html, rtf, xls, txt
     * @param waterMarks （非必传）水印，json格式字符串；如：[{"content":"水印1","fontSize":"30","location":"UPPER_LEFT","imageBase64":"/9j/4AAQSkZJRgABAQEASABIAAD/4gxY"},{"content":"水印2","fontSize":"30","location":"LOWER_LEFT"}]
     * @param width （非必传）宽（文件尺寸，单位：mm）
     * @param height （非必传）高（文件尺寸，单位：mm）
     * @return
     */
    public HttpResponse creatFile(File file,String title,String fileType,
                                  String waterMarks,Float width,Float height){
        //请求参数设置
        Map<String, String> params = new HashMap<>();
        params.put("file",file.toString());
        params.put("title",title);
        params.put("fileType",fileType);
        //headers参数设置（MD5进行加密，token加密秘钥 Md5(appToken+appSercert+timestamp+nonce)timestamp当前系统时间不允许偏差超过15分钟、nonce取值UUID）
        Map<String,String> headers = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder(100);
        stringBuilder.append(qiYueSuoEntity.getAppToken());
        stringBuilder.append(qiYueSuoEntity.getAppSecret());
        stringBuilder.append(0);
        stringBuilder.append(GenID.getUUID());
        String md5Str = getMD5Str(stringBuilder.toString());
        headers.put("token",md5Str);
        //请求契约锁接口（请求方式由契约锁接口约定）
        Pair<Integer, String> form = HttpClientUtil.postForm(qiYueSuoEntity.getUrl(), params, headers);
        return null;
    }

    /**
     * md5加密
     * @param str
     * @return
     */
    public String getMD5Str(String str) {
        byte[] digest = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digest  = md5.digest(str.getBytes("utf-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //16是表示转换为16进制数
        String md5Str = new BigInteger(1, digest).toString(16);
        return md5Str;
    }

}

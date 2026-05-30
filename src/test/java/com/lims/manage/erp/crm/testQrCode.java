package com.lims.manage.erp.crm;

import com.google.zxing.WriterException;
import com.lims.manage.erp.util.QRCodeUtils;

import java.io.IOException;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.crm
 * @desc
 * @date 2023-08-25 16:24
 * @Copyright © 河南交科院
 */
public class testQrCode {
    public static void main(String[] args) {
        String content = "1651465";
        String imgPath = "D:\\Users\\Administrator\\Desktop\\logo.jpg";
        String desPath="D:\\Download\\1213.png";
        try {
            QRCodeUtils.encode(content,imgPath,desPath,false);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}

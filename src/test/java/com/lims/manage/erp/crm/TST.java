package com.lims.manage.erp.crm;

import com.lims.manage.erp.entity.ImagePro;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.util.ImageToPdfUtils;
import com.lims.manage.erp.vo.EntrustAddVo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.crm
 * @desc
 * @date 2021/9/22 17:31
 * @Copyright © 河南交科院
 */
public class TST {
    public static void main(String[] args) {
        try {
            //ImageToPdfUtils.insertImageToPdf("D:\\AAAct\\ZX-2021-SW-1471.pdf","D:\\AAAct\\tupian.pdf","D:\\AAAct\\tupian.jpg");
            List<ImagePro> imagePros = new ArrayList<>();
            imagePros.add(new ImagePro(130, 48, 15, "D:\\\\AAAct\\\\tupian.jpg"));
            ImageToPdfUtils.writeToPdf("D:\\AAAct\\ZX-2021-SW-1471.pdf","D:\\AAAct\\tupian.pdf",imagePros);
        }catch (Exception e){
            System.out.println("图片导入pdf成功!");
        }


    }
}

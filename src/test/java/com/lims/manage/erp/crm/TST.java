package com.lims.manage.erp.crm;

import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.vo.EntrustAddVo;

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
        String reportUrl = "http://192.168.2.35:9000/seal-cns-cma/cns.jpg";
        String image = reportUrl.substring(reportUrl.lastIndexOf("/")+1);
        System.out.println("==========="+image);
    }
}

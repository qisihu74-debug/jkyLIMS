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
        EntrustAddVo entrustAddVo = new EntrustAddVo();
        entrustAddVo.setAcceptanceDate(new Date(System.currentTimeMillis()));
        entrustAddVo.setBusinessAcceptor("111");
        entrustAddVo.setCheckPurpose("111");
        entrustAddVo.setEntrustCompany("111");
        entrustAddVo.setEntrustPeople("李四");
        entrustAddVo.setEntrustType("www");
        entrustAddVo.setIsSave("11");
        entrustAddVo.setPaymentMethod("3w2q3e");
    }
}

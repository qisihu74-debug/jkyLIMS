package com.stu.manage.demo.service;

import com.stu.manage.demo.entity.EntrustInfo;
import com.stu.manage.demo.entity.JtEntrustInfo;
import com.stu.manage.demo.entity.ProductVo;
import com.stu.manage.demo.entity.jtEntrustType;

import java.util.List;
import java.util.Map;

public interface EntrustService {
    /**
     * 再来一单--委托基本信息
     * @param entrustId
     * @return
     */
    EntrustInfo onceMore(int entrustId);

    /**
     * 实现add 用户委托基本信息
     */
    Integer addEntrust(JtEntrustInfo jtEntrustInfo);

    Integer addEntrustInfo(Map<String,Object> map);

    Map<String, List<ProductVo>> getSelectLists();
}

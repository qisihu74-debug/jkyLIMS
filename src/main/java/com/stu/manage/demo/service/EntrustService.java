package com.stu.manage.demo.service;

import com.stu.manage.demo.entity.EntrustInfo;

public interface EntrustService {
    /**
     * 再来一单--委托基本信息
     * @param entrustId
     * @return
     */
    EntrustInfo onceMore(int entrustId);
}

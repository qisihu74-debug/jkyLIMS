package com.stu.manage.demo.service;

import com.stu.manage.demo.entity.EntrustInfo;
import com.stu.manage.demo.entity.StatusEntity;

public interface EntrustService {
    /**
     * 再来一单--委托基本信息
     * @param entrustId
     * @return
     */
    EntrustInfo onceMore(int entrustId);

    /**
     * 查询委托单状态
     * @param id
     * @return
     */
    StatusEntity status(Integer id);
}

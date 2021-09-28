package com.stu.manage.demo.service;

import com.stu.manage.demo.entity.CheckItemCostVo;
import com.stu.manage.demo.entity.EntrustInfo;
import com.stu.manage.demo.entity.JtEntrustInfo;
import com.stu.manage.demo.entity.ProductVo;
import com.stu.manage.demo.entity.StatusEntity;

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
     * 根据产品获取产品全部检测项
     * @param productId
     * @return
     */
    List<CheckItemCostVo> getCheckItemsByProductId(int productId);

    /**
     * 实现add 用户委托基本信息
     */
    Integer addEntrust(JtEntrustInfo jtEntrustInfo);

    Integer addEntrustInfo(Map<String,Object> map);

    Map<String, List<ProductVo>> getSelectLists();
    /**
     * 根据产品获取产品检测依据
     * @param productId
     * @return
     */
    List<ProductVo> getCheckBasisByProductId(int productId);

    /**
     * 查询委托单状态
     * @param id
     * @return
     */
    StatusEntity status(Integer id);

    /**
     * 发送消息
     */
    List<String> sendMessage();

    /**
     * 我的委托列表
     * @param type
     * @param userId
     * @param adminId
     * @param startTime
     * @param endTime
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<StatusEntity> ownerTask(String type, Integer userId, String adminId, String startTime, String endTime, Integer pageNo, Integer pageSize);
}

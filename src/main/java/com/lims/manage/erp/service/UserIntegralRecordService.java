package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.UserIntegralRecord;
import com.lims.manage.erp.result.Result;

/**
 * 用户获取积分记录业务层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface UserIntegralRecordService extends IService<UserIntegralRecord> {

    /**
     * 获取用户当前签到状态
     * @return Result
     */
    Result<?> getSignInStatus();

    /**
     * 用户进行签到
     * @return Result
     */
    Result<?> signIn();

    /**
     * 添加用户积分和获取积分记录
     * @param ruleId 规则id
     * @param ruleCacheId  规则缓存id
     * @param userId 用户id
     * @param eventType 事件类型查看事件类型常量
     * @param eventId 事件id
     * @return Integer 返回类型(0:没有对应规则;1:添加成功;2:超过频次)
     */
    Integer addUserIntegralRecord(String ruleId,String ruleCacheId,String userId,String eventType,String eventId);
}

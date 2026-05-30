package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.IntegralInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.ShareUserInfoVo;
import com.lims.manage.erp.vo.UserIntegralRankingListVo;

/**
 * 积分对应称号信息业务层接口
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface IntegralInfoService extends IService<IntegralInfo> {

    /**
     * 获取用户积分信息(称号和徽章)
     *
     * @return Result
     */
    Result<?> getUserIntegralInfo();

    /**
     * 获取用户积分排行榜信息
     *
     * @param page         分页参数
     * @param integralType 积分类型
     * @return IPage<UserIntegralRankingListVo>
     */
    IPage<UserIntegralRankingListVo> getIntegralRankingList(Page page, String integralType);

    /**
     * 获取分享者简介
     * @param userId 分享者Id
     * @return ShareUserInfoVo
     */
    ShareUserInfoVo getShareUserInfo(String userId);
}

package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.IntegralInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.ShareUserInfoVo;
import com.lims.manage.erp.vo.UserIntegralRankingListVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 积分对应称号信息dao层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface IntegralInfoDao extends BaseMapper<IntegralInfo> {

    /**
     * 获取用户积分信息(称号和徽章)
     * @param userId 用户id
     * @return IntegralInfo
     */
    List<IntegralInfo> getUserIntegralInfo(@Param("userId")String userId);

    /**
     * 获取用户积分排行榜
     * @param page 分页参数
     * @param integralType 积分类型
     * @return IPage<UserIntegralRankingListVo>
     */
    IPage<UserIntegralRankingListVo> getIntegralRankingList(Page page, @Param("integralType") String integralType);

    /**
     * 获取分享者简介
     * @param auditStatus 审核状态
     * @param userId 用户id
     * @return Result
     */
    ShareUserInfoVo getShareUserInfo(@Param("auditStatus") Integer auditStatus, @Param("userId")String userId);
}

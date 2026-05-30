package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.CommonConstant;
import com.lims.manage.erp.entity.IntegralInfo;
import com.lims.manage.erp.mapper.IntegralInfoDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.IntegralInfoService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.ShareUserInfoVo;
import com.lims.manage.erp.vo.UserIntegralRankingListVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 积分对应称号信息业务层实现类
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class IntegralInfoServiceImpl extends ServiceImpl<IntegralInfoDao, IntegralInfo> implements IntegralInfoService {
    @Override
    public Result<?> getUserIntegralInfo() {
        //根据用户id获取用户积分信息和对应的称号徽章
        List<IntegralInfo> integralInfoList=baseMapper.getUserIntegralInfo(ShiroUtils.getUserInfo().getUserId().toString());
        return ResultUtil.success("获取用户称号与徽章信息",integralInfoList);
    }

    @Override
    public IPage<UserIntegralRankingListVo> getIntegralRankingList(Page page, String integralType) {
        return baseMapper.getIntegralRankingList(page,integralType);
    }

    @Override
    public ShareUserInfoVo getShareUserInfo(String userId) {
        return baseMapper.getShareUserInfo(CommonConstant.AUDIT_STATUS_1,userId);
    }
}

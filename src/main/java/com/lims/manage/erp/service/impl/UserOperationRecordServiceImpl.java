package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.UserOperationRecord;
import com.lims.manage.erp.mapper.UserOperationRecordDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.UserOperationRecordService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.BrowseFootstepsVo;
import com.lims.manage.erp.vo.UserOperationStatusVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户动态操作记录业务层实现类
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class UserOperationRecordServiceImpl extends ServiceImpl<UserOperationRecordDao, UserOperationRecord> implements UserOperationRecordService {

    @Override
    public UserOperationStatusVo getUserOperationStatus(UserOperationRecord userOperationRecord) {
        userOperationRecord.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
        return baseMapper.getUserOperationStatus(userOperationRecord);
    }

    @Override
    public IPage<BrowseFootstepsVo> getBrowseFootsteps(Page<BrowseFootstepsVo> page) {
        return baseMapper.getBrowseFootsteps(page,ShiroUtils.getUserInfo().getUserId().toString());
    }

    @Override
    public IPage<BrowseFootstepsVo> getUserLikeList(Page<BrowseFootstepsVo> page) {
        return baseMapper.getUserLikeList(page,ShiroUtils.getUserInfo().getUserId().toString());
    }

    @Override
    public IPage<BrowseFootstepsVo> getUserCollectList(Page<BrowseFootstepsVo> page) {
        return baseMapper.getUserCollectList(page,ShiroUtils.getUserInfo().getUserId().toString());
    }
}

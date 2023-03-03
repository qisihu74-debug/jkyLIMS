package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.UserOperationRecord;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.BrowseFootstepsVo;
import com.lims.manage.erp.vo.UserOperationStatusVo;

/**
 * 用户动态操作记录业务层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface UserOperationRecordService extends IService<UserOperationRecord> {

    /**
     * 根据事件类型和事件id获取用户的操作状态
     * @param userOperationRecord 事件类型及id
     * @return UserOperationStatusVo
     */
    UserOperationStatusVo getUserOperationStatus(UserOperationRecord userOperationRecord);

    /**
     * 获取浏览足迹
     * @param page 分页参数
     * @return IPage<BrowseFootstepsVo>
     */
    IPage<BrowseFootstepsVo> getBrowseFootsteps(Page<BrowseFootstepsVo> page);

    /**
     * 获取点赞列表
     * @param page 分页参数
     * @return IPage<BrowseFootstepsVo>
     */
    IPage<BrowseFootstepsVo> getUserLikeList(Page<BrowseFootstepsVo> page);

    /**
     * 获取用户收藏列表
     * @param page 分页参数
     * @return IPage<BrowseFootstepsVo>
     */
    IPage<BrowseFootstepsVo> getUserCollectList(Page<BrowseFootstepsVo> page);
}

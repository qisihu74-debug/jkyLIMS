package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.UserOperationRecord;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.BrowseFootstepsVo;
import com.lims.manage.erp.vo.UserOperationStatusVo;
import org.apache.ibatis.annotations.Param;

/**
 * 用户动态操作记录dao层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface UserOperationRecordDao extends BaseMapper<UserOperationRecord> {

    /**
     * 根据事件类型和事件id获取用户的操作状态
     * @param userOperationRecord 事件类型及id
     * @return UserOperationStatusVo
     */
    UserOperationStatusVo getUserOperationStatus(@Param("userOperationRecord") UserOperationRecord userOperationRecord);

    /**
     * 获取浏览足迹
     * @param page 分页参数
     * @param userId 用户id
     * @return IPage<BrowseFootstepsVo>
     */
    IPage<BrowseFootstepsVo> getBrowseFootsteps(Page<BrowseFootstepsVo> page,@Param("userId")String userId);

    /**
     * 获取用户点赞列表
     * @param page 分页参数
     * @param userId 用户id
     * @return IPage<BrowseFootstepsVo>
     */
    IPage<BrowseFootstepsVo> getUserLikeList(Page<BrowseFootstepsVo> page,@Param("userId")String userId);

    /**
     * 获取用户点赞列表
     * @param page 分页参数
     * @param userId 用户id
     * @return IPage<BrowseFootstepsVo>
     */
    IPage<BrowseFootstepsVo> getUserCollectList(Page<BrowseFootstepsVo> page,@Param("userId")String userId);
}

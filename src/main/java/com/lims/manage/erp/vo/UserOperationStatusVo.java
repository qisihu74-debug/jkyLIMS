package com.lims.manage.erp.vo;

import lombok.Data;
import lombok.Getter;

/**
 * 用户操作状态
 * @author: zhq
 * @date: 2023-01-29
 * @version: v1.0
 */
@Data
public class UserOperationStatusVo {
    /**
     * 用户点赞状态
     */
    private Integer likeStatus;

    /**
     * 用户点踩状态
     */
    private Integer tapStatus;

    /**
     * 用户收藏状态
     */
    private Integer collectStatus;

    /**
     * 用户完成状态
     */
    private Integer completeStatus;
}

package com.lims.manage.erp.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 用户积分排行榜信息
 *
 * @author : zhq
 * @version : V1.0
 * @date :   2023-01-03
 */
@Data
public class UserIntegralRankingListVo {

    /**
     * 排行名次
     */
    private Integer rowNum;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户积分
     */
    private Integer integralNum;

    /**
     * 用户称号
     */
    private String integralTitle;

    /**
     * 字典项排序
     */
    private String deptName;
}

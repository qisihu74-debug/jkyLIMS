package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/6 11:48
 * 判定依据集合
 */
@Data
public class JudgmentBasisVo {

    /**
     * 委托单id
     */
    private Long id;
    /**
     * 样品id
     */
    private Integer sampleId;
    /**
     * 依据名称
     */
    private String standardName;
    /**
     * 依据id
     */
    private Integer standardId;
    /**
     * 检测名称
     */
    private String checkItemName;
    /**
     * 检测id
     */
    private Integer checkItemId;
    /**
     * 检测次数
     */
    private Integer times;
    /**
     * 检测单价
     */
    private String checkPrice;
    /**
     * 总价
     */
    private String totalPrice;
    /**
     * 方法id
     */
    private Integer methodId;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 检测室
     */
    private String testingRoom;
    /**
     * 检测项全部的检测依据
     */
    private List<LabelValueVo> checkBasisList;
    /**
     * 可做gai
     */
    private List<LabelValueVo> testingRoomList;
    /**
     * 检测项pid
     */
    private Integer checkItemPid;
    /**
     * 任务ID
     */
    private Long taskId;
    /**
     * 详情检测项-展示推荐团队
     */
    private Integer recommendTheTeam;
}

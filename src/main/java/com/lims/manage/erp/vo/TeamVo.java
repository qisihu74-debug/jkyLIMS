package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/3/1 15:30
 * 任务领取——返回人员展示数据
 */
@Data
public class TeamVo {
    /**
     * 团队信息（含下一级）
     */
    List<LabelValueVo> teamVo;
    /**
     * 复核人
     */
    List<LabelValueVo> reviewVo;
    /**
     * 审批人
     */
    List<LabelValueVo> approverVo;
    /**
     * 签发人
     */
    List<LabelValueVo> signerVo;
    /**
     * 团队信息 一层
     */
    List<LabelValueVo> ledSampleVo;

}

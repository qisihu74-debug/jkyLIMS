package com.lims.manage.erp.entity;

import com.lims.manage.erp.vo.LabelValueVo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2023-09-13 17:27
 * @Copyright © 河南交科院
 */
@Data
public class ApproveInfo {
    private String verifyer;
    private String issuer;
    /**
     * 委托单编号
     */
    private String entrustmentNo;
    /**
     * 委托单位
     */
    private String entrustCompany;
    /**
     * 见证单位
     */
    private String witnessUint;
    /**
     * 工程名称
     */
    private String projectName;
    /**
     * 工程部位
     */
    private String projectPart;
    /**
     * 检测人
     */
    private String inspector;
    /**
     * 记录人
     */
    private String recorder;
    /**
     * 复核人（报告审核人）
     */
    private String reviewer;
    /**
     * 授权签字人（报告签发人）id
     */
    private String receiver;
    /**
     * 授权签字人（报告签发人）name
     */
    private String receiverName;
    /**
     * 报告编号
     */
    private String reportCode;
    /**
     * 报告检测人
     */
    private List<LabelValueVo> jcrMap;
    /**
     * 报告审核人
     */
    private List<KeyValue> shrMap;
    /**
     * 报告签发人
     */
    private List<KeyValue> qfrMap;
    private String mobile;
    private String reportTime;
}

package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lims.manage.erp.entity.AduditBaseData;
import com.lims.manage.erp.entity.DivideRectificationRecord;
import lombok.Data;

import java.util.List;

/**
 * 内审检查详情 视图层
 *
 * @Author: DLC
 * @Date: 2024/7/19 14:51
 */
@Data
public class InternalAuditDetailsVo {

    /**
     * 内审ID
     */
    private int activeId;
    /**
     * 分工id
     */
    private int divideId;
    /**
     * 部门Id
     */
    private String deptId;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 部门负责人
     */
    private String userName;
    /**
     * 不符合项
     */
    private String nonConformance;
    /**
     * 不符合程序
     */
    private String nonConformanceProgram;
    /**
     * 不符合标准
     */
    private String substandard;
    /**
     * 不符合程度
     */
    private String nonComplianceDegree;
    /**
     * 检查结果
     */
    private String checkResult;
    /**
     * 操作日期
     */
    private String operatingTime;
    /**
     * '检查待开始','检查中','检查完成','措施验证','已完成'
     */
    private String state;
    /**
     * 0整改通知、1=原因分析及纠正、2=纠正完成、3=措施验证
     */
    private String problemRectificationState;

    /**
     * 审核员姓名
     */
    private String auditorName;

    /**
     * 检查记录
     */
    List<AduditBaseData> aduditBaseDataList;
    /**
     * 问题整改
     */
    DivideRectificationRecord divideRectificationRecord;
}

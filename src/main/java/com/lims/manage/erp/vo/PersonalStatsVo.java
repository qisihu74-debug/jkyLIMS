package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;

/**
 * @Author: DLC
 * @Date: 2022/5/20 16:37
 * 个人工作量统计Vo
 */
@Data
public class PersonalStatsVo {

    private Integer pageNum;
    private Integer pageSize;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 科室编号
     */
    private String code;

    /**
     * 开始日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 截止日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date stopDate;

    /**
     * 姓名
     */
    private String name;

    /**
     * 人员id
     */
    private Long userId;
    /**
     * 创建委托
     */
    private Integer entrustNumber;

    /**
     * 分配任务
     */
    private Integer taskNumber;

    /**
     * 试验检测
     */
    private Integer testNumber;

    /**
     * 数据复核
     */
    private Integer reviewNumber;

    /**
     * 报告制作
     */
    private Integer makeNumber;

    /**
     * 报告审批
     */
    private Integer approvalNumber;

    /**
     * 报告签发
     */
    private Integer issueNumber;

    /**
     * 报告盖章
     */
    private Integer sealNumber;

}

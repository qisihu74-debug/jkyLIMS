package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 任务单
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TestTaskPool implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 任务池任务id
     */
    @TableId(value = "id", type = IdType.ID_WORKER)
    private Integer id;

    /**
     * 任务流水号
     */
    private String sn;

    /**
     * 样品信息
     */
    private String sample;

    /**
     * 对应委托单
     */
    private Long entrustmentId;

    /**
     * 本单费用
     */
    private String price;

    /**
     * 要求完成时间
     */
    private Date requiredCompletionTime;

    /**
     * 任务流转要求
     */
    private String taskFlowReq;

    /**
     * 任务单号
     */
    private String taskCode;

    /**
     * 发布人
     */
    private String publisher;

    /**
     * 发布时间
     */
    private Date publishDate;

    /**
     * 领取人id
     */
    private Long receiveId;

    /**
     * 领取人（当前登陆人员）
     */
    private String receiveName;

    /**
     * 领取时间
     */
    private Date receiveDate;


}

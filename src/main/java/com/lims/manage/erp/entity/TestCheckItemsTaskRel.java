package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TestCheckItemsTaskRel implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.ID_WORKER)
    private Integer id;

    /**
     * 检测项主键
     */
    private Integer itemId;

    /**
     * 检测项名称
     */
    private String checkItemName;

    /**
     * 样品名称
     */
    private String sampleName;

    /**
     * 样品编号
     */
    private String sampleCode;

    /**
     * 样品主键
     */
    private Integer sampleId;

    /**
     * 委托单主键
     */
    private Long entrustId;

    /**
     * 检测人
     */
    private String inspector;

    /**
     * 记录人
     */
    private String recorder;

    /**
     * 复核人
     */
    private String reviewer;

    /**
     * 报告制作人
     */
    private String reportProducer;

    /**
     * 辅助人员
     */
    private String auxiliaryPersonnel;

    /**
     * 见习生：实习的新手
     */
    private String probationer;

    /**
     * 实习生
     */
    private String interns;


}

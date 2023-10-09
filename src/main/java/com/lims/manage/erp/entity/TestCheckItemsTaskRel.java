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
     * name人名
     */
    private String userName;

    /**
     * user_id
     */
    private String userId;

    /**
     * 0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生
     */
    private Integer userType;

    /**
     * 任务单id
     */
    private Long taskId;


}

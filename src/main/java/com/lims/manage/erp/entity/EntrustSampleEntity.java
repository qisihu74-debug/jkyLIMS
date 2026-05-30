package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2021/12/1 15:37
 * @Copyright © 河南交科院
 */
@Data
public class EntrustSampleEntity {
    /**
     * 样品id
     */
    private Integer sampleId;
    /**
     * 委托单id
     */
    private Long entrustmentId;
    /**
     * 判定依据id
     */
    private Integer standardId;

}

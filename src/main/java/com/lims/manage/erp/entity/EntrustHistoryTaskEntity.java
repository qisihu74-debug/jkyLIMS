package com.lims.manage.erp.entity;

import com.lims.manage.erp.vo.EntrustSampleInfoVo;
import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/10 10:08
 * 任务委托信息
 */
@Data
public class EntrustHistoryTaskEntity extends EntrustHistoryEntity {

    /**
     * 样品状态
     */
    private String sampleStatus;
    /**
     * 样品信息
     */
    private List<EntrustSampleInfoVo> sampleInfoVos;

}

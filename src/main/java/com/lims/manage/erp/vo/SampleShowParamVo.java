package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.SampleItemEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/1 19:44
 */
@Data
public class SampleShowParamVo {
    private Integer id;
    private List<Integer> standardFileIds;

    /**
     * 样品检测项
     */
    private List<CheckItemInfoVo> sampleCheckItem;
}

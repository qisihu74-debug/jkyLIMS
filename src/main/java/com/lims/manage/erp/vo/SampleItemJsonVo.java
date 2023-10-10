package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.SampleItemEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2023/10/10 14:49
 */
@Data
public class SampleItemJsonVo {
    /**
     * 领样人
     */
    String sampler;
    List<SampleItemEntity> list = new ArrayList<>();
}

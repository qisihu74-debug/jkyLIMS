package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.LabelInfo;

import java.util.List;

/**
 * 标签信息业务层接口
 * @author: zhq
 * @date: 2023-01-05
 * @version: v1.0
 */
public interface LabelInfoService extends IService<LabelInfo> {

    /**
     * 通过标签名称转换为标签id
     * @param labelNames 标签名称列表
     * @return String
     */
    String getLabelId(String labelNames);

    /**
     * 根据标签id列表删除标签信息
     * @param labelIdList 标签id列表
     */
    void delLabelInfo(List<String> labelIdList);
}

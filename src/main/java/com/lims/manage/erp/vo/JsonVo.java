package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2024/12/18 10:20
 */
@Data
public class JsonVo {
    /**
     * json格式
     */
    private List<Long> ids;

    /**
     * 内容
     */
    private String content;
}

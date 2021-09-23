package com.stu.manage.demo.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc
 * @date 2021/9/23 17:58
 * @Copyright © 河南交科院
 */
@Data
public class SampleStatus {
    /**
     * 样品编号
     */
    private String sampleCode;
    /**
     * 样品检测进度1.流程审核中2.非正常结束3.审核通过4.待发起
     */
    private String checkProgress;
}

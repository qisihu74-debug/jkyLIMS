package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.DivideEntity;
import lombok.Data;

import java.util.List;

/**
 * 评审分工 vo
 *
 * @Author: DLC
 * @Date: 2024/7/15 14:51
 */
@Data
public class DivideVo {

    /**
     * 分工id
     */
    private int divideId;
    /**
     * 活动id
     */
    private int activeId;
    /**
     * 受审部门id
     */
    private String deptId;
    /**
     * 受审部门名称
     */
    private String deptName;
    /**
     * 分工对象集合
     */
    private List<DivideEntity> divideList;

}

package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/2/9 16:33
 * 返回前台页码
 */
@Data
public class PagingToolVo {
    /**
     *当前页
     */
    private Integer pageNum;
    /**
     *当前页 最大数
     */
    private Integer pageSize;
    /**
     * 当前数
     */
    private Integer size;
    /**
     * 开始行数
     */
    private Integer startRow;
    /**
     * 结束行数
     */
    private Integer endRow;
    /**
     * 总数
     */
    private Integer total;
    /**
     * 页数
     */
    private Integer pages;

    /**
     * 数据
     */
    List<?> list;
}

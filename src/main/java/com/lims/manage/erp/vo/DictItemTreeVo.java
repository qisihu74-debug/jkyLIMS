package com.lims.manage.erp.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 字典管理-字典项树结构模型
 *
 * @author : zhq
 * @version : V1.0
 * @date :   2023-01-03
 */
@Data
public class DictItemTreeVo {

    /**
     * 字典项主键，唯一标识
     */
    private String itemId;

    /**
     * 所属字典标识
     */
    private String dictId;

    /**
     * 所属字典项父主键
     */
    private String parentItemId;

    /**
     * 字典项文本
     */
    private String itemText;

    /**
     * 字典项排序
     */
    @ApiModelProperty(value = "字典项排序")
    private Integer itemSort;

    /**
     * 字典项树结构子级
     */
    private List<DictItemTreeVo> children;

    public void setChildren(List<DictItemTreeVo> children) {
        this.children = children.size() == 0 ? null : children;
    }
}

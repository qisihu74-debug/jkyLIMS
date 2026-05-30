package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description: 自定义编号
 * @Author: zhq
 * @Date:   2023-06-13
 * @Version: V1.0
 */
@Data
@TableName("sys_serial_number_rule")
@ApiModel(value="sys_serial_number_rule对象", description="自定义编号")
public class SnRuleEntity {

	/**流水号主键*/
    @ApiModelProperty(value = "流水号主键")
	private Long serialNumberId;

	/**流水号类型*/
    @ApiModelProperty(value = "流水号类型")
	private String serialNumberType;

	/**内容*/
    @ApiModelProperty(value = "内容")
	private String serialNumberContent;

	/**排序*/
    @ApiModelProperty(value = "排序")
	private Integer sort;

}

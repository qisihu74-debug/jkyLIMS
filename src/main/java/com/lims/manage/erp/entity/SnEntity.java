package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @Description: 自定义编号
 * @Author: zhq
 * @Date:   2023-06-13
 * @Version: V1.0
 */
@Data
@TableName("sys_serial_number")
@ApiModel(value="sys_serial_number对象", description="自定义编号")
public class SnEntity {

	/**id*/
	@TableId
    @ApiModelProperty(value = "id")
	private Long id;

	/**单据类型*/
    @ApiModelProperty(value = "单据类型")
	private String receiptType;

	/**编号规则*/
    @ApiModelProperty(value = "编号规则")
	private String serialNumberRule;

	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
	private Date createTime;

	/**修改日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改日期")
	private Date updateTime;

	/**用户ID*/
    @ApiModelProperty(value = "用户ID")
	private String userId;
    @TableField(exist = false)
    private List<SnRuleEntity> list;

}

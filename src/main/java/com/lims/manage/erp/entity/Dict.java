package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 字典实体
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_dict")
public class Dict implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * ID
	 */
	@TableId(type = IdType.ID_WORKER_STR)
	private String dictId;

	/**
	 * 字典名称
	 */
	private String dictName;

	/**
	 * 字典code
	 */
	private String dictCode;

	/**
	 * 删除标记
	 */
	private Integer delFlag;

	/**创建人*/
	private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;

	/**更新人*/
	private String updateBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateTime;
}

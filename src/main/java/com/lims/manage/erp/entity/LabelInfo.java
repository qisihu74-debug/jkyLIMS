package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 标签信息实体
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_label_info")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LabelInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 标签ID
	 */
	@TableId(type = IdType.ID_WORKER_STR)
	private String labelId;

	/**
	 * 标签内容
	 */
	private String labelContent;

	/**
	 * 标签类型:0:系统标签;1:用户自定义标签
	 */
	private Integer labelType;

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
}

package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 学习资料审核记录实体
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_data_audit_record")
public class DataAuditRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * ID
	 */
	@TableId(type = IdType.ID_WORKER_STR)
	private String id;

	/**
	 * 资料id
	 */
	private String dataId;

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 审核状态
	 */
	private Integer auditStatus;

	/**
	 * 审核内容
	 */
	private String auditContent;

	/**创建人*/
	private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;

	/**学习资料类型*/
	@TableField(exist = false)
	private String dataType;
}

package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 数据流转记录表
 * @Author zhq
 * @CreateTime 2024-06-12
 */
@Data
@TableName("test_data_transfer_record")
public class DataTransferRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 记录id
	 */
	@TableId(type = IdType.AUTO)
	private String id;

	/**
	 * 数据id
	 */
	private String dataId;

	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 用户名称
	 */
	private String userName;

	/**
	 * 操作数据内容
	 */
	private String dataContent;

	/**
	 * 操作数据描述
	 */
	private String dataDescribe;

	/**操作时间*/
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.util.Date dataTime;
}

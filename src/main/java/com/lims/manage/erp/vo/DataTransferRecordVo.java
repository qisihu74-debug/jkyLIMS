package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 数据流转记录表vo
 * @Author zhq
 * @CreateTime 2024-06-12
 */
@Data
public class DataTransferRecordVo implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 记录id
	 */
	private String id;

	/**
	 * 用户名称
	 */
	private String userName;

	/**
	 * 操作数据描述
	 */
	private String dataDescribe;

	/**操作时间*/
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.util.Date dataTime;
}

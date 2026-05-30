package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;


/**
 * @Description 数据流转关系表
 * @Author zhq
 * @CreateTime 2024-06-13
 */
@Data
@TableName("test_data_transfer_relation")
public class DataTransferRelation implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 记录id
	 */
	@TableId(type = IdType.AUTO)
	private Integer id;

	/**
	 * 数据id
	 */
	private String dataId;

	/**
	 * 数据关系id
	 */
	private String dataRelationId;
}

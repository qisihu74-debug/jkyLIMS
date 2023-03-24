package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lims.manage.erp.vo.PlanInfoImportVo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.List;


/**
 * @Description 培训/考试计划结果附件
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_plan_file_info")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanFileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 计划名称
     */
    @TableId(type = IdType.ID_WORKER_STR)
    private String fileId;

    /**
     * 计划id
     */
    private String planId;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件后缀
     */
    private String fileSuffix;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;
}

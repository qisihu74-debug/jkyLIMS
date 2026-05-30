package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2023/5/22 15:46
 */
@Data
public class NewsBeanVo {
    /**
     * id
     */
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容描述
     */
    private String content;
    /**
     * 链接数据
     */
    private List<LinkedDataVo> linkedData = new ArrayList<>();
    /**
     * 发布日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date publishDate;

}

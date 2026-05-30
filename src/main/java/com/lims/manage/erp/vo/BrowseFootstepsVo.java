package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.List;


/**
 * @Description 浏览足迹
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class BrowseFootstepsVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 浏览日期
     */
    private String browseTime;

    /**
     * 浏览足迹详情
     */
    private List<BrowseFootstepsDetailVo> browseFootstepsVoList;
}

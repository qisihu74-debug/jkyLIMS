package com.lims.manage.erp.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SampleAddDetailVo {
    /**
     * 序号
     */
    private Integer index;
    /**
     *批次数量
     */
    private String batchNumber;
    /**
     * 样品照片
     */
    private String picture;

    private MultipartFile files;
}

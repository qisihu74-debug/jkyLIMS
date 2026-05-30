package com.lims.manage.erp.http;

import com.lims.manage.erp.entity.QiYueSuoSealEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.http
 * @desc
 * @date 2022/2/25 11:15
 * @Copyright © 河南交科院
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QiYueSuoResponse {
    private Integer code;

    private String message;

    /**
     * 合同id
     */
    private Long contractId;
    /**
     * 报告合同签署url
     */
    private String signUrl;

    private List<QiYueSuoDocment> result;
    /**
     * 印章列表
     */
    private List<QiYueSuoSealEntity> list;
}

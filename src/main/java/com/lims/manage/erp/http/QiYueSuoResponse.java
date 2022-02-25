package com.lims.manage.erp.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

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

    private List<QiYueSuoDocment> result;
}

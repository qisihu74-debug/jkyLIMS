package com.lims.manage.erp.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.http
 * @desc
 * @date 2021/8/31 17:00
 * @Copyright © 河南交科院
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HttpResponse<T> implements Serializable {
    private Boolean success;

    private Integer code;

    private String msg;

    private List<T> data;
}

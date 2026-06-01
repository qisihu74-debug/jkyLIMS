package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class AuthorizedTechnicistVo {
    private Integer technicistId;
    private Long userId;
    private String userName;
    private String teamName;
}

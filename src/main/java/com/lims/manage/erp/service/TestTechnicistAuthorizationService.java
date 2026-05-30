package com.lims.manage.erp.service;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.AuthorizationSaveReq;

public interface TestTechnicistAuthorizationService {
    Result getAuthorizedList(Integer technicistId);
    Result getExcluded(Integer technicistId);
    Result save(AuthorizationSaveReq req);
}

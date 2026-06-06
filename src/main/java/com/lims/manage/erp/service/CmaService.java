package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.CmaCapabilityItem;
import com.lims.manage.erp.entity.CmaSyncLog;

import java.util.List;

public interface CmaService extends IService<CmaCapabilityItem> {

    PageInfo<CmaCapabilityItem> list(int pageNum, int pageSize, String domain, String standardName, String standardCode);

    List<String> domains();

    CmaSyncLog latestSync();

    void syncFromCma();

    void enrichHcnoAsync();
}

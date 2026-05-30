package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.Partenr;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * 合作伙伴（用户）(Partenr)表服务接口
 *
 * @author makejava
 * @since 2022-03-09 16:01:29
 */
public interface PartenrService extends IService<Partenr> {

    Result addpartenr(Partenr partenr);
    Result updpartenr(Partenr partenr);
    Result delPatent(List<Long> idList);
}


package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.TestTechnicistAuthorization;
import com.lims.manage.erp.mapper.TestTechnicistAuthorizationDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestTechnicistAuthorizationService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.AuthorizationExcludedVo;
import com.lims.manage.erp.vo.AuthorizationItemVo;
import com.lims.manage.erp.vo.AuthorizationListResultVo;
import com.lims.manage.erp.vo.AuthorizationSaveReq;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("testTechnicistAuthorizationService")
public class TestTechnicistAuthorizationServiceImpl implements TestTechnicistAuthorizationService {

    @Resource
    private TestTechnicistAuthorizationDao authorizationDao;

    @Override
    public Result getAuthorizedList(Integer technicistId) {
        if (technicistId == null) {
            return ResultUtil.error(4001, "缺少 technicistId");
        }
        List<AuthorizationItemVo> items = authorizationDao.getAuthorizedItems(technicistId);
        int totalItems = items.size();
        int excludedCount = authorizationDao.countAllItems() - totalItems;

        AuthorizationListResultVo vo = new AuthorizationListResultVo();
        vo.setItems(items);
        vo.setTotalItems(totalItems);
        vo.setExcludedCount(excludedCount);
        return ResultUtil.success(vo);
    }

    @Override
    public Result getExcluded(Integer technicistId) {
        if (technicistId == null) {
            return ResultUtil.error(4001, "缺少 technicistId");
        }
        List<Integer> excluded = authorizationDao.getExcludedItemIds(technicistId);
        AuthorizationExcludedVo vo = new AuthorizationExcludedVo();
        vo.setTechnicistId(technicistId);
        vo.setExcludedItemIds(excluded);
        return ResultUtil.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result save(AuthorizationSaveReq req) {
        if (req.getTechnicistId() == null) {
            return ResultUtil.error(4001, "缺少 technicistId");
        }
        Long operatorId = ShiroUtils.getUserInfo() != null ? ShiroUtils.getUserInfo().getUserId() : null;

        if (authorizationDao.existsTechnicist(req.getTechnicistId()) == 0) {
            return ResultUtil.error(4003, "technicistId 不存在");
        }
        List<Integer> excludedIds = req.getExcludedItemIds();
        if (excludedIds != null && !excludedIds.isEmpty()) {
            int found = authorizationDao.countExistingItems(excludedIds);
            if (found < excludedIds.size()) {
                return ResultUtil.error(4003, "存在无效的 productItemId");
            }
        }

        authorizationDao.deleteByTechnicistId(req.getTechnicistId());

        if (CollectionUtils.isNotEmpty(excludedIds)) {
            List<TestTechnicistAuthorization> insertList = new ArrayList<>();
            for (Integer itemId : excludedIds) {
                TestTechnicistAuthorization record = new TestTechnicistAuthorization();
                record.setTechnicistId(req.getTechnicistId());
                record.setProductItemId(itemId);
                record.setGranted(0);
                record.setOperatorId(operatorId);
                insertList.add(record);
            }
            authorizationDao.insertBatchExcluded(insertList);
        }

        Map<String, Integer> data = new HashMap<>();
        data.put("excludedCount", excludedIds == null ? 0 : excludedIds.size());
        return ResultUtil.success(data);
    }
}

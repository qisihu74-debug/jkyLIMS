package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.*;

import java.util.List;
import java.util.Map;

/**
 * 产品检测项(TestProductItem)表服务接口
 *
 * @author makejava
 * @since 2022-03-02 15:14:51
 */
public interface TestProductItemService extends IService<TestProductItem> {
    Result addTestProductItem(TestProductItemParamVo testProductItemParamVo);

    Result updTestProductItem(TestProductItemParamVo testProductItemParamVo);

    /**
     * 旧版本
     *
     * @param idList
     * @return
     */
    Result delTestProductItemOlderVersion(List<Long> idList);

    /**
     * TODO：23年11月30日 进行检测项删除操作的重构
     * 1、异常操作时，进行事务回滚。
     * 2、直接删除、并且需要把绑定关系解除后进行删除。
     * 3、日志的记录信息
     *
     * @param idList
     * @return
     */
    Result delTestProductItem(List<Long> idList);

    Result disableStatusTestProductItem(List<Long> idList);

    Result enableStatusTestProductItem(List<Long> idList);

    TestProductItemParamVo getItemParamVo(TestProductItem testProductItem);

    List<TestProductItemSelVo> getTestProductSelVoList(TestProductItem testProductItem);

    List<TestProductItemTreeVo> getTreeList(TestProductItem testProductItem);

    /**
     * 查询产品标准模板的sheet
     * @param productId
     * @return
     */
    List<LabelValueVo> getProductTemplateSheet(Integer productId) throws Exception;

    List<Integer> getSheetIndex(Integer checkItemId);

    /**
     * 返回基础信息
     * @return
     */
    Map<String, List<LabelValueVo>> returnEntrustData();

    void updateHourById(Integer id, Integer hour);
}


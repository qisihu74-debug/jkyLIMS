package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.vo.*;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.vo.CheckItemDetailVo;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface EntrustService {
    /**
     * 新增委托
     * @param vo
     * @return
     */
    Boolean addEntrust(EntrustAddVo vo, MultipartFile file);

    /**
     * 查询产品检测项
     * @param productId
     * @return
     */
    List<CheckItemDetailVo> getAllItemByProductId(Integer productId);

    /**
     * 查询检测项详细信息
     * @param ids
     * @return
     */
    List<CheckItemInfoVo> getCheckItemInfoVo(List<Integer> ids);

    Map<String, List<LabelValueVo>> returnEntrustData();

    List<TestCustomerJsonEntity> returnTestCustomerEntityList(Integer companyId);

    /**
     * 新增委托单位信息
     * @param testCompanyEntity
     * @return
     */
    Boolean addCompanyData(TestCompanyJsonEntity testCompanyEntity);

    /**
     * 查询样品列表
     * @param sampleEntity
     * @return
     */
    List<SampleEntity> getSampleDataList(SampleEntity sampleEntity);

    List<LabelValueVo> selectProductList(String productName);

    /**
     * 新增样品信息
     * @param addParamVo
     * @return
     */
    Integer addSampleData(SampleAddParamVo addParamVo,MultipartFile[] file);

    /**
     * 查询判定依据
     * @param productId
     * @return
     */
    List<LabelValueVo> getJudges(Integer productId);

    /**
     * 历史委托信息
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryEntity> getEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity);
    /**
     * 史委托信息 具体详情
     * @param entrustmentId
     * @return
     */
    EntrustAddVo getEntrustHistoryDetail(Long entrustmentId);

}

package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface EntrustEntityMapper extends BaseMapper {
    /**
     * 获取最大委托单编号
     * @return
     */
    Integer selectMaxNo();

    EntrustAddVo selectByKeyId(Long id);

    /**
     * 保存委托单下样品信息
     * @param list
     */
    void BatchSaveEntrustSample(List<EntrustSampleEntity> list);

    /**
     * 保存委托单样品，判定依据信息
     * @param list1
     */
    void BatchSaveSampleStandard(@Param("list1") List<EntrustSampleEntity> list1);

    /**
     * 保存委托样品下检测项信息
     * @param sampleItemList
     */
    void BatchSaveEntrustSampleItem(@Param("sampleItemList") List<SampleItemEntity> sampleItemList);

    /**
     * 缴费记录新增
     * @param pamentEntity
     */
    void saveEntrustPayRecord(EntrustPamentEntity pamentEntity);

    /**
     * 查询判定依据
     * @param productId
     * @return
     */
    List<LabelValueVo> getJudges(Integer productId);
    /**
     * 委托历史 查询。
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryEntity> selectEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity);

    /**
     * 新增委托信息
     * @param basisInfo
     */
    void insertEntrustInfo(EntrustEntity basisInfo);

    /**
     * 修改委托信息
     * @param basisInfo
     * @return
     */
    int updateEntrustInfo(EntrustEntity basisInfo);

    /**
     * 根据委托编号 获取 存储的样品id 集合
     */
    List<Integer> getSampleIdSet(Long entrustmentId);

    /**
     * 通过委托单id 获取缴费记录
     */
    String getTestEntrustedPaymentRecordInfoPrice(Long entrustmentId);
    /**
     *  通过委托单id 支付方式
     */
    String getTestEntrustedInfoMethodName(Long entrustmentId);
    /**
     * 通过委托单id 获取联系地址
     */
    String getEntrustingParty(Long entrustmentId);
    /**
     * 删除样品id
     */
    int removeTestEntrustedSampleDetailsRel(Long entrustmentId);
    /**
     * 删除判定依据id
     */
    int removeTestEntrustedSampleStandardRel(Long entrustmentId);
    /**
     * 删除缴费
     */
    int removeTestEntrustedPaymentRecordInfo(Long entrustmentId);
    /**
     * 样品下检测依据
     */
    int removeTestEntrustedSampleCheckitemRel(Long entrustmentId);


    /**
     * 获取产品下的标准文件名称
     * @param productId
     * @return
     */
    List<String> getStatndardByPId(Integer productId);
}
package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
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

    EntrustAddVo selectByKeyId(Integer id);

    /**
     * 保存委托单下样品信息
     * @param list
     */
    void BatchSaveEntrustSample(List<EntrustSampleEntity> list);

    /**
     * 保存委托单样品，判定依据信息
     * @param list1
     */
    void BatchSaveSampleStandard(List<EntrustSampleEntity> list1);

    /**
     * 保存委托样品下检测项信息
     * @param sampleItemList
     */
    void BatchSaveEntrustSampleItem(List<SampleItemEntity> sampleItemList);

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
}
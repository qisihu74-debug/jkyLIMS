package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.EntrustHistoryTaskEntity;
import com.lims.manage.erp.entity.EntrustPamentEntity;
import com.lims.manage.erp.entity.EntrustSampleEntity;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.HistoryEntrustDataVo;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
     * 通过委托编号 获取委托单id 是否存在
     * @param code
     * @return
     */
    EntrustAddVo getByData(Integer code);

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
     * 通过委托单id 获取样品依据信息集合
     * @param entrustmentId
     * @return
     */
    @Select("SELECT\n" +
            "t2.code as judgmentBasis\n" +
            "FROM\n" +
            "test_entrusted_sample_standard_rel as t1 \n" +
            "LEFT JOIN test_standard_file as t2 ON t1.standard_id = t2.id\n" +
            "WHERE t1.entrustment_id = #{entrustmentId} and t2.code is not null\n")
    List<String> getSampleStandard(Long entrustmentId);

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
     * 委托历史 查询。
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryEntity> selectEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity);

    /**
     * 历史委托查询 state不为0 state不为144
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryEntity> selectEntrustHistoryListRelease_of(EntrustHistoryEntity entrustHistoryEntity);
    /**
     * 委托单任务待发布列表
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryTaskEntity> selectEntrustReleasedList(EntrustHistoryTaskEntity entrustHistoryEntity);

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
     * 查询样品id 是否存在
     * @param entrustmentId
     * @return
     */
    @Select("SELECT count(*) FROM test_entrusted_sample_details_rel WHERE entrustment_id= #{entrustmentId}")
    Integer countSampleDetailsRel(Long entrustmentId);
    /**
     * 删除样品id
     */
    int removeTestEntrustedSampleDetailsRel(Long entrustmentId);

    /**
     * 查询依据id 是否存在
     * @param entrustmentId
     * @return
     */
    @Select("SELECT count(*) FROM test_entrusted_sample_standard_rel WHERE entrustment_id = #{entrustmentId}")
    Integer countSampleStandardRel(Long entrustmentId);
    /**
     * 删除判定依据id
     */
    int removeTestEntrustedSampleStandardRel(Long entrustmentId);
    /**
     * 删除缴费
     */
    int removeTestEntrustedPaymentRecordInfo(Long entrustmentId);

    /**
     * 判断样品下检测依据
     * @param entrustmentId
     * @return
     */
    @Select("SELECT count(*) FROM test_entrusted_sample_checkitem_rel WHERE  entrust_id = #{entrustmentId}")
    Integer countSampleCheckitemRel(Long entrustmentId);
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

    /**
     * 查询委托下的样品ID
     * @param entrustmentId
     * @return
     */
    List<Integer> getSampleId(Long entrustmentId);

    /**
     * 查询委托单位上一次工程名称、工程部位
     * @param name
     * @return
     */
    HistoryEntrustDataVo getHistoryData(String name);

    /**
     * 根据检测项ID查询可以做的团队
     * @param checkItemId
     * @return
     */
    List<LabelValueVo> getDept(Integer checkItemId);

}
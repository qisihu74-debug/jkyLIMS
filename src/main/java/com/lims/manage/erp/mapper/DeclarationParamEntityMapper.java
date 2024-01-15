package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.DeclarationItemEntity;
import com.lims.manage.erp.entity.DeclarationParamEntity;
import com.lims.manage.erp.entity.DeclarationProductEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface DeclarationParamEntityMapper {
    int insert(DeclarationParamEntity record);

    int batchInsert(@Param("items")List<DeclarationParamEntity> items);

    int insertNew(DeclarationItemEntity record);

    int insertSelective(DeclarationParamEntity record);

    /**
     * 查询依据标准下拉列表
     * @param standard
     * @return
     */
    List<LabelValueVo> getStandard(String standard);

    /**
     * 查询检测方法下拉列表
     * @param standardId
     * @param method
     * @return
     */
    List<LabelValueVo> getMethod(Long standardId,String method);

    /**
     * 查询仪器设备下拉列表
     * @param equipment
     * @return
     */
    List<LabelValueVo> getEquipment(String equipment);

    /**
     * 校验检测项是否已存在
     * @param paramEntity
     * @return
     */
    DeclarationItemEntity checkParamNew(DeclarationItemEntity paramEntity);
    DeclarationParamEntity checkParam(DeclarationParamEntity paramEntity);

    /**
     * 查询检测项ID
     * @param productId
     * @param checkItemName
     * @return
     */
    Long getCheckItemId(Long productId,String checkItemName);

    /**
     * 删除申报参数
     * @param record
     * @return
     */
    int deleteParam(DeclarationItemEntity record);

    int deleteItem(DeclarationItemEntity record);

    /**
     * 查询申报参数列表
     * @param paramEntity
     * @return
     */
    List<DeclarationParamEntity> getParamList(DeclarationParamEntity paramEntity);
    List<DeclarationItemEntity> getItemList(DeclarationItemEntity paramEntity);

    /**
     * 删除申报参数的检测依据
     * @param record
     * @return
     */
    int deleteParamStandard(DeclarationParamEntity record);

    /**
     * 查询申报参数详情
     * @param paramEntity
     * @return
     */
    List<DeclarationParamEntity> getParamDetail(DeclarationItemEntity paramEntity);
    DeclarationItemEntity getParamDetailInfo(DeclarationItemEntity paramEntity);

    /**
     * 查询产品的检测项
     * @param productId
     * @return
     */
    List<LabelValueVo> getCheckItemList(Long productId);
}

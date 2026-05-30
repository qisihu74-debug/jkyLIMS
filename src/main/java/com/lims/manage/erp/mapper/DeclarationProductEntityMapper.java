package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.DeclarationProductEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface DeclarationProductEntityMapper {
    int insert(DeclarationProductEntity record);

    int insertSelective(DeclarationProductEntity record);

    /**
     * 查询产品类别下拉
     * @return
     */
    List<LabelValueVo> getProductType();

    /**
     * 查询产品下拉
     * @param productTypeId
     * @return
     */
    List<LabelValueVo> getProductListSelect(Integer productTypeId);

    /**
     * 删除申报计划下的产品
     * @param record
     * @return
     */
    int deleteProduct(DeclarationProductEntity record);

    /**
     * 查询当前计划下有没有指定产品
     * @param record
     * @return
     */
    DeclarationProductEntity checkProduct(DeclarationProductEntity record);

    /**
     * 查询产品是否存在
     * @param productName
     * @return
     */
    Long getProductId(String productName);

    /**
     * 编辑申报计划下的产品信息
     * @param productEntity
     * @return
     */
    int updateProduct(DeclarationProductEntity productEntity);

    /**
     * 查询申报计划下的产品列表
     * @param productEntity
     * @return
     */
    List<DeclarationProductEntity> getProductList(DeclarationProductEntity productEntity);
}

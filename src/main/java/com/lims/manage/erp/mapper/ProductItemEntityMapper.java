package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ProductItemEntity;
import com.lims.manage.erp.vo.CheckItemDetailVo;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@Mapper
public interface ProductItemEntityMapper {
    int deleteByPrimaryKey(Integer checkItemId);

    int insert(ProductItemEntity record);

    int insertSelective(ProductItemEntity record);

    ProductItemEntity selectByPrimaryKey(Integer checkItemId);

    int updateByPrimaryKeySelective(ProductItemEntity record);

    int updateByPrimaryKey(ProductItemEntity record);

    /**
     * 根据产品ID查询产品检测项信息
     * @param productId 产品ID
     * @return 产品检测项数据
     */
    List<CheckItemDetailVo> getAllItemByProductId(Integer productId);

    /**
     * 查询检测项详细信息
     * @param checkIds
     * @return
     */
    List<CheckItemInfoVo> getItemInfo(@Param("checkIds")List<Integer> checkIds);


}
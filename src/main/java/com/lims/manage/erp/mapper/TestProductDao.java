package com.lims.manage.erp.mapper;

import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/11/30 15:33
 * 产品信息
 */
@Component
@Mapper
public interface TestProductDao {
    /**
     * 查询产品--模糊查询
     * @param productName
     * @return
     */
    List<LabelValueVo> selectProductList(@Param("productName")String productName);

    /**
     * 通过产品ID查询产品名称
     * @param productId
     * @return
     */
    String getProductNameById(Integer productId);

    /**
     * 查询当前年份最大样品编号
     * @param year
     * @return
     */
    String getMaxNumber(String year);
}

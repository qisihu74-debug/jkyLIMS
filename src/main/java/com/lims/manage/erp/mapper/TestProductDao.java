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

    List<LabelValueVo> selectProductList(@Param("productName")String productName);
}

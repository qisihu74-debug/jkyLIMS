package com.stu.manage.demo.mapper;

import com.stu.manage.demo.entity.ProductVo;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface ProductMapper {

    /**
     * 获取所有产品类型
     * @return
     */
    List<ProductVo> getProductType();

    /**
     * 根据产品类型获取产品
     * @param type
     * @return
     */
    List<ProductVo> getProduct(int type);

}

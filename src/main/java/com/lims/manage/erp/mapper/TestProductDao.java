package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TestInstrumentVo;
import com.lims.manage.erp.vo.TestProductVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/11/30 15:33
 * 产品信息
 */
@Component
@Mapper
public interface TestProductDao extends BaseMapper<TestProduct> {
    /**
     * 查询产品--模糊查询
     *
     * @param productName
     * @return
     */
    List<LabelValueVo> selectProductList(@Param("productName") String productName);

    /**
     * 通过产品ID查询产品名称
     *
     * @param productId
     * @return
     */
    String getProductNameById(Integer productId);

    /**
     * 查询产品判定依据
     *
     * @param productId
     * @return
     */
    List<LabelValueVo> getJudges(Integer productId);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<TestProduct> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<TestProduct> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<TestProduct> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<TestProduct> entities);

    IPage<TestProductVo> getPageList(IPage<TestProductVo> page, @Param(Constants.WRAPPER) Wrapper<TestProduct> queryWrapper);

    int isLast(Integer checkItemId);

    List<LabelValueVo> getAllCheckBasis(Integer checkItemId);
}

package com.stu.manage.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stu.manage.demo.entity.*;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface EntrustMapper extends BaseMapper<EntrustStat> {

    /**
     * 再来一单--委托基本信息
     * @param entrustId
     * @return
     */
    EntrustInfo onceMore(int entrustId);

    /**
     * 再来一单--样品信息
     * @param entrustId
     * @return
     */
    List<SampleInfoVo> getSampleInfo(int entrustId);
    /**
     * 再来一单--检测项信息
     * @param entrustId
     * @return
     */
    List<CheckItemInfoVo> getCheckItemInfo(int entrustId);

    /**
     * 根据产品获取产品全部检测项
     * @param productId
     * @return
     */
    List<CheckItemCostVo> getCheckItemsByProductId(int productId);

    /**
     * 根据产品获取产品判定依据
     * @param productId
     * @return
     */
    List<ProductVo> getCheckBasisByProductId(int productId);

    /**
     * 检测委托下的样品状态
     * @param id
     * @return
     */
    List<SampleStatus> getSampleStat(Integer id);

    /**
     * 获取任务状态和任务流程审批状态
     * @param id
     * @return
     */
    List<SampleStatus> getTaskStat(Integer id);
}

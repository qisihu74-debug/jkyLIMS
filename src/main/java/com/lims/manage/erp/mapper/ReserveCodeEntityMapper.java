package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReserveCodeEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface ReserveCodeEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReserveCodeEntity record);

    int insertSelective(ReserveCodeEntity record);

    ReserveCodeEntity selectByPrimaryKey(Long id);

    /**
     * 查询预留编号信息
     * @param entity
     * @return
     */
    List<ReserveCodeEntity> getList(ReserveCodeEntity entity);

    int updateByPrimaryKeySelective(ReserveCodeEntity record);

    /**
     * 编辑预留编号
     * @param record
     * @return
     */
    int updateReserveCode(ReserveCodeEntity record);

    int updateByPrimaryKey(ReserveCodeEntity record);

    /**
     * 批量删除
     * @param ids
     * @return
     */
    int batchDelete(List<Long> ids);

    /**
     * 查询委托单号下拉列表
     * @param entrustmentNo
     * @return
     */
    List<LabelValueVo> getEntrustmentNoList(@Param("entrustmentNo") String entrustmentNo);

    /**
     * 校验预留编号是否重复
     * @param code
     * @return
     */
    int getCodeSize(String code);

    /**
     * 批量插入预留编号
     * @param records
     * @return
     */
    int batchInsert(@Param("records") List<ReserveCodeEntity> records);

}
package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReserveCodeEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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
     *
     * @param records
     * @return
     */
    int batchInsert(@Param("records") List<ReserveCodeEntity> records);

    // 查询最终报告：
    @Select("SELECT id,report_code FROM test_report_record WHERE report_code like CONCAT(\"%\",#{reportCode},\"%\")  LIMIT 1")
    ReserveCodeEntity selectReportRecord(String reportCode);

    // 查询中间报告：
    @Select("SELECT id,report_code FROM test_report_record_mid WHERE report_code like CONCAT(\"%\",#{reportCode},\"%\")  LIMIT 1")
    ReserveCodeEntity selectReportRecordMid(String reportCode);

    // 查询留号报告：
    @Select("SELECT id,entrustment_no,report_code,create_date,use_date FROM test_reserve_code WHERE report_code like CONCAT(\"%\",#{reportCode},\"%\")  LIMIT 1")
    ReserveCodeEntity selectReportRecordReserveCode(String reportCode);

    /**
     * 查询最终报告
     *
     * @param year
     * @param code
     * @return
     */
    @Select("SELECT\n" +
            "\tMAX( SUBSTR( report_code FROM 13 ) + 0 ) \n" +
            "FROM\n" +
            "\ttest_report_record \n" +
            "WHERE\n" +
            "\treport_code LIKE CONCAT( '%-', #{year}, '-%' ) \n" +
            "\tAND report_code LIKE CONCAT( '%', #{code}, '%' )")
    Integer getfinalReportOtherMaxCode(String year, String code);

    /**
     * 查询中间报告
     *
     * @param year
     * @param code
     * @return
     */
    @Select("SELECT\n" +
            "\tMAX( SUBSTR( report_code FROM 13 ) + 0 ) \n" +
            "FROM\n" +
            "\ttest_report_record_mid \n" +
            "WHERE\n" +
            "\treport_code LIKE CONCAT( '%-', #{year}, '-%' ) \n" +
            "\tAND report_code LIKE CONCAT( '%', #{code}, '%' )")
    Integer getMidReportOtherMaxCode(String year, String code);

    /**
     * 更新最终报告
     *
     * @param id
     * @param reportCode
     * @return
     */
    @Update("UPDATE test_report_record SET report_code = #{reportCode} WHERE id = #{id}")
    int updatefinalReport(@Param("id") Long id, @Param("reportCode") String reportCode);

    /**
     * 更新中间报告报告
     *
     * @param id
     * @param reportCode
     * @return
     */
    @Update("UPDATE test_report_record_mid SET report_code = #{reportCode} WHERE id = #{id}")
    int updateMidReport(@Param("id") Long id, @Param("reportCode") String reportCode);


}
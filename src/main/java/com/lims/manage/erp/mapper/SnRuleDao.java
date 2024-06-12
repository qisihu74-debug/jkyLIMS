package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SnRecord;
import com.lims.manage.erp.entity.SnRule;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-06-11 16:35
 * @Copyright © 河南交科院
 */
public interface SnRuleDao {
    @Select("select * from sys_serial_number_record where type=#{type}")
    SnRecord getInfoByType(@Param("type") String type);

    @Select("SELECT\n" +
            "\tt2.serial_number_content,t2.serial_number_type,t2.sort\n" +
            "FROM\n" +
            "\tsys_serial_number t1\n" +
            "LEFT JOIN sys_serial_number_rule t2 ON t1.id = t2.serial_number_id\n" +
            "WHERE\n" +
            "\tt1.receipt_type =#{type}\n" +
            "ORDER BY\n" +
            "\tt2.sort DESC")
    List<SnRule> getLastNumberByType(@Param("type") String type);

    @Update("update sys_serial_number_record set sn=#{sn} where id=#{id}")
    void updateSnById(@Param("id") Long id, @Param("sn") String sn);

    @Select("select serial_number_rule from sys_serial_number where receipt_type=#{type}")
    String getSnByType(@Param("type") String type);

    @Insert("insert into sys_serial_number_record(id,type,sn,status) values(#{item.id},#{item.type},#{item.sn},#{item.status})")
    void insertSn(@Param("item") SnRecord item);

    @Select("select id from sys_serial_number where receipt_type=#{type}")
    Long getIdByType(@Param("type") String type);

    @Select("select serial_number_type from sys_serial_number_rule where serial_number_id=#{id}")
    List<String> getMessageById(@Param("id") Long id);
}

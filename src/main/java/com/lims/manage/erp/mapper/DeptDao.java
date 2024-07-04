package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.vo.DingDeptVo;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/11/19 15:19
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface DeptDao extends BaseMapper<DingDeptEntity> {
    /**
     * 查询组织架构信息--树状递归
     * @return
     */
    List<DingDeptVo> getAllDept();

    /**
     * 根据角色ID查询角色信息
     * @param id
     * @return
     */
    LabelValueVo getRoleInfoById(Long id);

    /**
     * 获取部门信息
     * @param search
     * @return
     */
    List<DingDeptEntity> getAllList(@Param(value = "search") String search);

    /**
     * 获取部门子集列表
     * @param id
     * @return
     */
    @Select("WITH RECURSIVE td AS (\n" +
            "                SELECT * FROM sys_dept WHERE id = #{id} \n" +
            "                UNION ALL \n" +
            "                SELECT c.* FROM sys_dept c ,td WHERE c.parent_id = td.id\n" +
            "            ) SELECT * FROM td ORDER BY td.id")
    List<DingDeptEntity> sonList(Long id);


    @Select("WITH RECURSIVE cte AS (\n" +
            "            SELECT a.id, a.parent_id,a.name FROM sys_dept a WHERE a.id=#{id} \n" +
            "            UNION ALL\n" +
            "            SELECT k.id, k.parent_id,k.name FROM sys_dept k INNER JOIN cte c ON c.parent_id = k.id\n" +
            "            )SELECT id,name,parent_id FROM cte")
    List<DingDeptEntity> parentList(Long id);

    @Select("SELECT\n" +
            "id\n" +
            "FROM\n" +
            "sys_dept")
    List<Long> getDeptLong();

    @Select("select name from sys_dept where id=#{id} ")
    String getNameById(@Param("id") Long id);

    @Select({"<script>",
            " SELECT DISTINCT ",
            " t1.userid",
            " FROM sys_ding_user t1 LEFT  JOIN sys_dept t2 ON t2.id LIKE CONCAT( '%', t1.department, '%' )" +
            " WHERE t2.name in ",
            "<foreach item='item' index='index' collection='items' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    List<String> getUserIdsByDeptNames(@Param("items") List<String> items);

    @Select("SELECT\n" +
            "\tu.user_id \n" +
            "FROM\n" +
            "\tsys_user u\n" +
            "\tJOIN sys_dept d ON FIND_IN_SET( d.id, u.department ) > 0 \n" +
            "WHERE\n" +
            "\tu.user_id = #{userId}\n" +
            "\tAND d.`name` = '技术质量部'")
    Long checkUserId(@Param("userId") Long userId);

    @Select("SELECT\n" +
            "\tt1.id AS VALUE,\n" +
            "\tt1.NAME AS label \n" +
            "FROM\n" +
            "\tsys_dept AS t1\n" +
            "\tLEFT JOIN sys_user_dept_middle AS t2 ON t1.id = t2.dept_id\n" +
            "\tWHERE t2.user_id = #{userId}")
    List<LabelValueVo> selectDepartments(@Param("userId") Long userId);
}

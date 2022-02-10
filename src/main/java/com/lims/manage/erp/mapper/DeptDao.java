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

    /**
     * 根据当前登陆管理员id获取管理员所属部门及子级别=部门idj集合
     * @param userId
     * @return
     */
    @Select("WITH RECURSIVE td AS (\n" +
            "\tSELECT\n" +
            "\t\tid\n" +
            "\tFROM\n" +
            "\t\tsys_dept\n" +
            "\tWHERE\n" +
            "\t\tid IN (\n" +
            "\t\t\tSELECT DISTINCT\n" +
            "\t\t\t\tsdep.id\n" +
            "\t\t\tFROM\n" +
            "\t\t\t\tsys_user sysu\n" +
            "\t\t\tLEFT JOIN sys_person sysp ON sysu.person_id = sysp.id\n" +
            "\t\t\tLEFT JOIN sys_dept sdep ON sysp.dept_id = sdep.id\n" +
            "\t\t\tWHERE\n" +
            "\t\t\t\tsysu.user_id = #{userId}\n" +
            "\t\t)\n" +
            "\tAND is_delete = 0\n" +
            "\tUNION ALL\n" +
            "\t\tSELECT\n" +
            "\t\t\tc.id\n" +
            "\t\tFROM\n" +
            "\t\t\tsys_dept c,\n" +
            "\t\t\ttd\n" +
            "\t\tWHERE\n" +
            "\t\t\tc.pid = td.id\n" +
            ") SELECT\n" +
            "\tid\n" +
            "FROM\n" +
            "\ttd")
    List<Long> getChirdDeptsByUser(Long userId);
}

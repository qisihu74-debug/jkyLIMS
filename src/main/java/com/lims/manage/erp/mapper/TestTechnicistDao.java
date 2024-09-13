package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TechnicistCapacity;
import com.lims.manage.erp.entity.TestProductType;
import com.lims.manage.erp.entity.TestTechnicist;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TestTechnicistVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.List;

/**
 * 技术人员(TestTechnicistVo)表数据库访问层
 *
 * @author makejava
 * @since 2022-02-23 09:14:43
 */
public interface TestTechnicistDao extends BaseMapper<TestTechnicist> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestTechnicistVo> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestTechnicist> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestTechnicistVo> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestTechnicist> entities);
    IPage<TestTechnicistVo> getListPage(IPage<TestTechnicistVo> page, @Param(Constants.WRAPPER) Wrapper<TestTechnicist> queryWrapper);
    List<SysUserEntity> getUserList();

    @Select("select team_id from test_technicist where user_id=#{userId} and del_flag = 0 limit 1")
    Integer getSealer(@Param("userId") Long userId);

    List<LabelValueVo> inspectorList(@Param("search") String search);

    List<TechnicistCapacity> getTypeAndProductList(@Param("id") Serializable id);

    void insertBatchCapacity(@Param("list") List<TechnicistCapacity> capacityList);

    @Delete("delete from test_technicist_capacity where technicist_id=#{id}")
    void deleteCapatity(@Param("id") Integer id);

    void deleteBatchCapatity(@Param("list") List<Long> idList);

    List<TechnicistCapacity> getProductTypeAndProduct();

    @Select("select pid from test_team where id=#{id}")
    Integer getPidById(@Param("id") Integer id);
}


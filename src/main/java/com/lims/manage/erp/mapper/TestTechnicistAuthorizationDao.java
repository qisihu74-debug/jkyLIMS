package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestTechnicistAuthorization;
import com.lims.manage.erp.vo.AuthorizationItemVo;
import com.lims.manage.erp.vo.ProductItemNodeVo;
import com.lims.manage.erp.vo.ProductTypeWithItemsVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TestTechnicistAuthorizationDao extends BaseMapper<TestTechnicistAuthorization> {

    @Delete("DELETE FROM test_technicist_authorization WHERE technicist_id = #{technicistId}")
    void deleteByTechnicistId(@Param("technicistId") Integer technicistId);

    void insertBatchExcluded(@Param("list") List<TestTechnicistAuthorization> list);

    @Select("SELECT product_item_id FROM test_technicist_authorization WHERE technicist_id = #{technicistId} AND granted = 0")
    List<Integer> getExcludedItemIds(@Param("technicistId") Integer technicistId);

    List<AuthorizationItemVo> getAuthorizedItems(@Param("technicistId") Integer technicistId);

    @Select("SELECT COUNT(*) FROM test_product_item WHERE del_flag = 0")
    int countAllItems();

    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM test_technicist WHERE id = #{id} AND del_flag = 0")
    int existsTechnicist(@Param("id") Integer id);

    int countExistingItems(@Param("ids") java.util.List<Integer> ids);
}

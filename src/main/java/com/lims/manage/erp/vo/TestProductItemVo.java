package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.entity.TestProductSpecs;
import com.lims.manage.erp.entity.TestProductStandardFileRel;
import lombok.Data;
import java.util.List;

/**
 * 产品检测项(TestProductItem)表实体类
 *
 * @author makejava
 * @since 2022-03-02 15:14:51
 */
@SuppressWarnings("serial")
@Data
public class TestProductItemVo extends Model<TestProductItemVo> {
    //产品基本信息
    private TestProduct testProduct;
    //产品判断依据
    private List<Integer> standardRelIds;
    //产品规格等级
    private List<String> specsList;
    //报告模板xls与产品关联
    private List<Integer> templateIds;
}


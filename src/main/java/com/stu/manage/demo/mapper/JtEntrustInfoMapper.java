package com.stu.manage.demo.mapper;

import com.stu.manage.demo.entity.Company;
import com.stu.manage.demo.entity.JtEntrustInfo;
import com.stu.manage.demo.entity.ProductVo;
import com.stu.manage.demo.entity.jtEntrustType;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/9/23 10:00
 * 委托基本信息
 */
@Component
@Mapper
public interface JtEntrustInfoMapper {

    /**
     * 实现 Add 委托基本信息
     * @param record
     * @return
     */
    int insertSelective(JtEntrustInfo record);

    /**
     * 获取自动生成编号
     * @param entrustNumber
     * @return
     */
    int selectEntrustNumberForCount(@Param("entrustNumber")String entrustNumber);

    /**
     * 查询委托公司信息
     */
    Company selectCompanyName(@Param("comName")String comName);

    /**
     * 新增公司信息
     */
    int insertCompany(Company company);

    /**
     * 委托方式查询
     */
    List<ProductVo> getEntrustTheWay();

    /**
     * 检验目的
     */
    List<ProductVo> getcheckPurpose();

    /**
     * 取样方式
     */
    List<ProductVo> getreceiveWay();

    /**
     * 取报告方式
     */
    List<ProductVo> getreportGetWay();

}

package com.lims.manage.erp.service;

import com.alibaba.fastjson.JSONException;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.EntrustHistoryTaskEntity;
import com.lims.manage.erp.entity.TaskEntity;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.vo.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface EntrustService {
    /**
     * 新增委托
     * @param vo
     * @return
     */
    Boolean addEntrust(EntrustAddVo vo, MultipartFile[] file);

    /**
     * 新增委托测试丁
     * @param vo
     * @return
     */
    Boolean addEntrustTest(EntrustAddVo vo, MultipartFile[] file);





    /**
     * 修改委托
     */
    Boolean updateEntrust(EntrustAddVo vo, MultipartFile[] file);

    /**
     * 修改委托测试 new
     */
    Boolean updateEntrustTestNew(EntrustAddVo vo, MultipartFile[] file);

    /**
     * 修改委托样品测试 new
     */
    Boolean updateEntrustTestNewSample(EntrustAddVo vo);

    /**
     * 实现作废
     */
    Boolean abandonEntrust(EntrustEntity entrustEntity);

    /**
     * 查询检测项详细信息
     * @param ids
     * @return
     */
    List<CheckItemInfoVo> getCheckItemInfoVo(List<Integer> ids);

    /**
     * 查询检测项 方法 依据
     * @param id
     * @return
     */
    Map<String,List<LabelValueVo>> getItemMethodStandard(Integer id);

    Map<String, List<LabelValueVo>> returnEntrustData();

    List<TestCustomerJsonEntity> returnTestCustomerEntityList(Integer companyId);

    /**
     * 新增委托单位信息
     * @param testCompanyEntity
     * @return
     */
    Boolean addCompanyData(TestCompanyJsonEntity testCompanyEntity);

    /**
     * 新增委托单位信息 公司名称、地址、类型
     * @param testCompanyEntity
     * @return
     */
    Boolean addCompanyDataTwo(TestCompanyJsonEntity testCompanyEntity);

    /**
     * 历史委托信息
     * @param entrustHistoryEntity
     * @return
     */
    PageInfo getEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity) throws ParseException;
    /**
     * 委托单任务待发布列表
     * @param entrustHistoryEntity
     * @return
     */
    PageInfo getEntrustReleasedList(EntrustHistoryTaskEntity entrustHistoryEntity) throws ParseException;
    /**
     * 史委托信息 具体详情
     * @param entrustmentId
     * @return
     */
    EntrustAddVo getEntrustHistoryDetail(Long entrustmentId);

    /**
     * 根据检测项ID查询可以做的团队
     * @param checkItemId
     * @return
     */
    List<LabelValueVo> getDept(Integer checkItemId);

    /**
     * 历史委托信息 具体详情 测试
     * @param entrustmentId
     * @return
     */
    EntrustAddVo getEntrustHistoryDetailTest(Long entrustmentId);

    /**
     * 委托发布
     * @param entity
     * @return
     */
    Boolean publishTask(TaskEntity entity);

    /**
     * 分配任务
     * @param entity
     * @return
     */
    Boolean distributionTask(TaskVo entity);

    /**
     * 填充数据
     * @param detail
     * @param object
     */
    XWPFDocument downloadEntrust(EntrustAddVo detail, InputStream object);

    /**
     * 查询委托单位上一次工程名称、工程部位
     * @param name
     * @return
     */
    HistoryEntrustDataVo getHistoryData(String name);


    /**
     * 查询委托单位上一次项目名称、部位
     * 包括 unitData 单位联系人集合
     * @param name
     * @param type
     * @return
     */
    HistoryEntrustDataVo getHistoryData(String name,Integer type);
}

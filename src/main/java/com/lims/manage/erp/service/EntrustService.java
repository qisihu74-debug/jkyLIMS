package com.lims.manage.erp.service;

import com.alibaba.fastjson.JSONException;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.EntrustHistoryTaskEntity;
import com.lims.manage.erp.entity.TaskEntity;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.vo.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
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
     * 修改委托样品测试 new 暂时废弃 （勿删！！）
     */
    Boolean updateEntrustTestNewSample(EntrustAddVo vo);

    /**
     * 修改委托样品测试 new 3.23
     */
    Boolean updateEntrustTestNewSampleEnscript1(EntrustAddVo vo);
    Boolean updateEntrustTestNewSampleEnscript(EntrustAddVo vo);

    /**
     * 修改委托检测项
     * @param vo
     * @return
     */
    Boolean updateEntrustCheckItem(EntrustAddVo vo);

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
     * 再来一单（复制委托单详情）
     * 样品信息：以样品签收中委托单位id相同的信息为准。否则为空。
     * @param entrustmentId
     * @return
     */
    EntrustAddVo getAnotherList(Long entrustmentId);

    /**
     * 分布委托信息 具体详情
     * @param entrustmentId
     * @return
     */
    EntrustAddVo getEntrustDistributionDetail(Long entrustmentId);

    /**
     * 根据检测项ID查询可以做的团队
     * @param checkItemId
     * @return
     */
    List<LabelValueVo> getDept(Integer checkItemId);

    /**
     * 历史委托信息 具体详情
     * 修改委托时 修改样品时 查询详情 线上使用 丁
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
     * 任务发布4.12日修改
     * 修改报告的制作单位选择样式
     * 权限放到task表
     * @param entity
     * @return
     */
    Boolean distributionTask412(TaskVo entity);

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

    /**
     * 样品状态查询 样品状态1待检，2在检，3已检
     * @param sampleId
     * @return
     */
    String findStateBySampleId(int sampleId, EntrustEntityMapper mapper,TaskMapper taskMapper);

    /**
     * 获取委托单模板信息
     * @return
     */
    String getMessage();
    /**
     * 查询产品所有的检测项及检测项的检测依据
     *
     * @param productId
     * @return
     */
    List<CheckItemDetailVo> getCheckItemBasis(Integer productId);

    /**
     * 查询检测项详情：检测项名称，检测项方法，规格型号，检测依据
     *
     * @param ids
     * @return
     */
    List<CheckItemInfoVo> getCheckItemInfo(List<Integer> ids);

    /**
     * 查询当前可出报告科室
     * @param entrustmentId
     * @return
     */
    List<LabelValueVo> getReportTeams(Long entrustmentId);

    /**
     * 修改出报告科室
     * @param entrustmentId
     * @param deptIds
     * @return
     */
    int updateReportTeam(Long entrustmentId,List<Integer> deptIds);

    /**
     * 再来一单（复制委托单详情）
     * 样品信息来源： 以旧委托单下 样品信息详情关联 返回前端时 样品id 伪造
     * @param entrustmentId
     * @return
     */
    EntrustAddVo getAnotherListCopy(Long entrustmentId);

    /**
     * 新增委托_（针对 再来一单的数据保存）
     * @param vo
     * @return
     */
    Boolean addEntrustCopy(EntrustAddVo vo, MultipartFile[] file);

    Long checkEntrustId(Long entrustId);
}

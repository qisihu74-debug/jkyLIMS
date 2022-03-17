package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.EntrustHistoryTaskEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TaskEntity;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/entrust/")
public class EntrustController {

    @Autowired
    private EntrustService entrustService;
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private EntrustEntityMapper entrustEntityMapper;

    /**
     * 新增委托
     *
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/addEntrust")
    //@RequiresPermissions("entrust:entrust:addEntrust")
    public Result addEntrust(@RequestParam("json") String json, MultipartFile[] file) {
        EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
        Boolean isSuccess = entrustService.addEntrust(entrust, file);
        if (isSuccess) {
            return ResultUtil.success();
        } else {
            return ResultUtil.error(678, "新增委托失败！");
        }
    }

    /**
     * 新增委托 测试丁
     *
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/addEntrust_Test")
    //@RequiresPermissions("entrust:entrust:addEntrust")
    public Result addEntrustTest(@RequestParam("json") String json, MultipartFile[] file) {
        EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
        Boolean isSuccess = entrustService.addEntrustTest(entrust, file);
        if (isSuccess) {
            return ResultUtil.success("新建委托成功");
        } else {
            return ResultUtil.error(678, "新增委托失败！");
        }
    }


    /**
     * 修改委托
     *
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/updateEntrust")
//    @RequiresPermissions("entrust:entrust:updateEntrust")
    public Result updateEntrust(@RequestParam("json") String json, MultipartFile[] file) {
        EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
        Boolean isSuccess = entrustService.updateEntrust(entrust, file);
        if (isSuccess) {
            return ResultUtil.success("修改成功");
        } else {
            return ResultUtil.error(678, "修改委托失败！");
        }
    }

    /**
     * 修改委托测试丁 new
     *
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/updateEntrust_test_new")
//    @RequiresPermissions("entrust:entrust:updateEntrust")
    public Result updateEntrustTestNew(@RequestParam("json") String json, MultipartFile[] file) {
        EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
        Boolean isSuccess = entrustService.updateEntrustTestNew(entrust, file);
        if (isSuccess) {
            return ResultUtil.success("修改委托成功");
        } else {
            return ResultUtil.error(678, "修改委托失败！");
        }
    }

    /**
     * 修改委托样品信息
     *
     * @param
     * @return
     */
    @PostMapping("/updateEntrust_test_new_sample")
//    @RequiresPermissions("entrust:entrust:updateEntrust")
    public Result updateEntrustTestNewSample(@RequestBody EntrustAddVo entrust) {
        Boolean isSuccess = entrustService.updateEntrustTestNewSample(entrust);
        if (isSuccess) {
            return ResultUtil.success("修改委托下样品成功");
        } else {
            return ResultUtil.error(678, "修改委托下样品失败！");
        }
    }

    /**
     * 废弃委托单信息
     *
     * @return
     */
    @PostMapping("/abandonEntrust")
    public Result abandonEntrust(@RequestBody EntrustEntity entrustEntity) {
        // 获取当前登录用户id。
        Date date = new Date();
        if (ShiroUtils.getUserInfo() != null) {
            entrustEntity.setOperateUser(ShiroUtils.getUserInfo().getUserId());
        }
        entrustEntity.setOperateDate(date);
        Boolean flag = entrustService.abandonEntrust(entrustEntity);
        if (flag) {
            return ResultUtil.success("成功");
        }
        return ResultUtil.error(678, "作废委托失败！");

    }

    /**
     * @return
     */
    @GetMapping("get_Basics")
    public Result ReturnBasicsData() {
        return ResultUtil.success(entrustService.returnEntrustData());
    }

    /**
     * @param companyId
     * @return
     */
    @GetMapping("get_entrusted_unit")
    public Result methodDispay(Integer companyId) {
        List<TestCustomerJsonEntity> collectList = entrustService.returnTestCustomerEntityList(companyId);
        if (collectList.isEmpty()) {
            return ResultUtil.error(201, "用户信息不存在");
        }
        return ResultUtil.success(collectList);
    }

    /**
     * 新增客户
     *
     * @param testCompanyEntity
     * @return
     */
    @PostMapping("add_new_company")
    public Result methodPost(@RequestBody TestCompanyJsonEntity testCompanyEntity) {
        if (testCompanyEntity.getCompanyName() != null && testCompanyEntity.getType() != null) {
            if (entrustEntityMapper.getCompanyName(testCompanyEntity.getCompanyName(), Integer.parseInt(testCompanyEntity.getType())) != null) {
                return ResultUtil.error(201, "单位名称已存在");
            }
        } else {
            return ResultUtil.error(201, "缺少必填参数");
        }
        boolean BooleStatus = entrustService.addCompanyData(testCompanyEntity);
        if (BooleStatus) {
            return ResultUtil.success();
        }
        return ResultUtil.error(201, "增加数据失败");
    }

    /**
     * 新增委托单位
     *
     * @param testCompanyEntity
     * @return
     */
    @PostMapping("add_new_company_two")
    public Result methodPostTwo(@RequestBody TestCompanyJsonEntity testCompanyEntity) {
        if (testCompanyEntity.getCompanyName() != null && testCompanyEntity.getType() != null) {
            if (entrustEntityMapper.getCompanyName(testCompanyEntity.getCompanyName(), Integer.parseInt(testCompanyEntity.getType())) != null) {
                return ResultUtil.error(201, "单位名称已存在");
            }
        } else {
            return ResultUtil.error(201, "缺少必填参数");
        }
        boolean BooleStatus = entrustService.addCompanyDataTwo(testCompanyEntity);
        if (BooleStatus) {
            return ResultUtil.success();
        }
        return ResultUtil.error(201, "增加数据失败");
    }

    /**
     * 查询检测项详情：检测项名称，检测项方法，规格型号，检测依据
     *
     * @param itemIds
     * @return
     */
    @RequestMapping("/getItemDetail")
    public Result getItemDetail(@RequestBody CheckItemParamVo itemIds) {
        if (itemIds == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(entrustService.getCheckItemInfoVo(itemIds.getIds()));
        }
    }

    /**
     * 查询检测项 方法 依据
     *
     * @param itemId
     * @return
     */
    @RequestMapping("/getItemMethodStandard")
    public Result getItemMethodStandard(Integer itemId) {
        if (itemId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(entrustService.getItemMethodStandard(itemId));
        }
    }

    /**
     * 查询历史委托
     * 1、角色过滤 “客户代表”、“市场部业务员”
     * 2、“客户”指定自身委托
     *
     * @param entrustHistoryEntity
     * @return
     */
    @RequestMapping("/get_entrust_history")
//    @RequiresPermissions("test:entrust:get_entrust_history")
    public Result getEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity) throws ParseException {
        if (entrustHistoryEntity.getPageNum() == null || entrustHistoryEntity.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数");
        }
        if (entrustHistoryEntity.getState() == null) {
            entrustHistoryEntity.setState(0);
        }
        if (entrustHistoryEntity.getState() != 0 && entrustHistoryEntity.getState() != 144 && entrustHistoryEntity.getState() != 1) {
            return ResultUtil.error("必填参数状态有误");
        }
        return ResultUtil.success(entrustService.getEntrustHistoryList(entrustHistoryEntity));
    }

    /**
     * 查询历史委托信息详情
     *
     * @param entrustmentId
     * @return
     */
    @RequestMapping("/get_entrust_history_detail")
    public Result getEntrustHistoryDetail(Long entrustmentId) {
        return ResultUtil.success(entrustService.getEntrustHistoryDetail(entrustmentId));
    }

    /**
     * 根据检测项ID查询可以做该检测项的团队
     *
     * @param checkItemId
     * @return
     */
    @GetMapping("/getDept")
    public Result getDept(Integer checkItemId) {
        return ResultUtil.success(entrustService.getDept(checkItemId));
    }

    /**
     * 查询历史委托信息详情 测试
     *
     * @param id
     * @return
     */
    @GetMapping("/get_entrust_history_detail_test")
    public Result getEntrustHistoryDetailTest(Long id) {
        return ResultUtil.success(entrustService.getEntrustHistoryDetailTest(id));
    }

    /**
     * 委托单发布，转为任务
     *
     * @param entity
     * @return
     */
    @PostMapping("publishTask")
    //@RequiresPermissions("entrust:task:publishTask")
    public Result publishTask(@RequestBody TaskEntity entity) {
        if (entity.getEntrustmentId() == null) {
            return ResultUtil.error(-1, "缺少必要参数");
        }
        //核查委托单位、委托人、委托人联系方式、样品信息、检测项信息是否完整
        EntrustAddVo vo = entrustService.getEntrustHistoryDetail(entity.getEntrustmentId());
        if (StringUtils.isEmpty(vo.getEntrustCompany()) || StringUtils.isEmpty(vo.getEntrustPeople())
                || StringUtils.isEmpty(vo.getEntrustPhone())) {
            return ResultUtil.error(-1, "请检查委托人信息是否完整！");
        }
        List<SampleEntity> samples = vo.getSamples();
        if (CollectionUtils.isEmpty(samples)) {
            return ResultUtil.error(-1, "请检查委托单样品信息是否完整！");
        }
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                if (CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVos())) {
                    return ResultUtil.error(-1, "请检查委托单样品下检测项信息是否完整！");
                }
            }
        }
        Boolean flag = entrustService.publishTask(entity);
        if (flag) {
            /*logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"账户："+ShiroUtils.getUserInfo().getUsername()+"发布任务成功编号为："+vo.getEntrustmentNo(),
                    Const.ENTRUST_PUBLISH,true);*/
            return ResultUtil.success("委托发布成功！");
        } else {
            return ResultUtil.error(-1, "委托发布失败！");
        }
    }


    @PostMapping("distributionTask")
    public Result distributionTask(@RequestBody TaskVo entity) {
        if (entity.getEntrustmentId() == null) {
            return ResultUtil.error(-1, "缺少必要参数");
        }
        //核查委托单位、委托人、委托人联系方式、样品信息、检测项信息是否完整
        EntrustAddVo vo = entrustService.getEntrustHistoryDetail(entity.getEntrustmentId());
        if (StringUtils.isEmpty(vo.getEntrustCompany()) || StringUtils.isEmpty(vo.getEntrustPeople())
                || StringUtils.isEmpty(vo.getEntrustPhone())) {
            return ResultUtil.error(-1, "请检查委托人信息是否完整！");
        }
        List<SampleEntity> samples = vo.getSamples();
        if (CollectionUtils.isEmpty(samples)) {
            return ResultUtil.error(-1, "请检查委托单样品信息是否完整！");
        }
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                if (CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVos())) {
                    return ResultUtil.error(-1, "请检查委托单样品下检测项信息是否完整！");
                }
            }
        }
        if (!CollectionUtils.isEmpty(entity.getCheckItemDeptVoList())) {
            for (CheckItemDeptVo checkItemDeptVo : entity.getCheckItemDeptVoList()) {
                if (checkItemDeptVo.getDeptId() == null) {
                    return ResultUtil.error(-1, "请确认所有检测项是否分配科室！");
                }
            }
        }
        Boolean flag = entrustService.distributionTask(entity);
        if (flag) {
            /*logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"账户："+ShiroUtils.getUserInfo().getUsername()+"发布任务成功编号为："+vo.getEntrustmentNo(),
                    Const.ENTRUST_PUBLISH,true);*/
            return ResultUtil.success("委托分配成功！");
        } else {
            return ResultUtil.error(-1, "委托分配失败！");
        }
    }

    /**
     * 委托单任务待发布列表
     *
     * @param entrustHistoryEntity
     * @return
     */
    @RequestMapping("/releasedList")
//    @RequiresPermissions("test:entrust:releasedList")
    public Result getEntrustReleasedList(EntrustHistoryTaskEntity entrustHistoryEntity) throws ParseException {
        if (entrustHistoryEntity.getPageNum() == null || entrustHistoryEntity.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数");
        }
        return ResultUtil.success(entrustService.getEntrustReleasedList(entrustHistoryEntity));
    }

    /**
     * 委托单下载
     *
     * @param entrustId
     * @return
     */
    @RequestMapping("downloadEntrust")
    public void downloadEntrust(Long entrustId, HttpServletResponse response) {
        String fileName = "BD20210021.docx";
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_entrust_template, fileName);
            //填充数据
            EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
            XWPFDocument document = entrustService.downloadEntrust(detail, object);
            response.reset();
            response.setHeader("Access-Control-Expose-Headers","Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            document.write(outputStream);
            outputStream.close();
        } catch (Exception ex) {
            log.info("导出失败：", ex.getMessage());
        }
    }

    /**
     * 查询委托单位上一次工程名称、工程部位
     *
     * @param name
     * @return
     */
    @GetMapping("/getHistoryData")
    public Result getHistoryData(String name) {
        if (name == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            HistoryEntrustDataVo historyData = entrustService.getHistoryData(name);
            if (historyData == null) {
                historyData = new HistoryEntrustDataVo();
            }
            return ResultUtil.success(historyData);
        }
    }


    /**
     * 查询委托单位上一次项目名称、部位
     * 包括 unitData 单位联系人集合
     *
     * @param companyName
     * @param type
     * @return
     */
    @GetMapping("/getHistoryData_two")
    public Result getHistoryDataTwo(String companyName, String type) {
        if (companyName == null || type == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            HistoryEntrustDataVo historyData = entrustService.getHistoryData(companyName, Integer.parseInt(type));
            if (historyData == null) {
                historyData = new HistoryEntrustDataVo();
            }
            return ResultUtil.success(historyData);
        }
    }
}

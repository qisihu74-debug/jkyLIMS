package com.lims.manage.erp.service.impl;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.EntrustHistoryTaskEntity;
import com.lims.manage.erp.entity.EntrustPamentEntity;
import com.lims.manage.erp.entity.EntrustSampleEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TaskEntity;
import com.lims.manage.erp.entity.TestCompanyEntity;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.entity.TestInitDataEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.ProductItemEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TeamMapper;
import com.lims.manage.erp.mapper.TestCompanyDao;
import com.lims.manage.erp.mapper.TestCustomerDao;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.*;
import io.minio.errors.MinioException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EntrustServiceImpl implements EntrustService {

    Logger logger = LoggerFactory.getLogger(EntrustServiceImpl.class);
    @Autowired
    private EntrustEntityMapper entityMapper;
    @Autowired
    TestCompanyDao testCompanyDao;
    @Autowired
    TestCustomerDao testCustomerDao;
    @Autowired
    SampleEntityMapper sampleEntityMapper;
    @Autowired
    TestProductDao testProductDao;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ProductItemEntityMapper itemEntityMapper;
    @Autowired
    private TeamMapper teamMapper;

    public static HttpHeaders getHttpHeaders(String fileName) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("UTF-8"), "iso-8859-1"));
        return headers;
    }

    /**
     * 新增委托任务
     *
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean addEntrust(EntrustAddVo vo, MultipartFile[] file) {
        //存放委托基本信息==》test_entrusted
        EntrustEntity basisInfo = new EntrustEntity(vo);
        basisInfo.setId(GenID.getID());
        //设置委托编号
        Integer code = null;
        String currentTime = DateUtil.getTodayString().substring(0, 6);
        //获取当前最大样品编号
        Integer entrustNum = entityMapper.selectMaxNo();
        if (entrustNum != null && entrustNum > 0) {
            String substring = entrustNum.toString().substring(0, 6);
            if (substring.equals(currentTime)) {
                code = entrustNum + 1;
            } else {
                code = Integer.parseInt(currentTime + "0001");
            }
        } else {
            code = Integer.parseInt(currentTime + "0001");
        }
        basisInfo.setEntrustmentNo(code);
        //附件存在上传附件到服务器
        if (file != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (MultipartFile multipartFile : file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, multipartFile, code + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                basisInfo.setFileUrl(substring);
            }
        }
        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        int totalMoney = 0;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                sampleEntityMapper.updateSampleUse(sampleEntity.getId(), 1);
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                List<Integer> standardFileIds = sampleEntity.getStandardFileIds();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (Integer integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer);
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    //计算检测项总价钱
                    for (SampleItemEntity entity : sampleCheckItem) {
                        int money = entity.getTimes() * entity.getUnitPrice();
                        totalMoney = totalMoney + money;
                    }
                    //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                    for (SampleItemEntity entity : sampleCheckItem) {
                        entity.setSampleId(sampleEntity.getId());
                        entity.setEntrustId(basisInfo.getId());
                    }
                    entityMapper.BatchSaveEntrustSampleItem(sampleCheckItem);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }


        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
//            pamentEntity.setOperator(ShiroUtils.getUserInfo().getUsername());
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        //得到总价钱，再保存委托基本信息
        basisInfo.setCountPrice(totalMoney + "");
        entityMapper.insertEntrustInfo(basisInfo);
        return true;
    }

    /**
     * 新增委托 测试
     *
     * @param vo
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean addEntrustTest(EntrustAddVo vo, MultipartFile[] file) {

        //存放委托基本信息==》test_entrusted
        EntrustEntity basisInfo = new EntrustEntity(vo);
        basisInfo.setId(GenID.getID());
        //设置委托编号
        Integer code = null;
        String currentTime = DateUtil.getTodayString().substring(0, 6);
        //获取当前最大样品编号
        Integer entrustNum = entityMapper.selectMaxNo();
        if (entrustNum != null && entrustNum > 0) {
            String substring = entrustNum.toString().substring(0, 6);
            if (substring.equals(currentTime)) {
                code = entrustNum + 1;
            } else {
                code = Integer.parseInt(currentTime + "0001");
            }
        } else {
            code = Integer.parseInt(currentTime + "0001");
        }
        basisInfo.setEntrustmentNo(code);
        // 通过委托编号 查询是否存在
        if (entityMapper.getByData(basisInfo.getEntrustmentNo()) != null) {
            return false;
        }
        //附件存在上传附件到服务器
        if (file != null) {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringfileUrlStr = new StringBuilder();
            for (MultipartFile multipartFile : file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, multipartFile, code + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
                // 存放上传文件的名称带后缀如：（委托文档资料.pdf,原始文档.docx）
                stringfileUrlStr.append(name);
                stringfileUrlStr.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                basisInfo.setFileUrl(substring);
            }
            String fileUrlStr = stringfileUrlStr.toString();
            if (!StringUtils.isEmpty(fileUrlStr)) {
                String substring = fileUrlStr.substring(0, fileUrlStr.length() - 1);
                basisInfo.setFileUrlStr(substring);
            }
        }
        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        int totalMoney = 0;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                sampleEntityMapper.updateSampleUse(sampleEntity.getId(), 1);
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                List<Integer> standardFileIds = sampleEntity.getStandardFileIds();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (Integer integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer);
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    //计算检测项总价钱
                    for (SampleItemEntity entity : sampleCheckItem) {
                        if (entity.getUnitPrice() >= 0 || entity.getUnitPrice() != null) {
                            int money = entity.getTimes() * entity.getUnitPrice();
                            totalMoney = totalMoney + money;
                        }
                    }
                    //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                    for (SampleItemEntity entity : sampleCheckItem) {
                        entity.setSampleId(sampleEntity.getId());
                        entity.setEntrustId(basisInfo.getId());
                    }
                    entityMapper.BatchSaveEntrustSampleItem(sampleCheckItem);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }


        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
//            pamentEntity.setOperator(ShiroUtils.getUserInfo().getUsername());
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        //得到总价钱，再保存委托基本信息
        basisInfo.setCountPrice(totalMoney + "");
        basisInfo.setState(0);
        entityMapper.insertEntrustInfo(basisInfo);
        return true;
    }


    /**
     * 修改委托
     *
     * @param vo
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEntrust(EntrustAddVo vo, MultipartFile[] file) {
        EntrustEntity basisInfo = new EntrustEntity(vo);
        Integer code = vo.getEntrustmentNo();
        //附件存在上传附件到服务器
        if (file != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (MultipartFile multipartFile : file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                // 去清除 MinIo 桶数据。
                MinIoUtil.deleteFile(BucketsConst.buckets_entrust_enclosure, code + "." + strings[strings.length - 1]);
                String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, multipartFile, code + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                basisInfo.setFileUrl(substring);
            }
        }
        // 刪除的样品id集合
//        List<Integer>  removeSamplesId =  entityMapper.getSampleIdSet(basisInfo.getId());
        // 删除样品id
        entityMapper.removeTestEntrustedSampleDetailsRel(basisInfo.getId());
        //修改样品为未使用
        List<Integer> sampleIds = entityMapper.getSampleId(basisInfo.getId());
        if (!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                sampleEntityMapper.updateSampleUse(sampleId, 0);
            }
        }
        // 删除判定依据id
        entityMapper.removeTestEntrustedSampleStandardRel(basisInfo.getId());
        // 删除缴费信息
        entityMapper.removeTestEntrustedPaymentRecordInfo(basisInfo.getId());
        // 样品下检测依据
        entityMapper.removeTestEntrustedSampleCheckitemRel(basisInfo.getId());

        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        int totalMoney = 0;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                List<Integer> standardFileIds = sampleEntity.getStandardFileIds();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (Integer integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer);
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    //计算检测项总价钱
                    for (SampleItemEntity entity : sampleCheckItem) {
                        int money = entity.getTimes() * entity.getUnitPrice();
                        totalMoney = totalMoney + money;
                    }
                    //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                    for (SampleItemEntity entity : sampleCheckItem) {
                        entity.setSampleId(sampleEntity.getId());
                        entity.setEntrustId(basisInfo.getId());
                    }

                    entityMapper.BatchSaveEntrustSampleItem(sampleCheckItem);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }

        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
//            pamentEntity.setOperator(ShiroUtils.getUserInfo().getUsername());
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        //得到总价钱，再保存委托基本信息
        basisInfo.setPaymentCount(totalMoney + "");
        //存放委托基本信息==》test_entrusted
        entityMapper.updateEntrustInfo(basisInfo);
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean updateEntrustTestNew(EntrustAddVo vo, MultipartFile[] file) {
        EntrustEntity basisInfo = new EntrustEntity(vo);
        Integer code = vo.getEntrustmentNo();
        //附件存在上传附件到服务器
        if (file != null) {
            // 查询委托单下 文件信息(entrustData.getFileUrl(),entrustData.getFileUrlStr(),获取后缀进行删除操作)
            EntrustAddVo entrustData = entityMapper.selectByKeyId(basisInfo.getId());
            if (entrustData.getFileUrl() != null && !entrustData.getFileUrl().isEmpty()) {
                // 去清除 MinIo 桶数据。
                try {
                    String[] strings2 = entrustData.getFileUrlStr().split(",");
                    for (int i = 0; i < strings2.length; i++) {
                        String[] strings3 = strings2[i].split("\\.");
                        MinIoUtil.deleteFile(BucketsConst.buckets_entrust_enclosure, code + "." + strings3[strings2.length - 1]);
                    }
                } catch (Exception e) {
                    logger.info("修改委托下清除 MinIo 桶数据 出错");
                }
            }

            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringfileUrlStr = new StringBuilder();
            for (MultipartFile multipartFile : file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, multipartFile, code + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
                // 存放上传文件的名称带后缀如：（委托文档资料.pdf,原始文档.docx）
                stringfileUrlStr.append(name);
                stringfileUrlStr.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                basisInfo.setFileUrl(substring);
            }
            String fileUrlStr = stringfileUrlStr.toString();
            if (!StringUtils.isEmpty(fileUrlStr)) {
                String substring = fileUrlStr.substring(0, fileUrlStr.length() - 1);
                basisInfo.setFileUrlStr(substring);
            }
        }
        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
//            pamentEntity.setOperator(ShiroUtils.getUserInfo().getUsername());
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        //存放委托基本信息==》test_entrusted
        entityMapper.updateEntrustInfo(basisInfo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEntrustTestNewSample(EntrustAddVo vo) {
        EntrustEntity basisInfo = new EntrustEntity(vo);
        // 删除样品id
        // 统计是否存在
        if (entityMapper.countSampleDetailsRel(basisInfo.getId()) > 0) {
            entityMapper.removeTestEntrustedSampleDetailsRel(basisInfo.getId());
        }
        //修改样品为未使用
        List<Integer> sampleIds = entityMapper.getSampleId(basisInfo.getId());
        if (!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                sampleEntityMapper.updateSampleUse(sampleId, 0);
            }
        }
        // 删除判定依据id
        if (entityMapper.countSampleStandardRel(basisInfo.getId()) > 0) {
            entityMapper.removeTestEntrustedSampleStandardRel(basisInfo.getId());
        }
        // 删除缴费信息
//        entityMapper.removeTestEntrustedPaymentRecordInfo(basisInfo.getId());
        // 样品下检测依据
        if (entityMapper.countSampleCheckitemRel(basisInfo.getId()) > 0) {
            entityMapper.removeTestEntrustedSampleCheckitemRel(basisInfo.getId());
        }

        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        int totalMoney = 0;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                sampleEntityMapper.updateSampleUse(sampleEntity.getId(), 1);
                // 样品依据
                List<JudgmentBasisVo> standardFileIds = sampleEntity.getStandardFileIdStr();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (JudgmentBasisVo integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer.getStandardId());
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<JudgmentBasisVo> sampleCheckItem = sampleEntity.getJudgmentBasisVoStr();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    //计算检测项总价钱
                    for (JudgmentBasisVo entity : sampleCheckItem) {
                        if (entity.getCheckPrice() != null && !entity.getCheckPrice().equals("")) {
                            int money = entity.getTimes() * Integer.parseInt(entity.getCheckPrice());
                            totalMoney = totalMoney + money;
                        }
                    }
                    //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                    for (JudgmentBasisVo entity : sampleCheckItem) {
                        entity.setSampleId(sampleEntity.getId());
                        entity.setId(basisInfo.getId());
                    }
                    // 判定依据集合转成 符合数据存储 样品依据
                    List<SampleItemEntity> sampleItemList = new ArrayList<>();
                    for (JudgmentBasisVo entity : sampleCheckItem) {
                        SampleItemEntity sampleItemEntity = new SampleItemEntity();
                        sampleItemEntity.setSampleId(entity.getSampleId());
                        sampleItemEntity.setEntrustId(entity.getId());
                        if (entity.getMethodId() != null && entity.getMethodId() >= 0) {
                            sampleItemEntity.setMethodId(entity.getMethodId());
                        }
                        if (entity.getCheckItemId() != null && entity.getCheckItemId() >= 0) {
                            sampleItemEntity.setCheckItemId(entity.getCheckItemId().longValue());
                        }
                        if (entity.getStandardId() != null && entity.getStandardId() >= 0) {
                            sampleItemEntity.setStandardId(entity.getStandardId().intValue());
                        }
                        sampleItemEntity.setTimes(entity.getTimes());
                        if (entity.getCheckPrice() != null && !entity.getCheckPrice().equals("")) {
                            sampleItemEntity.setUnitPrice(Integer.parseInt(entity.getCheckPrice()));
                        }
                        sampleItemList.add(sampleItemEntity);
                    }
                    entityMapper.BatchSaveEntrustSampleItem(sampleItemList);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }


        if (totalMoney != 0) {
            //得到总价钱，再保存委托基本信息
            basisInfo.setPaymentCount(totalMoney + "");
            //存放委托基本信息==》test_entrusted
            entityMapper.updateEntrustInfo(basisInfo);
        }

        return true;
    }

    @Override
    public Boolean abandonEntrust(EntrustEntity entrustEntity) {
        entrustEntity.setState(144);
        entityMapper.updateEntrustInfo(entrustEntity);
        List<Integer> sampleIds = entityMapper.getSampleId(entrustEntity.getId());
        if (!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                sampleEntityMapper.updateSampleUse(sampleId, 0);
            }
        }
        return true;
    }

    @Override
    public List<CheckItemInfoVo> getCheckItemInfoVo(List<Integer> ids) {
        return itemEntityMapper.getItemInfo2(ids);
    }

    @Override
    public Map<String, List<LabelValueVo>> getItemMethodStandard(Integer id) {
        Map<String, List<LabelValueVo>> result = Maps.newHashMap();
        List<LabelValueVo> itemMethod = itemEntityMapper.getItemMethod(id);
        List<LabelValueVo> itemStandard = itemEntityMapper.getItemStandard(id);
        result.put("itemMethod", itemMethod);
        result.put("itemStandard", itemStandard);
        return result;
    }

    @Override
    public Map<String, List<LabelValueVo>> returnEntrustData() {
        Map<String, List<LabelValueVo>> map = new HashMap<>();
        // type =1 委托单位
        List<LabelValueVo> EntrustCompany = testCompanyDao.selectEntrustCompanyList(1);
        // type =2 见证单位
        List<LabelValueVo> witnessCompany = testCompanyDao.selectEntrustCompanyList(2);
        List<TestInitDataEntity> ReturnBasisData = testCompanyDao.selectEntrustBasis();
        // 团队信息
        List<LabelValueVo> arryTeam = testCompanyDao.selectTestTeam();
//        1 = 委托方式： 2=取样方式 ： 3=检测目的：4 ： 取报告方式 5：样品外观  6：签章类型： 7：用户来源： 8：设备类型  9：支付方式
        // 委托方式 type =1
        List<LabelValueVo> arryEntrust = new ArrayList<>();
        // 取样方式 type =2
        List<LabelValueVo> arrySampling = new ArrayList<>();
        // 3=检测目的
        List<LabelValueVo> arryCheckout = new ArrayList<>();
        // 4=取报告方式
        List<LabelValueVo> arryGetReport = new ArrayList<>();
        // 5：样品外观
        List<LabelValueVo> arrySampleAppearance = new ArrayList<>();
        // 6：签章类型：
        List<LabelValueVo> arrySeal = new ArrayList<>();
        // 8：设备类型
        List<LabelValueVo> arryEquipment = new ArrayList<>();
        // 11：支付方式
        List<LabelValueVo> arryPayment = new ArrayList<>();
        for (TestInitDataEntity testInitDataEntity : ReturnBasisData) {
            LabelValueVo labelValueVo = new LabelValueVo();
            labelValueVo.setLabel(testInitDataEntity.getName());
            labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
            switch (testInitDataEntity.getType()) {
                case 1:
                    arryEntrust.add(labelValueVo);
                    break;
                case 2:
                    arrySampling.add(labelValueVo);
                    break;
                case 3:
                    arryCheckout.add(labelValueVo);
                    break;
                case 4:
                    arryGetReport.add(labelValueVo);
                    break;
                case 5:
                    arrySampleAppearance.add(labelValueVo);
                    break;
                case 6:
                    arrySeal.add(labelValueVo);
                    break;
                case 11:
                    arryPayment.add(labelValueVo);
                    break;
                default:
                    break;
            }
        }
        map.put("entrustCompany", EntrustCompany);
        map.put("witnessCompany", witnessCompany);
        map.put("arryEntrust", arryEntrust);
        map.put("arrySampling", arrySampling);
        map.put("arryCheckout", arryCheckout);
        map.put("arryGetReport", arryGetReport);
        map.put("arrySampleAppearance", arrySampleAppearance);
        map.put("arrySeal", arrySeal);
        map.put("arryTeam", arryTeam);
        map.put("arryPayment", arryPayment);
        return map;
    }

    @Override
    public List<TestCustomerJsonEntity> returnTestCustomerEntityList(Integer companyId) {
        return testCompanyDao.selectPeopleInformation(companyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addCompanyData(TestCompanyJsonEntity testCompanyEntity) {
        TestCompanyEntity testCompanyEntity1 = new TestCompanyEntity();
        testCompanyEntity1.setCompanyName(testCompanyEntity.getCompanyName());
        testCompanyEntity1.setType(testCompanyEntity.getType());
        int statusNumber = testCompanyDao.insert(testCompanyEntity1);
        if (statusNumber >= 1) {
            TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
            testCustomerEntity.setCompanyId(testCompanyEntity1.getCompanyId());
            testCustomerEntity.setContacts(testCompanyEntity.getContacts());
            testCustomerEntity.setPhone(testCompanyEntity.getContactWay());
            int addCustomer = testCustomerDao.insertTestCustomer(testCustomerEntity);
            if (addCustomer >= 1) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<EntrustHistoryEntity> getEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity) throws ParseException {
        if (entrustHistoryEntity.getDateInterval() != null) {
            String[] strArry = entrustHistoryEntity.getDateInterval().split("~");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i <= strArry.length - 1; i++) {
                if (i == 0) {
                    entrustHistoryEntity.setStartDate(dateFormat.parse(strArry[i]));
                }
                if (i == 1) {
                    entrustHistoryEntity.setEndingDate(dateFormat.parse(strArry[i]));
                }
            }
        }
        // 获取状态
        if (entrustHistoryEntity.getState() == 1) {
            return entityMapper.selectEntrustHistoryListRelease_of(entrustHistoryEntity);
        }
        return entityMapper.selectEntrustHistoryList(entrustHistoryEntity);
    }

    @Override
    public List<EntrustHistoryTaskEntity> getEntrustReleasedList(EntrustHistoryTaskEntity entrustHistoryEntity) throws ParseException {
        entrustHistoryEntity.setState(0);
        if (entrustHistoryEntity.getDateInterval() != null) {
            String[] strArry = entrustHistoryEntity.getDateInterval().split("~");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i <= strArry.length - 1; i++) {
                if (i == 0) {
                    entrustHistoryEntity.setStartDate(dateFormat.parse(strArry[i]));
                }
                if (i == 1) {
                    entrustHistoryEntity.setEndingDate(dateFormat.parse(strArry[i]));
                }
            }
        }

        return entityMapper.selectEntrustReleasedList(entrustHistoryEntity);
    }

    @Override
    public EntrustAddVo getEntrustHistoryDetail(Long entrustmentId) {
        // 通过委托ID 委托单信息 → test_entrusted_info
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustmentId);
        // 通过委托单id 获取缴费记录 依据id 同价价格
        entrustAddVo.setPaymentRecord(entityMapper.getTestEntrustedPaymentRecordInfoPrice(entrustmentId));
        // -- 支付方式。
//        entrustAddVo.setPaymentMethod(entityMapper.getTestEntrustedInfoMethodName(entrustmentId));
        // 联系地址
//        entrustAddVo.setAdress(entityMapper.getEntrustingParty(entrustmentId));
        // 通过委托ID 样品集合 → test_sample
        List<SampleEntity> sampleCollection = sampleEntityMapper.selectSampleListGroup(entrustmentId);
        // 样品信息 进行补充 检测依据集合，检测项集合
        for (SampleEntity sampleEntity : sampleCollection) {
            // 样品下 检测项、检测依据 补充。
            // 根据 委托单状态 进行选择项查询 0&&144 查询默认部门信息 state =1 查询所属指定部门信息
            if (entrustAddVo.getState() == 0 || entrustAddVo.getState() == 144) {
                List<JudgmentBasisVo> list = sampleEntityMapper.selectTestStandardList(sampleEntity.getId(), entrustmentId);
                if (list != null && !list.isEmpty()) {
                    // 根据检测项id 查询 默认匹配部门信息
                    for (JudgmentBasisVo data : list) {
                        List<String> strings = sampleEntityMapper.getTeamNameStrings(data.getCheckItemId());
                        data.setTestingRoom(strings.toString());
                    }
                    sampleEntity.setJudgmentBasisVos(list);
                }
            } else {
                sampleEntity.setJudgmentBasisVos(sampleEntityMapper.selectTestStandardList(sampleEntity.getId(), entrustmentId));
            }

            // 补充样品下 依据集合
            sampleEntity.setStandardFileIds(sampleEntityMapper.getSampleBasisSet(sampleEntity.getId(), entrustAddVo.getId()));
        }
        entrustAddVo.setSamples(sampleCollection);
        return entrustAddVo;
    }

    @Override
    public List<LabelValueVo> getDept(Integer checkItemId) {
        return entityMapper.getDept(checkItemId);
    }

    @Override
    public EntrustAddVo getEntrustHistoryDetailTest(Long entrustmentId) {
        // 通过委托ID 委托单信息 → test_entrusted_info
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustmentId);
        // 通过委托单id 获取缴费记录 依据id 同价价格
        entrustAddVo.setPaymentRecordShow(entityMapper.getTestEntrustedPaymentRecordInfoPrice(entrustmentId));
        // -- 支付方式。
//        entrustAddVo.setPaymentMethod(entityMapper.getTestEntrustedInfoMethodName(entrustmentId));
        // 联系地址
//        entrustAddVo.setAdress(entityMapper.getEntrustingParty(entrustmentId));
        // 通过委托ID 样品集合 → test_sample
        List<SampleEntity> sampleCollection = sampleEntityMapper.selectSampleListGroup(entrustmentId);
        // 样品信息 进行补充 检测依据集合，检测项集合
        for (SampleEntity sampleEntity : sampleCollection) {
            // 样品下 检测项、检测依据 补充。
            List<JudgmentBasisVo> listJson = sampleEntityMapper.selectTestStandardList(sampleEntity.getId(), entrustmentId);
            sampleEntity.setJudgmentBasisVoStr(listJson);
            // 补充样品下 依据集合
            List<JudgmentBasisVo> standardList = sampleEntityMapper.getSampleBasisList(sampleEntity.getId(), entrustAddVo.getId());
            sampleEntity.setStandardFileIdStr(standardList);
        }
        entrustAddVo.setSamples(sampleCollection);
        return entrustAddVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishTask(TaskEntity entity) {
        //根据委托单生成任务单
        entity.setId(GenID.getID());
        //设置任务编号由团队代码（单字符英文字母）+年月(4字符)+“-”+三位流水号（3字符），如A2108-015。
        //团队编号这部分在团队表中，任务编号生成时不考虑，展示时拼接上即可
        //获取当前最大样品编号
        Integer code = null;
        Integer entrustNum = taskMapper.selectMaxNo();
        String currentTime = DateUtil.getTodayString().substring(2, 6);
        if (entrustNum != null && entrustNum > 0) {
            String substring = entrustNum.toString().substring(0, 4);
            if (substring.equals(currentTime)) {
                code = entrustNum + 1;
            } else {
                code = Integer.parseInt(currentTime + "001");
            }
        } else {
            code = Integer.parseInt(currentTime + "001");
        }
        entity.setCode(code.toString());
        if (!StringUtils.isEmpty(entity.getTeamId())) {
            //设置接收人为团队副团长
            List<SysUserEntity> userEntity = teamMapper.getUsersByTid(entity.getTeamId());
            if (!CollectionUtils.isEmpty(userEntity)) {
                for (SysUserEntity sysUserEntity : userEntity) {
                    if (sysUserEntity.getPosition().equals(Const.SYS_MANAGER_LOG)) {
                        entity.setReceiver(sysUserEntity.getUsername());
                    }
                }
                entity.setReceiveTime(new java.sql.Date(System.currentTimeMillis()));
            }
        }
        //任务单保存
        taskMapper.save(entity);
        //更新委托单状态
        taskMapper.updateEntrustById(entity.getEntrustmentId(), 1);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean distributionTask(TaskVo entity) {
        List<Long> deptIds = Lists.newArrayList();
        List<CheckItemDeptVo> checkItemDeptVoList = entity.getCheckItemDeptVoList();
        Long dept = null;
        for (CheckItemDeptVo vo: checkItemDeptVoList) {
            if(!deptIds.contains(vo.getDeptId())){
                deptIds.add(vo.getDeptId());
            }
            Integer issueReport = vo.getIssueReport();
            if(issueReport == 1){
                dept = vo.getDeptId();
            }
        }
        //创建任务对象
        List<TaskVo> vos = Lists.newArrayList();
        for (Long deptId:deptIds) {
            TaskVo vo = new TaskVo();
            vo.setId(GenID.getID());
            String teamCode = taskMapper.getTeamCode(deptId);
            Integer integer = taskMapper.selectMaxNoByCode(teamCode);
            Integer code = null;
            if(integer == null){
                String currentTime = DateUtil.getTodayString().substring(2, 6);
                code = Integer.parseInt(currentTime+"001");
            }else{
                code = integer+1;
            }
            String codeStr = code + "";
            vo.setDeptId(deptId);
            vo.setCode(codeStr);
            vo.setTaskCode(teamCode+codeStr.substring(0,3)+"-"+codeStr.substring(4,7));
            vo.setEntrustmentId(entity.getEntrustmentId());
            vo.setRequiredCompletionTime(entity.getRequiredCompletionTime());
            vo.setState(1);
            if(deptId.equals(dept)){
                vo.setIssueReport(1);
            }else{
                vo.setIssueReport(0);
            }
            vos.add(vo);
        }

        for (int i = 0; i < vos.size(); i++) {
            System.out.println("任务对象："+vos.get(i));
        }

        for (int i = 0; i < entity.getCheckItemDeptVoList().size(); i++) {
            System.out.println("检测项信息："+entity.getCheckItemDeptVoList().get(i));
        }
        //任务单保存
        taskMapper.batchSave(vos);
        //更新检测项信息
        taskMapper.batchUpdateCheckItem(entity.getCheckItemDeptVoList());
        return true;
    }

    @Override
    public XWPFDocument downloadEntrust(EntrustAddVo detail, InputStream object) {
        XWPFDocument doc = null;
        try {
            doc = new XWPFDocument(object);
            List<XWPFTable> tables = doc.getTables();
            List<XWPFTableRow> rows;
            XWPFTable table = tables.get(0);
            //表格属性
            CTTblPr pr = table.getCTTbl().getTblPr();
            //获取表格对应的行
            rows = table.getRows();
            //设置模板数据
            rows.get(3).getTableCells().get(2).setText(detail.getEntrustCompany());//委托单位
            rows.get(4).getTableCells().get(2).setText(detail.getWitnessUint());//见证单位
            rows.get(5).getTableCells().get(2).setText(detail.getProjectPart());//工程部位
            rows.get(6).getTableCells().get(2).setText(detail.getProjectName());//工程名称
            //设置样品信息
            List<SampleEntity> samples = detail.getSamples();
            int sampleIndex = 8;
            int index = 1;
            for (int i = 0; i < samples.size(); i++) {
                rows.get(sampleIndex).getTableCells().get(index).setText(samples.get(i).getSampleName());//样品名称
                rows.get(sampleIndex).getTableCells().get(index + 1).setText(samples.get(i).getSpecs());//规格等级
                rows.get(sampleIndex).getTableCells().get(index + 2).setText(samples.get(i).getBatchNumber());//批号/编号
                rows.get(sampleIndex).getTableCells().get(index + 3).setText(samples.get(i).getSampleGroups().toString());//样品数量
                rows.get(sampleIndex).getTableCells().get(index + 4).setText(samples.get(i).getGeneration());//代表批量
                rows.get(sampleIndex).getTableCells().get(index + 5).setText(samples.get(i).getManufacturer());//样品产地/生产厂家
                sampleIndex = sampleIndex + 1;
            }
            //设置其它信息
            String ss = "";
            rows.get(14).getTableCells().get(2).setText(detail.getPresentInformation() == null ? "--" : detail.getPresentInformation());//提供资料
            rows.get(15).getTableCells().get(2).setText(detail.getSamplingMethod() == null ? "--" : detail.getSamplingMethod());//取样方式
            rows.get(15).getTableCells().get(4).setText(detail.getCheckPurpose() == null ? "--" : detail.getCheckPurpose());//检验目的
            Integer productId = samples.get(0).getProductId();
            List<String> list = entityMapper.getStatndardByPId(productId);
            StringBuilder stringBuilder = new StringBuilder();
            if (!CollectionUtils.isEmpty(list)) {
                for (String s : list) {
                    stringBuilder.append(s);
                    stringBuilder.append(",");
                }
                String substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                rows.get(15).getTableCells().get(6).setText(substring == null ? "--" : substring);//产品标准
            }
            StringBuilder stringBuilder1 = new StringBuilder();
            if (!CollectionUtils.isEmpty(samples)) {
                for (SampleEntity entity : samples) {
                    List<JudgmentBasisVo> sampleCheckItem = entity.getJudgmentBasisVos();
                    for (JudgmentBasisVo itemEntity : sampleCheckItem) {
                        String name = itemEntity.getCheckItemName();
                        stringBuilder1.append(name);
                        stringBuilder1.append("(");
                        //TODO 检测项的检测依据
                        stringBuilder1.append("GB2021-2001");
                        stringBuilder1.append(")");
                        stringBuilder1.append(",");
                    }
                }
                String substring = stringBuilder1.toString().substring(0, stringBuilder1.length() - 1);
                rows.get(16).getTableCells().get(2).setText(substring == null ? "--" : substring);//检验项目及检测依据
            }
            rows.get(17).getTableCells().get(2).setText(detail.getReportCount().toString());//报告分数
            rows.get(17).getTableCells().get(4).setText(detail.getReportType() == null ? "--" : detail.getReportType());//取报告方式
            rows.get(17).getTableCells().get(6).setText(detail.getAddress() == null ? "--" : detail.getAddress());//收报告单位
            rows.get(18).getTableCells().get(2).setText(detail.getAddress() == null ? "--" : detail.getAddress());//联系地址
            rows.get(18).getTableCells().get(4).setText(detail.getAddressee() == null ? "--" : detail.getAddressee());//联系人
            rows.get(18).getTableCells().get(6).setText(detail.getMobile() == null ? "--" : detail.getMobile());//联系方式
            rows.get(19).getTableCells().get(2).setText(detail.getEntrustPeople() == null ? "--" : detail.getEntrustPeople());//委托人
            rows.get(19).getTableCells().get(4).setText(detail.getEntrustPhone() == null ? "--" : detail.getEntrustPhone());//委托人电话
            rows.get(19).getTableCells().get(6).setText(detail.getWitnessPerson() == null ? "--" : detail.getWitnessPerson());//见证人
            SampleEntity sampleEntity = samples.get(0);
            if (sampleEntity != null) {
                String s = sampleEntity.getSampleName() + "(" + sampleEntity.getSpecs() + "," + sampleEntity.getOutward() + ")";
                rows.get(20).getTableCells().get(2).setText(s == null ? "--" : s);//样品状态
            }
            rows.get(20).getTableCells().get(4).setText(detail.getIsSave().equals("1") ? "保留" : "废弃");//样品保留
            rows.get(21).getTableCells().get(2).setText(detail.getPaymentCount() == null ? "--" : detail.getPaymentCount());//检验收费
            rows.get(21).getTableCells().get(4).setText(detail.getPaymentMethod() == null ? "--" : detail.getPaymentMethod());//支付方式
            //TODO 本次缴费统计缴费记录表
            rows.get(21).getTableCells().get(6).setText(detail.getPaymentRecord() == null ? "--" : detail.getPaymentRecord());//本次交费
            rows.get(22).getTableCells().get(2).setText(DateUtil.formatDate(detail.getRequestDate()));//完成期限
            rows.get(22).getTableCells().get(4).setText(detail.getBusinessAcceptor() == null ? "--" : detail.getBusinessAcceptor());//业务受理人
            rows.get(22).getTableCells().get(6).setText(DateUtil.formatDate(detail.getAcceptanceDate()));//受理日期
        } catch (Exception e) {
            logger.error("设置委托单信息到模板异常:{}", e);
        }
        return doc;
    }

    @Override
    public HistoryEntrustDataVo getHistoryData(String name) {
        return entityMapper.getHistoryData(name);
    }

}

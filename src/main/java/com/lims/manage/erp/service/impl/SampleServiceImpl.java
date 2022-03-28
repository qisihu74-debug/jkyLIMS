package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.service.SampleService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class SampleServiceImpl implements SampleService {
    @Autowired
    TestProductDao testProductDao;

    @Autowired
    SampleEntityMapper sampleEntityMapper;

    @Override
    public Integer addSampleData(SampleAddParamVo addParamVo, MultipartFile[] file) {
        int result = 0;
        String insertFlag = System.currentTimeMillis() + "";
        //查询产品名称
        String productName = testProductDao.getProductNameById(addParamVo.getSampleName());
        //处理详情数据
        List<SampleAddDetailVo> details = addParamVo.getDetails();
        //获取数据库当前年份最大的样品编号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date now = new Date();
        String maxNumber = sampleEntityMapper.getMaxNumber(sdf.format(now));
        int newMax;
        if (maxNumber == null) {
            newMax = 0;
        } else {
            newMax = Integer.parseInt(maxNumber);
        }
        if (details != null) {
            for (int i = 0; i < details.size(); i++) {
                //生成样品编号
                StringBuilder code = new StringBuilder("YP-" + sdf.format(now) + "-");
                String sampleCode;
                if (addParamVo.getSampleGroups() > 1) {
                    //组数大于1，将前n个命名为
                    if (i % addParamVo.getQuantityPerGroup() == 0) {
                        newMax = newMax + 1;
                    }
                    String num = String.format("%0" + 4 + "d", newMax);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i % addParamVo.getQuantityPerGroup() + 1);
                    sampleCode = prefix + "_" + suffix;
                } else {
                    //生成样品编号
                    String num = String.format("%0" + 4 + "d", newMax + 1);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i + 1);
                    if (addParamVo.getQuantityPerGroup() > 1) {
                        sampleCode = prefix + "_" + suffix;
                    } else {
                        sampleCode = prefix.toString();
                    }
                }
                //保存样品图片

                MultipartFile multipartFile = null;
                int pictureName = i + 1;
                String suffix = "";
                if (file.length > 0) {//有上传文件
                    for (int j = 0; j < file.length; j++) {
                        String originalFilename = file[j].getOriginalFilename();
                        String[] split = originalFilename.split("\\.");
                        if ((pictureName + "").equals(split[0])) {
                            suffix = split[1];
                            multipartFile = file[j];
                            break;
                        }
                    }
                }
                String pictureFileName = null;
                if (!"".equals(suffix)) {
                    pictureFileName = sampleCode + "." + suffix;
                }
                String pictureUrl = null;
                if (multipartFile != null) {
                    pictureUrl = MinIoUtil.upload("test-sample", multipartFile, pictureFileName);
                }
                //保存样品信息
                SampleEntity sampleEntity = new SampleEntity(addParamVo, details.get(i), productName, sampleCode, pictureUrl, insertFlag);
                result = sampleEntityMapper.insert(sampleEntity);
            }
        } else {
            StringBuilder code = new StringBuilder("YP-" + sdf.format(now) + "-");
            //生成样品编号
            String num = String.format("%0" + 4 + "d", newMax + 1);
            StringBuilder sampleCode = code.append(num);
            SampleEntity sampleEntity = new SampleEntity(addParamVo, null, productName, sampleCode.toString(), null, insertFlag);
            result = sampleEntityMapper.insert(sampleEntity);
        }
        return result;
    }

    @Override
    public List<SamplePublicInfoVo> getSamplePublicInfos(SampleDetailVo paramVo) {
        if (paramVo.getReceivedDate() != null) {
            String[] split = paramVo.getReceivedDate().split("~");
            paramVo.setBeginDate(split[0]);
            paramVo.setEndDate(split[1]);
        }
        return sampleEntityMapper.getSamplePublicInfos(paramVo);
    }

    @Override
    public PageInfo getSamplePublicInfos2(SampleDetailVo paramVo) {
        PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
        if (paramVo.getReceivedDate() != null) {
            String[] split = paramVo.getReceivedDate().split("~");
            paramVo.setBeginDate(split[0]);
            paramVo.setEndDate(split[1]);
        }
        List<SamplePublicInfoVo> list = sampleEntityMapper.getSamplePublicInfos1(paramVo);
        List<String> insertFlags = Lists.newArrayList();
        for (SamplePublicInfoVo vo:list) {
            insertFlags.add(vo.getInsertFlag());
        }
        List<SamplePrivateInfoVo> samplePrivateInfos1 = sampleEntityMapper.getSamplePrivateInfos1(insertFlags);
        for (SamplePublicInfoVo vo:list) {
            String insertFlag = vo.getInsertFlag();
            List<SamplePrivateInfoVo> childNode = Lists.newArrayList();
            for (SamplePrivateInfoVo privateInfoVo: samplePrivateInfos1) {
                String insertFlag1 = privateInfoVo.getInsertFlag();
                if(insertFlag.equals(insertFlag1)){
                    childNode.add(privateInfoVo);
                }
            }
            vo.setChildNode(childNode);
        }
        PageInfo<SamplePublicInfoVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public List<SampleEntity> getSampleDataList(SampleEntity sampleEntity) {
        return sampleEntityMapper.selectSampleList(sampleEntity);
    }

    @Override
    public PageInfo getSampleDataList2(SampleEntity sampleEntity) {
        PageHelper.startPage(sampleEntity.getPageNum(), sampleEntity.getPageSize());
        if (sampleEntity.getReceivedDate() != null) {
            String[] split = sampleEntity.getReceivedDate().split("~");
            sampleEntity.setBeginDate(split[0]);
            sampleEntity.setEndDate(split[1]);
        }
        List<SampleEntity> list = sampleEntityMapper.selectSampleList(sampleEntity);
        List<SampleEntity> sampleEntities = sampleEntityMapper.selectSampleList(sampleEntity);
//        List<SampleEntrustAddVo> sampleEntrustAddVos = sampleEntityMapper.selectSampleList(sampleEntity);
        PageInfo<SampleEntity> pageInfo = new PageInfo<>(sampleEntities);
        return pageInfo;
    }
    @Override
    public PageInfo getSampleDataListNew(SampleEntity sampleEntity) {
        PageHelper.startPage(sampleEntity.getPageNum(), sampleEntity.getPageSize());
        if (sampleEntity.getReceivedDate() != null) {
            String[] split = sampleEntity.getReceivedDate().split("~");
            sampleEntity.setBeginDate(split[0]);
            sampleEntity.setEndDate(split[1]);
        }
        List<SampleEntrustAddVo> topList = sampleEntityMapper.selectSampleListTop(sampleEntity);
        List<String> codes = Lists.newArrayList();
        for (SampleEntrustAddVo sampleEntrustAddVo : topList) {
            codes.add(sampleEntrustAddVo.getCode());
        }
        if(!CollectionUtils.isEmpty(codes)){
            List<SampleEntity> groupNode = sampleEntityMapper.getGroupNode(codes);
            for (SampleEntrustAddVo sampleEntrustAddVo : topList) {
                List<SampleEntity> samples = Lists.newArrayList();
                for (SampleEntity sampleEntity1 : groupNode) {
                    if(sampleEntrustAddVo.getCode().equals(sampleEntity1.getSampleCode().substring(0,12))){
                        samples.add(sampleEntity1);
                    }
                }
                sampleEntrustAddVo.setSamples(samples);
            }
        }

        PageInfo<SampleEntrustAddVo> pageInfo = new PageInfo<>(topList);
        return pageInfo;
    }

    @Override
    public SampleDetailVo getSampleGroupInfo(String insertFlag) {
        return sampleEntityMapper.getSampleGroupInfo(insertFlag);
    }

    @Override
    public List<SampleDetailVo> selectSampleList2(SampleEntity sampleEntity) {
        return sampleEntityMapper.selectSampleList2(sampleEntity);
    }

    @Override
    public int updateSampleInfo(SampleEntity record) {
        return sampleEntityMapper.updateSampleInfo(record);
    }

    @Override
    public SampleDetailVo getSampleTagInfo(Integer sampleId) {
        return sampleEntityMapper.getSampleTagInfo(sampleId);
    }
}

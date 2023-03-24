package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.SopStandardINstructionDao;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;


@Service("sopStandardInstructionService")
public class SopStandardInstructionServiceImpl extends ServiceImpl<SopStandardINstructionDao, SopStandardInstruction> implements SopStandardInstructionService {

    Logger logger = LoggerFactory.getLogger(SopStandardInstructionServiceImpl.class);

    /*日志*/
    @Resource
    private LogManagerService logManagerService;
    @Resource
    private TestProductDao productDao;
    @Resource
    private TestInstrumentService testInstrumentService;
    @Resource
    private TestProductItemService testProductItemService;
    @Resource
    private TestTeamService testTeamService;
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;


    @Resource
    private SopStandardInstructionService instructionService;


    @Override
    public Result addStandardInstruction(SopStandardInstruction instruction, MultipartFile file) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }

        //更新设备，产品，产品检测项，团队名称相关信息
        instruction = saveInstruction(instruction);

        if (file != null && !file.isEmpty()) {
            instruction.setFileUrl(saveFile(file));
        }

        if (instructionService.save(instruction)) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    "用户：" + userInfo.getUsername() + "添加SOP标准作业指导书" + instruction.getId() + "成功!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, true);
            return ResultUtil.success("添加成功!");
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    "用户：" + userInfo.getUsername() + "添加SOP标准作业指导书" + instruction.getId() + "失败!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, false);
            return ResultUtil.error("添加失败!");
        }
    }


    //设备，产品，产品检测项，团队名称查询
    public SopStandardInstruction saveInstruction(SopStandardInstruction instruction) {

        //产品
        if (StringUtils.isNotBlank(instruction.getProductId())) {
            StringBuilder productStr = new StringBuilder();
            String[] strings = instruction.getProductId().split(",");
            for (String str : strings) {
                productStr.append(productDao.getProductNameById(Integer.valueOf(str))).append(",");
            }
            instruction.setProduct(productStr.deleteCharAt(productStr.length() - 1).toString());
        }

        //设备
        if (StringUtils.isNotBlank(instruction.getFacilityId())) {
            StringBuilder nameStr = new StringBuilder();
            String[] strings = instruction.getFacilityId().split(",");
            for (String str : strings) {
                QueryWrapper<TestInstrument> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("del_flag", 0);
                queryWrapper.eq("id", str);
                TestInstrument testInstrument = testInstrumentService.getOne(queryWrapper);
                if (testInstrument.getName() != null) {
                    nameStr.append(testInstrument.getName()).append(",");
                }
            }
            instruction.setFacility(nameStr.deleteCharAt(nameStr.length() - 1).toString());
        }

        //产品检测项/产品参数
        if (StringUtils.isNotBlank(instruction.getProductParameterId())) {
            StringBuilder productParameterStr = new StringBuilder();
            String[] strings = instruction.getProductParameterId().split(",");
            for (String str : strings) {
                QueryWrapper<TestProductItem> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("del_flag", 0);
                queryWrapper.eq("check_item_id", str);
                TestProductItem testProductItem = testProductItemService.getOne(queryWrapper);
                if (testProductItem.getCheckItemName() != null) {
                    productParameterStr.append(testProductItem.getCheckItemName()).append(",");
                }
            }
            instruction.setProductParameter(productParameterStr.deleteCharAt(productParameterStr.length() - 1).toString());

        }

        //团队
        if (StringUtils.isNotBlank(instruction.getPositionId())) {
            StringBuilder positionStr = new StringBuilder();
            String[] strings = instruction.getPositionId().split(",");
            for (String str : strings) {
                QueryWrapper<TestTeam> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("del_flag", 0);
                queryWrapper.eq("id", str);
                TestTeam testTeam = testTeamService.getOne(queryWrapper);

                positionStr.append(testTeam.getName()).append(",");
            }
            instruction.setPosition(positionStr.deleteCharAt(positionStr.length() - 1).toString());
        }

        instruction.setDelFlag(0);
        instruction.setCreateTime(new Date());

        return instruction;
    }


    public String saveFile(MultipartFile file) {
        String url = null;

        try {
            String originalFilename = file.getOriginalFilename();
            //获取文件名
            String fileName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            String reportCode = fileName + ".pdf";

            //上传的是doc/docx转为pdf
            if (originalFilename.contains(".pdf")) {
                url = MinIoUtil.upload("sop-standard-instruction", file, reportCode);
            } else {
                String temporary = qiYueSuoEntity.getAutographPath() + reportCode;
                //word转pdf
                File fi = new File(temporary);
                //创建临时文件
                fi.createNewFile();
                PDFHelper3.doc2pdf(file, qiYueSuoEntity.getAutographPath() + reportCode);
                InputStream inputStream = FileUtils.openInputStream(fi);
                url = MinIoUtil.upload("sop-standard-instruction", reportCode,
                        inputStream, "application/octet-stream");
            }
        } catch (Exception e) {
            logger.debug("上传文件失败:{}", e);
        }
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        try {
            url = URLDecoder.decode(url, "utf-8");//调用方法解析字符串
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        return "http://121.89.242.0:9000/report-download/"+reportCode;
        return url;
    }


    @Override
    public Result delStandardInstruction(String id) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }

        SopStandardInstruction instruction = getById(id);
        instruction.setDelFlag(1);
        if (this.updateById(instruction)) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    "用户：" + userInfo.getUsername() + "删除SOP标准作业指导书" + instruction.getId() + "成功!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, true);
            return ResultUtil.success("删除成功");

        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    "用户：" + userInfo.getUsername() + "删除SOP标准作业指导书" + instruction.getId() + "失败!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, false);
            return ResultUtil.error("删除失败!");

        }

    }

    @Override
    public Result updStandardInstruction(SopStandardInstruction instruction, MultipartFile file) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (instruction.getSopId() == null) {
            return ResultUtil.error("SOP编号不能为空");
        }
        if (instruction.getSopName() == null) {
            return ResultUtil.error("SOP名称不能为空");
        }

        //更新设备，产品，产品检测项，团队名称相关信息
        instruction = saveInstruction(instruction);

        if (file != null && !file.isEmpty()) {
            instruction.setFileUrl(saveFile(file));
        }

        if (this.updateById(instruction)) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    "用户：" + userInfo.getUsername() + "修改SOP标准作业指导书" + instruction.getId() + "成功!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, true);
            return ResultUtil.success("修改成功");

        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    "用户：" + userInfo.getUsername() + "修改SOP标准作业指导书" + instruction.getId() + "失败!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, false);
            return ResultUtil.error("修改失败!");

        }

    }
}

package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.SopStandardInstruction;
import com.lims.manage.erp.result.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * SOP标准作业指导书业务层
 */
public interface SopStandardInstructionService extends IService<SopStandardInstruction> {

    Result addStandardInstruction(SopStandardInstruction instruction, MultipartFile file);

    Result delStandardInstruction(String id);

    Result updStandardInstruction(SopStandardInstruction instruction, MultipartFile file);


}

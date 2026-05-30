package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.TestSkillList;
import com.lims.manage.erp.mapper.TestSkillListDao;
import com.lims.manage.erp.service.TestSkillListService;
import org.springframework.stereotype.Service;

/**
 * 技术人员技能表(TestSkillList)表服务实现类
 *
 * @author makejava
 * @since 2022-02-23 09:14:46
 */
@Service("testSkillListService")
public class TestSkillListServiceImpl extends ServiceImpl<TestSkillListDao, TestSkillList> implements TestSkillListService {

}


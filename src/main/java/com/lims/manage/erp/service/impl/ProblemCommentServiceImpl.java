package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.ProblemComment;
import com.lims.manage.erp.mapper.ProblemCommentDao;
import com.lims.manage.erp.service.ProblemCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 问答评论业务层实现类
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class ProblemCommentServiceImpl extends ServiceImpl<ProblemCommentDao, ProblemComment> implements ProblemCommentService {
}

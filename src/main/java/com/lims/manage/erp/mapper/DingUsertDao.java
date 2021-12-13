package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.vo.UserInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/11/22 10:52
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface DingUsertDao extends BaseMapper<DingUserEntity> {
    /**
     * 更新用户手机，邮箱等信息
     * @param vo
     * @return
     */
    Boolean updateDingUserInfo(UserInfoVo vo);
}

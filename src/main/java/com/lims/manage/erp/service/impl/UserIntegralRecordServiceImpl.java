package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.CommonConstant;
import com.lims.manage.erp.entity.IntegralRule;
import com.lims.manage.erp.entity.UserIntegralInfo;
import com.lims.manage.erp.entity.UserIntegralRecord;
import com.lims.manage.erp.mapper.UserIntegralRecordDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.IntegralRuleService;
import com.lims.manage.erp.service.UserIntegralInfoService;
import com.lims.manage.erp.service.UserIntegralRecordService;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 用户获取积分记录业务层实现类
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class UserIntegralRecordServiceImpl extends ServiceImpl<UserIntegralRecordDao, UserIntegralRecord> implements UserIntegralRecordService {

    @Resource
    private IntegralRuleService integralRuleService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserIntegralInfoService userIntegralInfoService;

    @Override
    public Result<?> getSignInStatus() {
        //获取当前登陆用户id
        String userId = ShiroUtils.getUserInfo().getUserId().toString();
        //通过key获取redis缓存
        String signInKey = CommonConstant.CACHE_INTEGRAL_RULE_SIGN_IN + userId;
        Object countObj = redisTemplate.opsForValue().get(signInKey);
        if (countObj == null) {
            return ResultUtil.success("用户还未签到", false);
        } else {
            return ResultUtil.success("用户已签到", true);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> signIn() {
        //根据当前登录用户进行签到
        //获取当前登陆用户id
        String userId = ShiroUtils.getUserInfo().getUserId().toString();
        Integer resultInt = addUserIntegralRecord(CommonConstant.INTEGRAL_RULE_SIGN_IN, CommonConstant.CACHE_INTEGRAL_RULE_SIGN_IN, userId, null, null);
        switch (resultInt) {
            case 1:
                return ResultUtil.success("用户签到成功");
            case 2:
                return ResultUtil.error("超出规则频次限制");
            default:
                return ResultUtil.error("没有对应的规则信息");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addUserIntegralRecord(String ruleId, String ruleCacheId, String userId, String eventType, String eventId) {
        return  addUserIntegralRecord(ruleId,ruleCacheId,userId,eventType,eventId,null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addUserIntegralRecord(String ruleId, String ruleCacheId, String userId, String eventType, String eventId, Integer integralNum) {
        log.info("添加用户积分信息:ruleId:{},ruleCacheId:{},userId:{},eventType:{},eventId:{}",ruleId,ruleCacheId,userId,eventType,eventId);
        //根据积分规则id获取积分的信息
        IntegralRule integralRule = integralRuleService.getById(ruleId);
        //如果有规则信息
        if (integralRule != null) {
            //该规则是否有每日频次限制
            if (ruleCacheId!=null && integralRule.getFrequency() != null) {
                //如果有限制
                //通过key获取redis缓存
                String signInKey = ruleCacheId + userId;
                Object countObj = redisTemplate.opsForValue().get(signInKey);
                int count = 0;
                if (countObj != null) {
                    count = Integer.parseInt(countObj.toString());
                }
                //判断缓存中的次数是否小于规则频次
                if (count < integralRule.getFrequency()) {
                    count = count + 1;
                    //缓存数据进行变更
                    redisTemplate.opsForValue().set(signInKey, String.valueOf(count), getNowToNextDaySeconds(), TimeUnit.SECONDS);
                } else {
                    return 2;
                }
            }
            //添加获取记录
            UserIntegralRecord userIntegralRecord = new UserIntegralRecord();
            userIntegralRecord.setUserId(userId);
            userIntegralRecord.setIntegralType(integralRule.getIntegralType());
            if(integralNum==null){
                integralNum=integralRule.getIntegralNum();
            }
            userIntegralRecord.setIntegralNum(integralNum);
            userIntegralRecord.setRuleId(integralRule.getRuleId());
            userIntegralRecord.setGainTime(new Date());
            //如果有事件类型和事件id 则进行设置
            if (StringUtils.isNotEmpty(eventType)) {
                if (StringUtils.isNotEmpty(eventId)) {
                    userIntegralRecord.setEventType(eventType);
                    userIntegralRecord.setEventId(eventId);
                }
            }
            baseMapper.insert(userIntegralRecord);
            //根据用户id和积分类型获取用户积分信息
            LambdaQueryWrapper<UserIntegralInfo> userIntegralWrapper = Wrappers.lambdaQuery();
            userIntegralWrapper.eq(UserIntegralInfo::getUserId, userId);
            userIntegralWrapper.eq(UserIntegralInfo::getIntegralType, integralRule.getIntegralType());
            UserIntegralInfo userIntegralInfo = userIntegralInfoService.getOne(userIntegralWrapper);
            if (userIntegralInfo == null) {
                userIntegralInfo = new UserIntegralInfo();
                userIntegralInfo.setUserId(userId);
                userIntegralInfo.setIntegralType(integralRule.getIntegralType());
                userIntegralInfo.setIntegralNum(0);
            }
            userIntegralInfo.setIntegralNum(userIntegralInfo.getIntegralNum() + integralNum);
            //更新或添加用户积分信息
            userIntegralInfoService.saveOrUpdate(userIntegralInfo);
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 计算第二天凌晨与当前时间的时间差秒数
     *
     * @param
     * @return java.lang.Long
     * @author shy
     * @date 2021/3/12 18:10
     */
    public static Long getNowToNextDaySeconds() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
    }

}

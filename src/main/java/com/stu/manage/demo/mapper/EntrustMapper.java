package com.stu.manage.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stu.manage.demo.entity.EntrustInfo;
import com.stu.manage.demo.entity.EntrustStat;
import com.stu.manage.demo.entity.SampleStatus;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface EntrustMapper extends BaseMapper<EntrustStat> {

    /**
     * 再来一单--委托基本信息
     * @param entrustId
     * @return
     */
    EntrustInfo onceMore(int entrustId);

    /**
     * 检测委托下的样品状态
     * @param id
     * @return
     */
    List<SampleStatus> getSampleStat(Integer id);
}

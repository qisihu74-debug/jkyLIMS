package com.lims.manage.erp.util;

import com.lims.manage.erp.vo.EntrustCategoryVo;
import org.springframework.util.StringUtils;

/**
 * @Author: DLC
 * @Date: 2022/9/27 11:47
 *
 */
public class EntrustNoStrUtils {
    /**
     * 用于拆分委托编号信息
     * @param EntrustNoStr
     * @return
     */
    public static EntrustCategoryVo splitEntrustNo(String EntrustNoStr){
        EntrustCategoryVo entrustCategoryVo = new EntrustCategoryVo();
        // 处理委托编号String信息类型
        if(!StringUtils.isEmpty(EntrustNoStr)){
            // 查询委托编号 != null、对 编号类型值进行赋值
            String MN = "MN";
            String BD = "BD";
            if(EntrustNoStr.contains(MN)){
                entrustCategoryVo.setEntrustCategoryType(MN);
                // 拆分委托编号 : "MD10086" 10086进行存储
                String[] entrustNostrs = EntrustNoStr.split(MN);
                if(entrustNostrs.length>0){
                    if(!StringUtils.isEmpty(entrustNostrs[1])){
                        entrustCategoryVo.setEntrustmentNo(Integer.parseInt(entrustNostrs[1]));
                    }
                }
            } else if(EntrustNoStr.contains(BD)){
                entrustCategoryVo.setEntrustCategoryType(BD);
                // 拆分委托编号 : "MD10086" 10086进行存储
                String[] entrustNostrs = EntrustNoStr.split(BD);
                if(entrustNostrs.length>0){
                    if(!StringUtils.isEmpty(entrustNostrs[1])){
                        entrustCategoryVo.setEntrustmentNo(Integer.parseInt(entrustNostrs[1]));
                    }
                }
            } else {
                entrustCategoryVo.setEntrustCategoryType(null);
                entrustCategoryVo.setEntrustmentNo(Integer.parseInt(EntrustNoStr));
            }
        }
        return entrustCategoryVo;
    }
}

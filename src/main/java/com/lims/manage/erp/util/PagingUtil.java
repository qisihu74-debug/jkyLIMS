package com.lims.manage.erp.util;

import com.github.pagehelper.PageInfo;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2023/3/1 16:33
 */
public class PagingUtil {


    /**
     * 进行逻辑分页
     * @param pageNumData
     * @param pageSizeData
     * @param dataList
     * @return
     */
    public static PageInfo pagingUtilityList(Integer pageNumData, Integer pageSizeData, List<Object> dataList) {
        int pageNum;
        int pageSize;
        int startRow;
        int endRow;
        int pages;
        List<Object> subList = new ArrayList<>();
        List<Object> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dataList)) {
            list1 = (List<Object>) dataList.get(0);
        }
        pageNum = pageNumData <= 1 ? 1 : pageNumData;
        pageSize = pageSizeData <= 1 ? 1 : pageSizeData;
        if (CollectionUtils.isEmpty(list1)) {
            PageInfo pageInfo = new PageInfo();
            pageInfo.setList(list1);
            return pageInfo;
        }
        int totalCount = list1.size();
        if (totalCount <= pageSize) {
            pages = 1;
        } else {
            if (totalCount % pageSize > 0) {
                pages = totalCount / pageSize + 1;
            } else {
                pages = totalCount / pageSize;
            }
        }
        startRow = pageNum > 0 ? (pageNum - 1) * pageSize : 0;
        endRow = startRow + pageSize * (pageNum > 0 ? 1 : 0);
        if (endRow > totalCount) {
            endRow = totalCount;
        }
        PageInfo pageInfo = new PageInfo();
        try {
            subList = list1.subList(startRow, endRow);
        } catch (Exception e) {
            // 如果端点索引值超出范围
            System.out.println("如果端点索引值超出范围\t" + e);
            subList = list1.subList(1, endRow);
        }
        pageInfo.setPages(pages);
        pageInfo.setList(subList);
        pageInfo.setTotal(list1.size());
        return pageInfo;
    }
}

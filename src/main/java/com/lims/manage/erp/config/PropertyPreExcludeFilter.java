package com.lims.manage.erp.config;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

/**
 * 排除JSON敏感属性
 *
 * @author gjl
 */
public class PropertyPreExcludeFilter extends SimplePropertyPreFilter
{
    public PropertyPreExcludeFilter()
    {
    }

    public PropertyPreExcludeFilter addExcludes(String... filters)
    {
        for (int i = 0; i < filters.length; i++)
        {
            this.getExcludes().add(filters[i]);
        }
        return this;
    }
}

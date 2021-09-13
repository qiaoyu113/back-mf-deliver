package com.mfexpress.rent.deliver.base;

import tk.mybatis.mapper.common.ConditionMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;
import tk.mybatis.mapper.common.base.select.SelectMapper;


public interface BaseMapper<T> extends MySqlMapper<T>, Mapper<T>, ConditionMapper<T>, SelectMapper<T> {
}

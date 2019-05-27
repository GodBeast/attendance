package com.xkp.attendance.support;

import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.ConditionMapper;
import tk.mybatis.mapper.common.ExampleMapper;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.MySqlMapper;

@RegisterMapper
public interface BaseMapper<T> extends tk.mybatis.mapper.common.BaseMapper<T>, MySqlMapper<T>, IdsMapper<T>, ConditionMapper<T>, ExampleMapper<T> {

}

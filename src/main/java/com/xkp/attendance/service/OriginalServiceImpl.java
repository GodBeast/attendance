package com.xkp.attendance.service;

import com.xkp.attendance.entity.Original;
import com.xkp.attendance.mapper.OriginalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-23 15:54
 **/
@Service
public class OriginalServiceImpl implements OriginalService{
    @Autowired
    private OriginalMapper originalMapper;

    @Override
    public List<Original> findByStatus(Integer status) {
        Example example = new Example(Original.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", status);
        example.orderBy("createTime").asc();
        return originalMapper.selectByExample(example);
    }
}

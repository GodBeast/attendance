package com.xkp.attendance.service;

import com.xkp.attendance.entity.ClockIn;
import com.xkp.attendance.entity.Original;
import com.xkp.attendance.mapper.ClockInMapper;
import com.xkp.attendance.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-23 14:33
 **/
@Service
public class ClockInServiceImpl implements ClockInService{

    @Autowired
    private ClockInMapper clockInMapper;

    @Override
    public void transfer() {

    }

    @Override
    public List<Original> findByStatus(Integer status) {
        return null;
    }

    @Override
    public List<ClockIn> findByDateAndType(Date date, Integer type) {
        return findByUserAndType("", date, type);
    }

    @Override
    public List<ClockIn> findByUserAndType(String empNo, Date date, Integer type) {
        Example example = new Example(ClockIn.class);
        Example.Criteria criteria = example.createCriteria();
        if(!StringUtils.isEmpty(empNo)){
            criteria.andEqualTo("empNo", empNo);
        }
        criteria.andGreaterThanOrEqualTo("clockInDate", date);
        criteria.andLessThanOrEqualTo("clockInDate", DateUtil.getDateEnd(date));
        criteria.andEqualTo("type", type);
        return clockInMapper.selectByExample(example);
    }

}

package com.xkp.attendance.service;

import com.xkp.attendance.entity.ClockIn;
import com.xkp.attendance.entity.Original;

import java.util.Date;
import java.util.List;

public interface ClockInService {

    void transfer();

    List<Original> findByStatus(Integer status);

    List<ClockIn> findByDateAndType(Date date, Integer type);

    List<ClockIn> findByUserAndType(String employeeNo, Date date, Integer type);
}

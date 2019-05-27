package com.xkp.attendance.service;

import com.xkp.attendance.entity.Original;

import java.util.List;

public interface OriginalService {

    List<Original> findByStatus(Integer status);
}

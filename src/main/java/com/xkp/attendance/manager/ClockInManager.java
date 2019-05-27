package com.xkp.attendance.manager;

import com.xkp.attendance.mapper.ClockInMapper;
import com.xkp.attendance.mapper.OriginalMapper;
import com.xkp.attendance.utils.CommonUtils;
import com.xkp.attendance.entity.ClockIn;
import com.xkp.attendance.entity.Original;
import com.xkp.attendance.service.ClockInService;
import com.xkp.attendance.service.OriginalService;
import com.xkp.attendance.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-23 15:53
 **/
@Component
@Slf4j
public class ClockInManager {

    @Autowired
    private ClockInService clockInService;

    @Autowired
    private OriginalService originalService;

    @Autowired
    private ClockInMapper clockInMapper;

    @Autowired
    private OriginalMapper originalMapper;

    @Transactional
    public void transfer() {
        List<Original> originals = originalService.findByStatus(0);
        if (CollectionUtils.isEmpty(originals)) {
            log.info("没有待解析的数据");
        }

        originals.parallelStream().forEach(x -> {
            ClockIn clockIn = createClockIn(x);
            int insert = clockInMapper.insertSelective(clockIn);
            if(insert > 0){
                x.setStatus(1);
                x.setUpdateTime(new Date());
                originalMapper.updateByPrimaryKey(x);
            }
        });
    }

    private ClockIn createClockIn(Original o){
        ClockIn clockIn = new ClockIn();
        String original = o.getOriginal();
        String type = CommonUtils.getClockInType(original);
        if("B1".equals(type)){
            clockIn.setType(1);
        }else if("B2".equals(type)){
            clockIn.setType(2);
        }else {
            clockIn.setType(0);
        }
        clockIn.setEmpNo(CommonUtils.getEmpNo(original));
        Date clockInDate = CommonUtils.paseDate(CommonUtils.getClockInTime(original));
        clockIn.setClockInDate(clockInDate);
        clockIn.setMonth(Integer.parseInt(DateUtil.getMonth(clockInDate)));

        clockIn.setOriginalId(o.getId());
        return clockIn;

    }
}

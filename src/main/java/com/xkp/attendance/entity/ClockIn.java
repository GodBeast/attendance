package com.xkp.attendance.entity;

import lombok.Data;

import java.util.Date;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-19 10:55
 **/
@Data
public class ClockIn {

    private Integer id;

    /**
     * 员工考勤编号
     */
    private String empNo;

    /**
     * 打卡类型
     */
    private Integer type;

    /**
     * 打卡时间
     */
    private Date clockInDate;

    private Integer originalId;

    private Date createTime;


}

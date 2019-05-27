package com.xkp.attendance.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-19 10:55
 **/
@Data
@Table(name="clockIn")
public class ClockIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "SELECT LAST_INSERT_ID()")
    private Integer id;

    /**
     * 员工考勤编号
     */
    @Column(name="empNo")
    private String empNo;

    /**
     * 打卡类型
     */
    private Integer type;

    /**
     * 打卡时间
     */
    @Column(name = "clockInDate")
    private Date clockInDate;

    /**
     * 关联的原始数据id
     */
    @Column(name = "originalId")
    private Integer originalId;

    @Column(name = "createTime")
    private Date createTime;

    /**
     * 打卡月份
     */
    private Integer month;


}

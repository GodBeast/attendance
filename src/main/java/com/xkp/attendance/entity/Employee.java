package com.xkp.attendance.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-18 23:13
 **/
@Data
@Accessors(chain = true)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "SELECT LAST_INSERT_ID()")
    private  Integer id;

    @Column(name = "userCode")
    private Integer userCode;

    @Column(name = "username")
    private String username;

    private Integer type;

    /**
     * 薪资等级
     */
    @Column(name = "payLevel")
    private String payLevel;

    @Column(name = "cnName")
    private String cnName;

    @Column(name = "costCenter")
    private String costCenter;

    /**
     * 基本工资
     */
    @Column(name = "basicWage")
    private BigDecimal basicWage;

    /**
     * 月薪
     */
    private BigDecimal salary;

    private String cost;

}

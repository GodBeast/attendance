package com.xkp.attendance.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-18 23:20
 **/
@Data

public class Original {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "SELECT LAST_INSERT_ID()")
    private  Integer id;

    /**
     * 原始数据串
     */
    private  String original;

    @Column(name = "createTime")
    private Date createTime;

    @Column(name = "updateTime")
    private Date updateTime;

    /**
     * 状态：0，导入；1，已解析
     */
    private Integer status;

}

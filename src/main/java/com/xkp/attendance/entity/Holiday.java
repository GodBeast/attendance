package com.xkp.attendance.entity;

import lombok.Data;

import javax.persistence.Column;
import java.util.Date;

/**
 * @program: attendance1
 * @description:
 * @author: xkp
 * @create: 2019-06-19 21:51
 **/
@Data
public class Holiday {

    private Integer id;

    @Column(name="holidayDate")
    private Date holidayDate;

    //0:未知；1:双倍工资；2:三倍工资； 9:补班
    private Integer type;
}

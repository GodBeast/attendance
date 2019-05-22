package com.xkp.attendance.entity;

import lombok.Data;

import java.util.Date;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-18 23:20
 **/
@Data
public class Original {

    private  Integer id;

    /**
     * 原始数据串
     */
    private  String original;

    private Date createTime;

    /**
     * 状态：0，导入；1，已解析
     */
    private Integer status;

}

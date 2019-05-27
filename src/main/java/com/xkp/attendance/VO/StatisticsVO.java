package com.xkp.attendance.VO;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-25 21:49
 **/
@Data
@Accessors(chain = true)
public class StatisticsVO {

    private Integer userCode;

    private String username;

    //员工类型
    private Integer type;

    //打卡日期
    private Date clockInDate;

    //考勤状态:0正常；1，缺卡，2：迟到 3:早退
    private Integer clockInStatus;

    //上班打卡时间
    private Date clockInStartDate;

    //下班打卡时间
    private Date clockInEndDate;

    //加班时长
    private BigDecimal overtime;

    //是否补贴
    private Boolean subsidy;


}

package com.xkp.attendance.VO;

import com.xkp.attendance.utils.excel.annotation.ExcelField;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Title: AttendanceDataExcel
 * @ProjectName attendance
 * @Description: TODO
 * @Author xiaoli
 * @Date 2019-05-29 14:34
 */
public class AttendanceDataExcel implements Serializable {
    /**
     * 用户名称
     */
    private String username;
    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 员工类型
     */
    private Integer type;
    /**
     * 员工类型
     */
    private String typeName;

    /**
     * 打卡日期
     */
    private String clockInDate;

    /**
     * 考勤状态:0正常；1，缺卡，2：迟到 3:早退
     */
    private Integer clockInStatus;

    private String clockInStatusName;

    /**
     * 上班打卡时间
     */
    private String clockInStartDate;

    /**
     * 下班打卡时间
     */
    private String clockInEndDate;

    /**
     * 加班时长
     */
    private BigDecimal overtime;

    /**
     * 是否补贴
     */
    private Boolean subsidy;


    @ExcelField(title = "姓名", align = 2, sort = 1)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @ExcelField(title = "员工类型", align = 2, sort = 4)
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @ExcelField(title = "打卡日期", align = 2, sort = 6)
    public String getClockInDate() {
        return clockInDate;
    }

    public void setClockInDate(String clockInDate) {
        this.clockInDate = clockInDate;
    }

    public Integer getClockInStatus() {
        return clockInStatus;
    }

    public void setClockInStatus(Integer clockInStatus) {
        this.clockInStatus = clockInStatus;
    }

    public String getClockInStatusName() {
        return clockInStatusName;
    }

    @ExcelField(title = "考勤状态", align = 2, sort = 8)
    public void setClockInStatusName(String clockInStatusName) {
        this.clockInStatusName = clockInStatusName;
    }

    @ExcelField(title = "上班打卡时间", align = 2, sort = 10)
    public String getClockInStartDate() {
        return clockInStartDate;
    }

    public void setClockInStartDate(String clockInStartDate) {
        this.clockInStartDate = clockInStartDate;
    }

    @ExcelField(title = "下班打卡时间", align = 2, sort = 12)
    public String getClockInEndDate() {
        return clockInEndDate;
    }

    public void setClockInEndDate(String clockInEndDate) {
        this.clockInEndDate = clockInEndDate;
    }

    @ExcelField(title = "加班时长", align = 2, sort = 14)
    public BigDecimal getOvertime() {
        return overtime;
    }

    public void setOvertime(BigDecimal overtime) {
        this.overtime = overtime;
    }

    @ExcelField(title = "是否补贴", align = 2, sort = 16)
    public Boolean getSubsidy() {
        return subsidy;
    }

    public void setSubsidy(Boolean subsidy) {
        this.subsidy = subsidy;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
}

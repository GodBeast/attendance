package com.xkp.attendance.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum HolidayEnum {

    WORKDAY(0, "工作日"),
    WEEKEND(1, "周末"),
    HOLIDAY(2, "节假日"),
    AFTERCLASS(9, "补班"),
    UNKNOWN(-1, "未知"),
    ;

    private Integer value;
    private String description;

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }
}

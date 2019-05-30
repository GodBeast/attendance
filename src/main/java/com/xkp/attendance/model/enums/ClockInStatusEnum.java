package com.xkp.attendance.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum ClockInStatusEnum {

    NORMAL(0, "正常"),
    LACK(1, "缺卡"),
    LATE(2, "迟到"),
    EARLY(3, "早退"),
    REST(4, "休息日"),
    ;

    private Integer value;
    private String description;

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }
}

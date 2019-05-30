package com.xkp.attendance.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum EmpTypeEnum {

    UNKNOWN(0, "未知"),
    ADMIN(1, "管理员"),
    WHITECOLLAR(2, "白领"),
    WORKER(3, "工人"),
    ;

    private Integer value;
    private String description;

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }
}

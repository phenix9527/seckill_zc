package com.example.seckillzc.enums;

import lombok.Getter;

@Getter
public enum SecKillStatEnum {
    SUCCESS(2, "秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT(-1, "重复秒杀"),
    INNER_ERROR(-2, "系统异常"),
    DATA_REWRITE(-3, "数据篡改"),
    ;

    SecKillStatEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public static SecKillStatEnum stateOf(int index) {
        for (SecKillStatEnum state : values()) {
            if (state.getState() == index) {
                return state;
            }
        }
        return null;
    }

    private int state;
    private String stateInfo;
}

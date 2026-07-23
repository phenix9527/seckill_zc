package com.example.seckillzc.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 暴露秒杀地址DTO
 */
@Data
@Builder
public class Exposer {
    // 是否开启秒杀
    private boolean exposed;
    private String md5;
    private long secKillId;
    // 系统当前时间(毫秒)
    private long now;
    private long start;
    private long end;
}

package com.example.seckillzc.dto;

import com.example.seckillzc.entity.SuccessKilled;
import com.example.seckillzc.enums.SecKillStatEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecKillExecution {
    private long secKillId;
    private int state;
    private String stateInfo;
//    private SecKillStatEnum statEnum;
    private SuccessKilled successKilled;
}

package com.example.seckillzc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill")
public class Seckill {

    @TableId(value = "seckill_id", type = IdType.AUTO)
    private Long seckillId;

    private String name;

    private Integer stock;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createdAt;
}

package com.example.seckillzc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("success_killed")
public class SuccessKilled {

    @TableId(value = "seckill_id", type = IdType.INPUT)
    private Long seckillId;

    private Long userPhone;

    private Integer state;

    private LocalDateTime createdAt;
}

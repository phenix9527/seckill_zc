package com.example.seckillzc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckillzc.entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;

public interface SuccessKilledMapper extends BaseMapper<SuccessKilled> {

    /**
     * 插入秒杀明细（带 INSERT IGNORE，防同一用户对同一商品重复秒杀）。
     * 联合主键 (seckill_id, user_phone) 冲突时静默跳过，返回影响行数：
     * 1 = 插入成功；0 = 该用户已秒杀过（冲突被忽略，不抛异常）。
     * state / created_at 由数据库默认值填充（-1 / 当前时间）。
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);
}

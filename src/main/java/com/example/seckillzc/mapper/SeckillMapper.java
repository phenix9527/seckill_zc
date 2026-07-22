package com.example.seckillzc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.seckillzc.entity.Seckill;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface SeckillMapper extends BaseMapper<Seckill> {

    /**
     * 减库存（带时间窗口校验）。
     * 仅当 killTime 落在 [start_time, end_time] 且库存 > 0 时才扣减，返回影响行数：
     * 返回 1 表示扣减成功，返回 0 表示库存不足或不在秒杀时间窗口内。
     * SQL 定义见 resources/mapper/SeckillMapper.xml。
     *
     * @param seckillId 秒杀商品 id
     * @param killTime  当前秒杀时间，用于校验时间窗口
     * @return 影响行数（1 成功 / 0 失败）
     */
    int reduceStock(@Param("seckillId") long seckillId, @Param("killTime") LocalDateTime killTime);

    /**
     * 按名称关键字分页查询（name 为空时返回全部）。
     * 第一个参数为 IPage，分页由 MyBatis-Plus 分页插件自动完成，无需手写 limit。
     * SQL 定义见 resources/mapper/SeckillMapper.xml。
     */
    IPage<Seckill> selectPageByCondition(IPage<Seckill> page, @Param("name") String name);

}

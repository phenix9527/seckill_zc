package com.example.seckillzc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.seckillzc.entity.SuccessKilled;

public interface SuccessKilledService extends IService<SuccessKilled> {

    SuccessKilled getByKey(Long seckillId, Long userPhone);

    boolean updateByKey(SuccessKilled successKilled);

    boolean removeByKey(Long seckillId, Long userPhone);

    /**
     * 执行秒杀下单：先插明细（INSERT IGNORE 防同一用户重复秒杀），再扣库存。
     *
     * @param seckillId 秒杀商品 id
     * @param userPhone 用户手机号
     * @return true=秒杀成功；false=该用户已秒杀过（明细主键冲突被忽略，不扣库存）
     * @throws IllegalStateException 库存不足或不在活动时间内（事务回滚，明细一并撤销）
     */
    boolean executeSeckill(long seckillId, long userPhone);
}

package com.example.seckillzc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckillzc.entity.SuccessKilled;
import com.example.seckillzc.mapper.SeckillMapper;
import com.example.seckillzc.mapper.SuccessKilledMapper;
import com.example.seckillzc.service.SuccessKilledService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SuccessKilledServiceImpl extends ServiceImpl<SuccessKilledMapper, SuccessKilled> implements SuccessKilledService {

    private final SeckillMapper seckillMapper;

    public SuccessKilledServiceImpl(SeckillMapper seckillMapper) {
        this.seckillMapper = seckillMapper;
    }

    @Override
    public SuccessKilled getByKey(Long seckillId, Long userPhone) {
        return getOne(keyQuery(seckillId, userPhone));
    }

    @Override
    public boolean updateByKey(SuccessKilled successKilled) {
        LambdaUpdateWrapper<SuccessKilled> wrapper = new LambdaUpdateWrapper<SuccessKilled>()
                .eq(SuccessKilled::getSeckillId, successKilled.getSeckillId())
                .eq(SuccessKilled::getUserPhone, successKilled.getUserPhone())
                .set(successKilled.getState() != null, SuccessKilled::getState, successKilled.getState())
                .set(successKilled.getCreatedAt() != null, SuccessKilled::getCreatedAt, successKilled.getCreatedAt());
        return update(wrapper);
    }

    @Override
    public boolean removeByKey(Long seckillId, Long userPhone) {
        return remove(keyQuery(seckillId, userPhone));
    }

    @Override
    @Transactional
    public boolean executeSeckill(long seckillId, long userPhone) {
        // 1. 先插明细：INSERT IGNORE 保证同一用户对同一商品只成功一次
        int insertRows = baseMapper.insertSuccessKilled(seckillId, userPhone);
        if (insertRows != 1) {
            // 主键冲突被忽略 → 该用户已秒杀过，直接返回失败，不再扣库存
            return false;
        }
        // 2. 再扣库存（reduceStock 内置 stock>0 与时间窗口校验，防超卖/防窗口外）
        int reduceRows = seckillMapper.reduceStock(seckillId, LocalDateTime.now());
        if (reduceRows == 1) {
            return true;
        }
        // 库存不足 / 窗口外：抛异常触发事务回滚，撤销上面插入的明细，保证两者一致
        throw new IllegalStateException("秒杀失败：库存不足或不在活动时间窗口内");
    }

    private LambdaQueryWrapper<SuccessKilled> keyQuery(Long seckillId, Long userPhone) {
        return new LambdaQueryWrapper<SuccessKilled>()
                .eq(SuccessKilled::getSeckillId, seckillId)
                .eq(SuccessKilled::getUserPhone, userPhone);
    }
}

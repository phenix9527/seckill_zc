package com.example.seckillzc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckillzc.entity.SuccessKilled;
import com.example.seckillzc.mapper.SuccessKilledMapper;
import com.example.seckillzc.service.SuccessKilledService;
import org.springframework.stereotype.Service;

@Service
public class SuccessKilledServiceImpl extends ServiceImpl<SuccessKilledMapper, SuccessKilled> implements SuccessKilledService {

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

    private LambdaQueryWrapper<SuccessKilled> keyQuery(Long seckillId, Long userPhone) {
        return new LambdaQueryWrapper<SuccessKilled>()
                .eq(SuccessKilled::getSeckillId, seckillId)
                .eq(SuccessKilled::getUserPhone, userPhone);
    }
}

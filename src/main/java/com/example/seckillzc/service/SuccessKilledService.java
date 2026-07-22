package com.example.seckillzc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.seckillzc.entity.SuccessKilled;

public interface SuccessKilledService extends IService<SuccessKilled> {

    SuccessKilled getByKey(Long seckillId, Long userPhone);

    boolean updateByKey(SuccessKilled successKilled);

    boolean removeByKey(Long seckillId, Long userPhone);
}

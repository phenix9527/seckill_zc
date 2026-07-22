package com.example.seckillzc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckillzc.entity.Seckill;
import com.example.seckillzc.mapper.SeckillMapper;
import com.example.seckillzc.service.SeckillService;
import org.springframework.stereotype.Service;

@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillMapper, Seckill> implements SeckillService {
}

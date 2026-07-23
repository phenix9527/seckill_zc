package com.example.seckillzc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.seckillzc.dto.Exposer;
import com.example.seckillzc.dto.SecKillExecution;
import com.example.seckillzc.entity.Seckill;
import com.example.seckillzc.exception.RepeatKillException;
import com.example.seckillzc.exception.SecKillCloseException;
import com.example.seckillzc.exception.SecKillException;

import java.util.List;

/**
 * 站在 使用者 角度设计接口
 * 三个方法：方法定义粒度，参数，返回类型(return 类型/异常)
 */
public interface SeckillService extends IService<Seckill> {

    /**
     * 查询所有秒杀列表
     *
     * @return
     */
    List<Seckill> getSecKillList();

    /**
     * 查询一个秒杀信息
     *
     * @param secKillId
     * @return
     */
    Seckill getSecKillById(long secKillId);

    /**
     * 秒杀开启时输出秒杀接口地址,
     * 否则输出系统时间和秒杀时间
     *
     * @param secKillId
     */
    Exposer exportSecKillUrl(long secKillId);

    /**
     * 执行秒杀操作
     *
     * @param secKillId
     * @param userPhone
     * @param md5
     */
    SecKillExecution execute(long secKillId, long userPhone, String md5)
            throws SecKillException, RepeatKillException, SecKillCloseException;
}

package com.example.seckillzc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckillzc.dto.Exposer;
import com.example.seckillzc.dto.SecKillExecution;
import com.example.seckillzc.entity.Seckill;
import com.example.seckillzc.entity.SuccessKilled;
import com.example.seckillzc.enums.SecKillStatEnum;
import com.example.seckillzc.exception.RepeatKillException;
import com.example.seckillzc.exception.SecKillCloseException;
import com.example.seckillzc.exception.SecKillException;
import com.example.seckillzc.mapper.SeckillMapper;
import com.example.seckillzc.mapper.SuccessKilledMapper;
import com.example.seckillzc.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillMapper, Seckill> implements SeckillService {

    private static final String SALT = "dhfkdsjhfk~asdhf23094fdnskfjasdkjf^^^%$#324(*KS";

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private SuccessKilledMapper successKilledMapper;

    @Override
    public List<Seckill> getSecKillList() {
        LambdaQueryWrapper<Seckill> wrapper = new LambdaQueryWrapper();
        return seckillMapper.selectList(wrapper);
    }

    @Override
    public Seckill getSecKillById(long secKillId) {
        return seckillMapper.selectById(secKillId);
    }

    @Override
    public Exposer exportSecKillUrl(long secKillId) {
        Seckill secKill = this.getSecKillById(secKillId);
        if (Objects.isNull(secKill)) {
            return Exposer.builder().exposed(false).secKillId(secKillId).build();
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = secKill.getStartTime();
        LocalDateTime endTime = secKill.getEndTime();

        if (startTime.isAfter(now) && endTime.isBefore(now)) {
            return Exposer.builder().exposed(false).secKillId(secKillId)
                    .now(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .start(startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .end(endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();

        }
        return Exposer.builder().exposed(true).secKillId(secKillId)
                .now(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .start(startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .end(endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .secKillId(secKillId)
                .md5(getMd5(secKillId))
                .build();
    }

    private static String getMd5(long secKillId) {
        String base = secKillId + "/" + SALT;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }
/**
1.Spring 默认只对 RuntimeException/Error 回滚，不对受检异常回滚
2.事务是为了保证业务语义的原子性、一致性，隔离性，持久性
3.事务方法的执行时间尽可能短，不要穿插其他网络操作RPC、HTTP请求或者剥离到事务方法外部。
4.事务的本质：把一组操作打包成一个"不可分割"的整体，对外只承认两种结果——"全做成了"或"全没做"，绝不允许出现"做了一半"的中间状态
5."减库存 + 记订单"两步，就像自动售货机买饮料：投币和掉饮料必须同时发生。如果钱扣了饮料没掉（=库存减了订单没记），你亏了；
如果饮料掉了没扣钱（=订单记了库存没减），商家亏了。事务就是那个机制——要么"钱扣了+饮料掉了"一起发生，要么"啥也没变"一起撤掉，不存在中间态。
6.底层一句话怎么做到"要么全要么无"
 开始事务 → 数据库记一份 undo 日志（=草稿/后悔药）
 你执行一堆 SQL（在内存改，但先不真正落定）
 你喊 commit → 改动正式落盘（redo），全员可见
 中途报错 / 你喊 rollback → 按 undo 日志把改动全部复原，回到事务开始前
7.所以事务的本质，是你和数据库之间的一份契约："这一串操作，请当我是一个整体来处理，别让我和别人看到半成品。"
 */

 @Override
    @Transactional
    public SecKillExecution execute(long secKillId, long userPhone, String md5) throws SecKillException, RepeatKillException, SecKillCloseException {
        if (Objects.isNull(md5) || !Objects.equals(md5, getMd5(secKillId))) {
            throw new SecKillException("seckill data rewrite");
        }

        try {
            //执行秒杀逻辑： 减库存 + 记录购买行为
            // 减库存
            LocalDateTime now = LocalDateTime.now();
            int updateCount = seckillMapper.reduceStock(secKillId, now);
            if (updateCount <= 0) {
                throw new SecKillCloseException("seckill is closed");
            }
            // 记录用户购买行为
            int insertCount = successKilledMapper.insertSuccessKilled(secKillId, userPhone, 1);
            // 唯一： seckillId，userPhone
            if (insertCount <= 0) {
                // 重复秒杀
                throw new RepeatKillException("seckill repeated");
            }

            // 直接构造返回对象，避免事务内多余查询、缩短 seckill 行锁持有时间
            SuccessKilled successKilled = new SuccessKilled();
            successKilled.setSeckillId(secKillId);
            successKilled.setUserPhone(userPhone);
            successKilled.setState(1);

            return SecKillExecution.builder()
                    .secKillId(secKillId)
                    .state(SecKillStatEnum.SUCCESS.getState())
                    .stateInfo(SecKillStatEnum.SUCCESS.getStateInfo())
                    .successKilled(successKilled).build();
        } catch (SecKillCloseException | RepeatKillException e1) {
            throw e1;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // 所有编译器异常 转化为 运行期异常、给Spring
            throw new SecKillException("seckill inner error:" + e.getMessage());
        }
    }
}

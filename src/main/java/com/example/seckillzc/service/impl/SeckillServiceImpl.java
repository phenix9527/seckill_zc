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

    @Override
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

            LambdaQueryWrapper<SuccessKilled> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SuccessKilled::getSeckillId, secKillId);
            queryWrapper.eq(SuccessKilled::getUserPhone, userPhone);
            SuccessKilled successKilled = successKilledMapper.selectOne(queryWrapper);

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

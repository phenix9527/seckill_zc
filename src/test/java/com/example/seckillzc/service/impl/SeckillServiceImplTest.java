package com.example.seckillzc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.seckillzc.dto.Exposer;
import com.example.seckillzc.dto.SecKillExecution;
import com.example.seckillzc.entity.Seckill;
import com.example.seckillzc.entity.SuccessKilled;
import com.example.seckillzc.enums.SecKillStatEnum;
import com.example.seckillzc.exception.RepeatKillException;
import com.example.seckillzc.exception.SecKillCloseException;
import com.example.seckillzc.exception.SecKillException;
import com.example.seckillzc.service.SeckillService;
import com.example.seckillzc.service.SuccessKilledService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SeckillServiceImpl 自定义业务逻辑单元测试（execute / exportSecKillUrl）。
 *
 * 约定：H2 内存库 + @ActiveProfiles("test") + @Transactional 自动回滚保证隔离。
 * 基础 4 条种子数据窗口为 2026-07-21~07-22（已过期），故每个用例自行插入覆盖当前时间的种子。
 *
 * 事务回滚验证说明：验证“重复秒杀 / 异常时库存不泄漏”必须让 execute 运行在「独立事务」中，
 * 因此该用例标注 @Transactional(propagation = NOT_SUPPORTED)，使测试自身不开启事务，
 * execute 的 @Transactional 才会真正提交/回滚，从而能观测到回滚后的 DB 状态。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SeckillServiceImplTest {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private SuccessKilledService successKilledService;

    /** 插入一条种子秒杀商品；openWindow=true 表示活动窗口覆盖当前时间。save 在测试事务内随方法回滚。 */
    private Seckill seedSeckill(int stock, boolean openWindow) {
        LocalDateTime now = LocalDateTime.now();
        Seckill s = new Seckill();
        s.setName("单元测试种子-" + System.nanoTime());
        s.setStock(stock);
        if (openWindow) {
            s.setStartTime(now.minusDays(1));
            s.setEndTime(now.plusDays(1));
        } else {
            s.setStartTime(now.minusDays(2));
            s.setEndTime(now.minusDays(1));
        }
        seckillService.save(s);
        return s;
    }

    @Test
    void executeSuccessShouldReduceStockAndRecord() {
        Seckill seed = seedSeckill(10, true);
        long id = seed.getSeckillId();
        long phone = 13800000001L;

        // md5 通过 exportSecKillUrl 获取，避免硬编码 SALT
        String md5 = seckillService.exportSecKillUrl(id).getMd5();

        SecKillExecution result = seckillService.execute(id, phone, md5);

        assertEquals(SecKillStatEnum.SUCCESS.getState(), result.getState(), "应返回秒杀成功状态");
        assertNotNull(result.getSuccessKilled());

        // 库存应扣减 1
        Seckill after = seckillService.getById(id);
        assertEquals(9, after.getStock(), "库存应从 10 减为 9");

        // 应写入一条购买明细
        long killed = successKilledService.lambdaQuery()
                .eq(SuccessKilled::getSeckillId, id).count();
        assertEquals(1, killed, "应生成 1 条购买明细");
    }

    @Test
    void executeWithInvalidMd5ShouldThrowSecKillException() {
        Seckill seed = seedSeckill(10, true);
        long id = seed.getSeckillId();

        SecKillException ex = assertThrows(SecKillException.class,
                () -> seckillService.execute(id, 13800000002L, "deadbeef"));

        assertTrue(ex.getMessage().contains("rewrite"), "应提示数据篡改");

        // 不应产生任何购买明细，库存不变
        assertEquals(10, seckillService.getById(id).getStock());
        assertEquals(0, successKilledService.lambdaQuery()
                .eq(SuccessKilled::getSeckillId, id).count());
    }

    @Test
    void executeWhenSoldOutShouldThrowSecKillCloseException() {
        // 库存为 0，reduceStock 的 WHERE stock > 0 不成立，返回 0 → 关闭异常
        Seckill seed = seedSeckill(0, true);
        long id = seed.getSeckillId();
        String md5 = seckillService.exportSecKillUrl(id).getMd5();

        assertThrows(SecKillCloseException.class,
                () -> seckillService.execute(id, 13800000003L, md5));

        // 库存仍为 0，且无购买明细
        assertEquals(0, seckillService.getById(id).getStock());
        assertEquals(0, successKilledService.lambdaQuery()
                .eq(SuccessKilled::getSeckillId, id).count());
    }

    /**
     * 验证事务回滚：同一用户重复秒杀时，第二次的库存扣减必须被回滚，不能泄漏库存。
     * 本方法自身不开事务（NOT_SUPPORTED），让 execute 的 @Transactional 独立提交/回滚。
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void executeRepeatKillShouldNotLeakStock() {
        Seckill seed = seedSeckill(10, true);
        long id = seed.getSeckillId();
        long phone = 13800000004L;
        try {
            String md5 = seckillService.exportSecKillUrl(id).getMd5();

            // 第一次秒杀：成功并提交
            SecKillExecution first = seckillService.execute(id, phone, md5);
            assertEquals(SecKillStatEnum.SUCCESS.getState(), first.getState());

            // 第二次同一用户秒杀：应抛重复秒杀异常
            assertThrows(RepeatKillException.class,
                    () -> seckillService.execute(id, phone, md5));

            // 关键断言：库存只减了一次（10 -> 9），购买明细仅 1 条 —— 证明异常时库存扣减已回滚
            Seckill after = seckillService.getById(id);
            assertEquals(9, after.getStock(), "重复秒杀不应导致库存二次扣减（事务回滚生效）");
            long killed = successKilledService.lambdaQuery()
                    .eq(SuccessKilled::getSeckillId, id).count();
            assertEquals(1, killed, "重复秒杀不应产生多条购买明细");
        } finally {
            successKilledService.remove(
                    new LambdaQueryWrapper<SuccessKilled>().eq(SuccessKilled::getSeckillId, id));
            seckillService.removeById(id);
        }
    }

    @Test
    void exportSecKillUrlShouldExposeWhenWindowOpen() {
        Seckill seed = seedSeckill(10, true);
        long id = seed.getSeckillId();

        Exposer exposer = seckillService.exportSecKillUrl(id);

        assertTrue(exposer.isExposed(), "窗口内应暴露秒杀地址");
        assertNotNull(exposer.getMd5(), "暴露时应返回 md5 签名");
    }

    /**
     * 验证 exportSecKillUrl 修复后的正确行为：原判断条件
     *   startTime.isAfter(now) && endTime.isBefore(now)
     * 对合法时间窗口恒为 false（一个时刻不可能既在 start 前又在 end 后），已改为 ||。
     * 因此窗口已关闭（now 不在 [start, end] 内）时应返回 exposed=false，并附带时间信息。
     */
    @Test
    void exportSecKillUrlShouldNotExposeWhenWindowClosed() {
        Seckill seed = seedSeckill(10, false); // 已关闭的窗口（endTime 在过去）
        long id = seed.getSeckillId();

        Exposer exposer = seckillService.exportSecKillUrl(id);

        assertFalse(exposer.isExposed(), "窗口已关闭时应返回 exposed=false（修复后）");
        // 未暴露分支应返回 now/start/end 供前端倒计时展示
        assertNotNull(exposer.getNow());
        assertNotNull(exposer.getStart());
        assertNotNull(exposer.getEnd());
    }
}

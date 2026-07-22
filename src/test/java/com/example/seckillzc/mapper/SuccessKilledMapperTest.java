package com.example.seckillzc.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckillzc.entity.SuccessKilled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * SuccessKilledMapper.insertSuccessKilled 单元测试。
 * 验证 INSERT IGNORE 在联合主键 (seckill_id, user_phone) 冲突时静默跳过（返回 0），
 * 并且显式传入的 state 字段能正确持久化。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SuccessKilledMapperTest {

    @Autowired
    private SuccessKilledMapper successKilledMapper;

    @Test
    void insertSuccessKilledFirstTime() {
        int rows = successKilledMapper.insertSuccessKilled(1000L, 13800000001L, 0);
        assertSame(1, rows, "首次插入应成功，返回 1");
        SuccessKilled sk = selectByKey(1000L, 13800000001L);
        assertNotNull(sk, "插入后应能查出明细");
        assertEquals(0, sk.getState(), "state 应正确持久化为传入的 0");
    }

    @Test
    void insertSuccessKilledDuplicateIgnored() {
        successKilledMapper.insertSuccessKilled(1001L, 13800000002L, 0);
        // 同一用户对同一商品再次秒杀，即使传入不同 state，冲突被 IGNORE，返回 0
        int second = successKilledMapper.insertSuccessKilled(1001L, 13800000002L, 1);
        assertSame(0, second, "联合主键冲突应被 IGNORE，返回 0（不抛异常）");
        SuccessKilled sk = selectByKey(1001L, 13800000002L);
        assertEquals(0, sk.getState(), "IGNORE 跳过第二次写入，state 仍为首次插入的 0");
    }

    @Test
    void insertSuccessKilledSameSeckillDifferentPhone() {
        int a = successKilledMapper.insertSuccessKilled(1002L, 13800000003L, 0);
        int b = successKilledMapper.insertSuccessKilled(1002L, 13800000004L, 1);
        assertSame(1, a, "同一商品");
        assertSame(1, b, "不同手机号可各自成功插入");
        assertEquals(0, selectByKey(1002L, 13800000003L).getState());
        assertEquals(1, selectByKey(1002L, 13800000004L).getState());
    }

    private SuccessKilled selectByKey(long seckillId, long userPhone) {
        return successKilledMapper.selectOne(new QueryWrapper<SuccessKilled>()
                .eq("seckill_id", seckillId)
                .eq("user_phone", userPhone));
    }
}

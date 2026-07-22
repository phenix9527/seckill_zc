package com.example.seckillzc.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * SuccessKilledMapper.insertSuccessKilled 单元测试。
 * 验证 INSERT IGNORE 在联合主键 (seckill_id, user_phone) 冲突时静默跳过（返回 0）。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SuccessKilledMapperTest {

    @Autowired
    private SuccessKilledMapper successKilledMapper;

    @Test
    void insertSuccessKilledFirstTime() {
        int rows = successKilledMapper.insertSuccessKilled(1000L, 13800000001L);
        assertSame(1, rows, "首次插入应成功，返回 1");
    }

    @Test
    void insertSuccessKilledDuplicateIgnored() {
        successKilledMapper.insertSuccessKilled(1001L, 13800000002L);
        int second = successKilledMapper.insertSuccessKilled(1001L, 13800000002L);
        assertSame(0, second, "联合主键冲突应被 IGNORE，返回 0（不抛异常）");
    }

    @Test
    void insertSuccessKilledSameSeckillDifferentPhone() {
        int a = successKilledMapper.insertSuccessKilled(1002L, 13800000003L);
        int b = successKilledMapper.insertSuccessKilled(1002L, 13800000004L);
        assertSame(1, a, "同一商品");
        assertSame(1, b, "不同手机号可各自成功插入");
    }
}

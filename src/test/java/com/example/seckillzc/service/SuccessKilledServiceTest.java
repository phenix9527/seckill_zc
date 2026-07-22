package com.example.seckillzc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.seckillzc.entity.Seckill;
import com.example.seckillzc.entity.SuccessKilled;
import com.example.seckillzc.mapper.SeckillMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * success_killed（秒杀成功明细表）Service 层单元测试。
 * 重点验证联合主键（seckill_id + user_phone）下的自定义方法：
 * getByKey / updateByKey / removeByKey。
 * 使用 H2 内存数据库 + @Transactional 自动回滚，保证测试隔离。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SuccessKilledServiceTest {

    @Autowired
    private SuccessKilledService successKilledService;

    private SuccessKilled build(Long seckillId, Long userPhone, Integer state) {
        SuccessKilled sk = new SuccessKilled();
        sk.setSeckillId(seckillId);
        sk.setUserPhone(userPhone);
        sk.setState(state);
        return sk;
    }

    @Test
    void saveAndGetByKey() {
        SuccessKilled sk = build(1000L, 13800000001L, -1);
        boolean saved = successKilledService.save(sk);
        assertTrue(saved);

        SuccessKilled fetched = successKilledService.getByKey(1000L, 13800000001L);
        assertNotNull(fetched);
        assertEquals(1000L, fetched.getSeckillId());
        assertEquals(13800000001L, fetched.getUserPhone());
        assertEquals(-1, fetched.getState());
        assertNotNull(fetched.getCreatedAt(), "created_at 应有默认值");
    }

    @Test
    void getByKeyNotFound() {
        SuccessKilled fetched = successKilledService.getByKey(9999L, 13900000000L);
        assertNull(fetched);
    }

    @Test
    void updateByKey() {
        successKilledService.save(build(1001L, 13800000002L, -1));

        SuccessKilled update = build(1001L, 13800000002L, 1);
        boolean updated = successKilledService.updateByKey(update);
        assertTrue(updated);

        SuccessKilled fetched = successKilledService.getByKey(1001L, 13800000002L);
        assertEquals(1, fetched.getState(), "state 应被更新为 1");
    }

    @Test
    void updateByKeyOnlyChangesProvidedFields() {
        successKilledService.save(build(1002L, 13800000003L, 0));

        // 只更新 state，createdAt 为 null 不应覆盖原值
        SuccessKilled update = build(1002L, 13800000003L, 2);
        boolean updated = successKilledService.updateByKey(update);
        assertTrue(updated);

        SuccessKilled fetched = successKilledService.getByKey(1002L, 13800000003L);
        assertEquals(2, fetched.getState());
        assertNotNull(fetched.getCreatedAt(), "createdAt 未提供时不应被置空");
    }

    @Test
    void removeByKey() {
        successKilledService.save(build(1003L, 13800000004L, 0));
        long totalBefore = successKilledService.count();

        boolean removed = successKilledService.removeByKey(1003L, 13800000004L);
        assertTrue(removed);

        assertNull(successKilledService.getByKey(1003L, 13800000004L));
        assertEquals(totalBefore - 1, successKilledService.count());
    }

    @Test
    void removeByKeyNotFound() {
        boolean removed = successKilledService.removeByKey(8888L, 13700000000L);
        assertFalse(removed, "删除不存在的记录应返回 false（影响 0 行）");
        assertEquals(0, successKilledService.count());
    }

    @Test
    void compositePrimaryKeyRejectsDuplicate() {
        successKilledService.save(build(1004L, 13800000005L, -1));
        // 联合主键重复插入应抛出异常
        assertThrows(Exception.class,
                () -> successKilledService.save(build(1004L, 13800000005L, 0)));
    }

    @Test
    void sameSeckillDifferentPhoneAllowed() {
        // 同一秒杀商品、不同手机号可以各自成功（联合主键）
        successKilledService.save(build(1005L, 13800000006L, -1));
        successKilledService.save(build(1005L, 13800000007L, -1));

        assertEquals(2, successKilledService.count());
        assertNotNull(successKilledService.getByKey(1005L, 13800000006L));
        assertNotNull(successKilledService.getByKey(1005L, 13800000007L));
    }

    @Test
    void page() {
        successKilledService.save(build(1006L, 13800000008L, -1));
        successKilledService.save(build(1006L, 13800000009L, 0));
        successKilledService.save(build(1007L, 13800000010L, 1));

        Page<SuccessKilled> page = successKilledService.page(Page.of(1, 2));
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
        assertEquals(2, page.getPages());
    }
}

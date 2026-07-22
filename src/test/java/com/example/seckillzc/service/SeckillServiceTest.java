package com.example.seckillzc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.seckillzc.entity.Seckill;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * seckill（秒杀库存表）Service 层单元测试。
 * 使用 H2 内存数据库 + @Transactional 自动回滚，保证测试隔离。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SeckillServiceTest {

    @Autowired
    private SeckillService seckillService;

    @Test
    void listShouldReturnInitData() {
        List<Seckill> list = seckillService.list();
        assertEquals(4, list.size(), "初始化应有 4 条秒杀商品");

        Seckill first = list.get(0);
        assertEquals("1000元秒杀iphone17", first.getName());
        assertEquals(100, first.getStock());
        assertNotNull(first.getSeckillId());
        assertNotNull(first.getCreatedAt(), "created_at 应有默认值");
    }

    @Test
    void saveAndGetById() {
        Seckill seckill = new Seckill();
        seckill.setName("测试秒杀商品");
        seckill.setStock(50);
        seckill.setStartTime(LocalDateTime.of(2026, 7, 23, 0, 0, 0));
        seckill.setEndTime(LocalDateTime.of(2026, 7, 24, 0, 0, 0));

        boolean saved = seckillService.save(seckill);
        assertTrue(saved);
        assertNotNull(seckill.getSeckillId(), "保存后应生成自增主键");

        Seckill fetched = seckillService.getById(seckill.getSeckillId());
        assertNotNull(fetched);
        assertEquals("测试秒杀商品", fetched.getName());
        assertEquals(50, fetched.getStock());
        assertEquals(LocalDateTime.of(2026, 7, 23, 0, 0, 0), fetched.getStartTime());
    }

    @Test
    void updateById() {
        Seckill origin = seckillService.list().get(0);
        Long id = origin.getSeckillId();

        origin.setStock(999);
        origin.setName("已更新名称");
        boolean updated = seckillService.updateById(origin);
        assertTrue(updated);

        Seckill fetched = seckillService.getById(id);
        assertEquals(999, fetched.getStock());
        assertEquals("已更新名称", fetched.getName());
    }

    @Test
    void removeById() {
        long totalBefore = seckillService.count();
        Long id = seckillService.list().get(0).getSeckillId();

        boolean removed = seckillService.removeById(id);
        assertTrue(removed);

        assertEquals(totalBefore - 1, seckillService.count());
        assertNull(seckillService.getById(id), "删除后查询应返回 null");
    }

    @Test
    void getByIdNotFound() {
        Seckill fetched = seckillService.getById(999999L);
        assertNull(fetched);
    }

    @Test
    void page() {
        Page<Seckill> page1 = seckillService.page(Page.of(1, 2));
        assertEquals(4, page1.getTotal(), "总记录数应为 4");
        assertEquals(2, page1.getRecords().size(), "首页应有 2 条");
        assertEquals(2, page1.getPages(), "共 2 页");

        Page<Seckill> page2 = seckillService.page(Page.of(2, 2));
        assertEquals(2, page2.getRecords().size(), "第二页应有 2 条");
        assertFalse(page1.getRecords().get(0).getSeckillId()
                        .equals(page2.getRecords().get(0).getSeckillId()),
                "两页数据不应相同");
    }

    @Test
    void saveBatch() {
        Seckill s1 = new Seckill();
        s1.setName("批量商品1");
        s1.setStock(10);
        s1.setStartTime(LocalDateTime.of(2026, 7, 23, 0, 0, 0));
        s1.setEndTime(LocalDateTime.of(2026, 7, 24, 0, 0, 0));

        Seckill s2 = new Seckill();
        s2.setName("批量商品2");
        s2.setStock(20);
        s2.setStartTime(LocalDateTime.of(2026, 7, 23, 0, 0, 0));
        s2.setEndTime(LocalDateTime.of(2026, 7, 24, 0, 0, 0));

        boolean saved = seckillService.saveBatch(List.of(s1, s2));
        assertTrue(saved);

        assertEquals(6, seckillService.count(), "批量插入后应有 6 条");
        assertNotNull(s1.getSeckillId());
        assertNotNull(s2.getSeckillId());
    }
}

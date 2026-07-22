package com.example.seckillzc.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.seckillzc.entity.Seckill;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SeckillMapper 分页查询接口单元测试。
 * 验证自定义 Mapper 方法 selectPageByCondition 在 MyBatis-Plus 分页插件下的分页行为。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SeckillMapperTest {

    @Autowired
    private SeckillMapper seckillMapper;

    @Test
    void selectPageByConditionAll() {
        IPage<Seckill> page = seckillMapper.selectPageByCondition(Page.of(1, 2), null);
        assertEquals(4, page.getTotal(), "初始数据共 4 条");
        assertEquals(2, page.getRecords().size(), "首页应返回 2 条");
        assertEquals(2, page.getPages(), "共 2 页");
    }

    @Test
    void selectPageByConditionBySecondPage() {
        IPage<Seckill> page = seckillMapper.selectPageByCondition(Page.of(2, 2), null);
        assertEquals(2, page.getRecords().size(), "第二页应返回 2 条");
    }

    @Test
    void selectPageByConditionWithName() {
        IPage<Seckill> page = seckillMapper.selectPageByCondition(Page.of(1, 10), "iphone");
        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertTrue(page.getRecords().get(0).getName().contains("iphone"));
    }

    @Test
    void selectPageByConditionNoMatch() {
        IPage<Seckill> page = seckillMapper.selectPageByCondition(Page.of(1, 10), "不存在的商品xyz");
        assertEquals(0, page.getTotal());
        assertTrue(page.getRecords().isEmpty());
    }

    @Test
    void reduceStockWithinWindow() {
        // 初始数据窗口 2026-07-21 00:00 ~ 2026-07-22 00:00，取窗口内时间
        LocalDateTime killTime = LocalDateTime.of(2026, 7, 21, 12, 0);
        long before = seckillMapper.selectById(1L).getStock();

        int affected = seckillMapper.reduceStock(1L, killTime);

        assertSame(1, affected, "窗口内且库存充足应扣减 1 行");
        assertEquals((long) (before - 1), (long) seckillMapper.selectById(1L).getStock(), "库存应减 1");
    }

    @Test
    void reduceStockOutsideWindow() {
        // 取窗口外时间（活动已结束）
        LocalDateTime killTime = LocalDateTime.of(2026, 7, 23, 12, 0);
        int affected = seckillMapper.reduceStock(1L, killTime);
        assertSame(0, affected, "窗口外不应扣减，返回 0");
    }
}

package com.example.seckillzc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.seckillzc.entity.Seckill;
import com.example.seckillzc.service.SeckillService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckills")
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @GetMapping
    public Page<Seckill> page(@RequestParam(defaultValue = "1") long current,
                              @RequestParam(defaultValue = "10") long size) {
        return seckillService.page(Page.of(current, size));
    }

    @GetMapping("/{seckillId}")
    public Seckill getById(@PathVariable Long seckillId) {
        return seckillService.getById(seckillId);
    }

    @PostMapping
    public boolean create(@RequestBody Seckill seckill) {
        return seckillService.save(seckill);
    }

    @PutMapping("/{seckillId}")
    public boolean update(@PathVariable Long seckillId, @RequestBody Seckill seckill) {
        seckill.setSeckillId(seckillId);
        return seckillService.updateById(seckill);
    }

    @DeleteMapping("/{seckillId}")
    public boolean delete(@PathVariable Long seckillId) {
        return seckillService.removeById(seckillId);
    }
}

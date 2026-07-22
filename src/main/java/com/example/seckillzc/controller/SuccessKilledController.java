package com.example.seckillzc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.seckillzc.entity.SuccessKilled;
import com.example.seckillzc.service.SuccessKilledService;
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
@RequestMapping("/success-killed")
public class SuccessKilledController {

    private final SuccessKilledService successKilledService;

    public SuccessKilledController(SuccessKilledService successKilledService) {
        this.successKilledService = successKilledService;
    }

    @GetMapping
    public Page<SuccessKilled> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size) {
        return successKilledService.page(Page.of(current, size));
    }

    @GetMapping("/{seckillId}/{userPhone}")
    public SuccessKilled getByKey(@PathVariable Long seckillId, @PathVariable Long userPhone) {
        return successKilledService.getByKey(seckillId, userPhone);
    }

    @PostMapping
    public boolean create(@RequestBody SuccessKilled successKilled) {
        return successKilledService.save(successKilled);
    }

    @PutMapping("/{seckillId}/{userPhone}")
    public boolean update(@PathVariable Long seckillId,
                          @PathVariable Long userPhone,
                          @RequestBody SuccessKilled successKilled) {
        successKilled.setSeckillId(seckillId);
        successKilled.setUserPhone(userPhone);
        return successKilledService.updateByKey(successKilled);
    }

    @DeleteMapping("/{seckillId}/{userPhone}")
    public boolean delete(@PathVariable Long seckillId, @PathVariable Long userPhone) {
        return successKilledService.removeByKey(seckillId, userPhone);
    }
}

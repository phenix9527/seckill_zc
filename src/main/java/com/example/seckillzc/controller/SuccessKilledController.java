package com.example.seckillzc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.seckillzc.dto.Result;
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
    public Result<Page<SuccessKilled>> page(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size) {
        return Result.success(successKilledService.page(Page.of(current, size)));
    }

    @GetMapping("/{seckillId}/{userPhone}")
    public Result<SuccessKilled> getByKey(@PathVariable Long seckillId, @PathVariable Long userPhone) {
        return Result.success(successKilledService.getByKey(seckillId, userPhone));
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody SuccessKilled successKilled) {
        return Result.success(successKilledService.save(successKilled));
    }

    @PutMapping("/{seckillId}/{userPhone}")
    public Result<Boolean> update(@PathVariable Long seckillId,
                                  @PathVariable Long userPhone,
                                  @RequestBody SuccessKilled successKilled) {
        successKilled.setSeckillId(seckillId);
        successKilled.setUserPhone(userPhone);
        return Result.success(successKilledService.updateByKey(successKilled));
    }

    @DeleteMapping("/{seckillId}/{userPhone}")
    public Result<Boolean> delete(@PathVariable Long seckillId, @PathVariable Long userPhone) {
        return Result.success(successKilledService.removeByKey(seckillId, userPhone));
    }
}

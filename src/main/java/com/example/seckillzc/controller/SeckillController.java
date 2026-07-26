package com.example.seckillzc.controller;

import com.example.seckillzc.dto.Exposer;
import com.example.seckillzc.dto.Result;
import com.example.seckillzc.dto.SecKillExecution;
import com.example.seckillzc.entity.Seckill;
import com.example.seckillzc.exception.NotRegisteredException;
import com.example.seckillzc.exception.RepeatKillException;
import com.example.seckillzc.service.SeckillService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 秒杀 API（Web 层）。所有返回值统一包裹为 {@link Result}。
 * <p>
 * URL 设计：
 * <pre>
 *   GET  /seckill/list                              秒杀列表
 *   GET  /seckill/{id}/detail                       秒杀详情
 *   GET  /seckill/time/now                          获取服务器当前时间（毫秒），用于前端倒计时校时
 *   POST /seckill/{id}/exposer                      暴露秒杀地址（返回 Exposer：exposed+md5 或 now/start/end）
 *   POST /seckill/{id}/{md5}/execution              执行秒杀（userPhone 当前由请求参数传入；生产环境应取自登录态/cookie）
 * </pre>
 * 异常统一由 {@link GlobalExceptionHandler} 转成 Result，不会返回默认错误页。
 */
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    /** GET /seckill/list — 秒杀列表 */
    @GetMapping("/list")
    public Result<List<Seckill>> list() {
        return Result.success(seckillService.getSecKillList());
    }

    /** GET /seckill/{id}/detail — 秒杀详情 */
    @GetMapping("/{id}/detail")
    public Result<Seckill> detail(@PathVariable("id") Long id) {
        return Result.success(seckillService.getSecKillById(id));
    }

    /** GET /seckill/time/now — 系统当前时间（毫秒），前端倒计时按 serverNow + 本地偏移计算 */
    @GetMapping("/time/now")
    public Result<Long> now() {
        return Result.success(System.currentTimeMillis());
    }

    /**
     * POST /seckill/{id}/exposer — 暴露秒杀地址。
     * <p>窗口内返回 exposed=true + md5；窗口外返回 exposed=false + now/start/end（用于前端倒计时）。
     */
    @PostMapping("/{id}/exposer")
    public Result<Exposer> exposer(@PathVariable("id") Long id) {
        return Result.success(seckillService.exportSecKillUrl(id));
    }

    /**
     * POST /seckill/{id}/{md5}/execution — 执行秒杀。
     * <p>userPhone 当前作为请求必填参数，便于 curl/Postman 调用；
     * 生产应替换为从登录态（cookie / JWT）中获取，避免前端伪造。
     * 业务失败（重复秒杀/已结束/数据篡改）由 service 抛异常，经 GlobalExceptionHandler 转 Result。
     */
    @PostMapping("/{id}/{md5}/execution")
    public Result<SecKillExecution> execute(@PathVariable("id") Long id,
                                            @PathVariable("md5") String md5,
                                            @CookieValue(value = "killPhone", required = false) Long userPhone) {
        if (Objects.isNull(userPhone)) {
            throw new NotRegisteredException("userPhone 不能为空，未注册");
        }
        return Result.success(seckillService.execute(id, userPhone, md5));
    }
}

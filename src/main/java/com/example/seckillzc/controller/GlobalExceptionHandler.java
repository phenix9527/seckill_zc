package com.example.seckillzc.controller;

import com.example.seckillzc.dto.Result;
import com.example.seckillzc.dto.SecKillExecution;
import com.example.seckillzc.enums.SecKillStatEnum;
import com.example.seckillzc.exception.NotRegisteredException;
import com.example.seckillzc.exception.RepeatKillException;
import com.example.seckillzc.exception.SecKillCloseException;
import com.example.seckillzc.exception.SecKillException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把所有 Controller 抛出的异常统一包装成 {@link Result}，
 * 避免 Spring 默认 Whitelabel HTML 错误页（前端 fetch().json() 会解析失败）。
 * <p>
 * 业务异常（SecKill*）HTTP 仍返回 200，由前端按 Result.code 判断；
 * 参数缺失 / 系统异常返回对应 HTTP 状态码，但 body 仍为 Result。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SecKillCloseException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<SecKillExecution> handleClose(SecKillCloseException e) {
        log.error("[seckill][close] {}", e.getMessage(), e);
        SecKillExecution execution = SecKillExecution.builder()
                .state(SecKillStatEnum.END.getState())
                .stateInfo(SecKillStatEnum.END.getStateInfo())
                .build();
        return Result.error(SecKillStatEnum.END.getState(), e.getMessage(), execution);
    }

    @ExceptionHandler(RepeatKillException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<SecKillExecution> handleRepeat(RepeatKillException e) {
        log.error("[seckill][repeat] {}", e.getMessage(), e);
        SecKillExecution execution = SecKillExecution.builder()
                .state(SecKillStatEnum.REPEAT.getState())
                .stateInfo(SecKillStatEnum.REPEAT.getStateInfo())
                .build();
        return Result.error(SecKillStatEnum.REPEAT.getState(), e.getMessage(), execution);
    }

    @ExceptionHandler(SecKillException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<SecKillExecution> handleSecKill(SecKillException e) {
        log.error("[seckill][inner] {}", e.getMessage(), e);
        String msg = e.getMessage();
        SecKillStatEnum stat = SecKillStatEnum.INNER_ERROR;
        if (msg != null && msg.contains("rewrite")) {
            stat = SecKillStatEnum.DATA_REWRITE;
        }
        SecKillExecution execution = SecKillExecution.builder()
                .state(stat.getState())
                .stateInfo(stat.getStateInfo())
                .build();
        return Result.error(stat.getState(), msg, execution);
    }

    @ExceptionHandler(NotRegisteredException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<SecKillExecution> handleNotRegistered(NotRegisteredException e) {
        log.error("[seckill][unauthorized] {}", e.getMessage(), e);
        SecKillExecution execution = SecKillExecution.builder()
                .state(SecKillStatEnum.UNAUTHORIZED.getState())
                .stateInfo(SecKillStatEnum.UNAUTHORIZED.getStateInfo())
                .build();
        return Result.error(SecKillStatEnum.UNAUTHORIZED.getState(), e.getMessage(), execution);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        log.error("[seckill][missing-param] {}", e.getMessage(), e);
        return Result.error(400, "缺少必需参数: " + e.getParameterName());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleOther(Exception e) {
        log.error("[seckill][system] {}", e.getMessage(), e);
        return Result.error(500, "系统异常: " + e.getMessage());
    }
}

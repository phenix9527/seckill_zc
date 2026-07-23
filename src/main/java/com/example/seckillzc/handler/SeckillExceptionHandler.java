package com.example.seckillzc.handler;

import com.example.seckillzc.dto.SecKillExecution;
import com.example.seckillzc.enums.SecKillStatEnum;
import com.example.seckillzc.exception.RepeatKillException;
import com.example.seckillzc.exception.SecKillCloseException;
import com.example.seckillzc.exception.SecKillException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 秒杀业务异常统一处理。
 * <p>
 * 把 {@code execute} 抛出的运行时异常转成结构化的 {@link SecKillExecution} 返回，
 * 前端无需区分 HTTP 状态码，统一读取 {@code state/stateInfo} 即可：
 * <ul>
 *   <li>{@link SecKillCloseException}  → END(0, 秒杀结束/售罄)</li>
 *   <li>{@link RepeatKillException}    → REPEAT(-1, 重复秒杀)</li>
 *   <li>{@link SecKillException}       → DATA_REWRITE(-3, 数据篡改) / INNER_ERROR(-2, 系统异常)</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class SeckillExceptionHandler {

    /** 秒杀已结束 / 售罄 */
    @ExceptionHandler(SecKillCloseException.class)
    @ResponseStatus(HttpStatus.OK)
    public SecKillExecution handleSecKillClose(SecKillCloseException e) {
        log.warn("秒杀结束: {}", e.getMessage());
        return SecKillExecution.builder()
                .state(SecKillStatEnum.END.getState())
                .stateInfo(SecKillStatEnum.END.getStateInfo())
                .build();
    }

    /** 重复秒杀 */
    @ExceptionHandler(RepeatKillException.class)
    @ResponseStatus(HttpStatus.OK)
    public SecKillExecution handleRepeatKill(RepeatKillException e) {
        log.warn("重复秒杀: {}", e.getMessage());
        return SecKillExecution.builder()
                .state(SecKillStatEnum.REPEAT.getState())
                .stateInfo(SecKillStatEnum.REPEAT.getStateInfo())
                .build();
    }

    /**
     * 数据篡改 / 系统内部异常（基类）。
     * 按异常消息区分：含 "rewrite" 视为客户端数据篡改，否则为系统异常。
     */
    @ExceptionHandler(SecKillException.class)
    @ResponseStatus(HttpStatus.OK)
    public SecKillExecution handleSecKillException(SecKillException e) {
        SecKillStatEnum statEnum = e.getMessage() != null && e.getMessage().contains("rewrite")
                ? SecKillStatEnum.DATA_REWRITE
                : SecKillStatEnum.INNER_ERROR;
        log.error("秒杀异常[{}]: {}", statEnum.getStateInfo(), e.getMessage());
        return SecKillExecution.builder()
                .state(statEnum.getState())
                .stateInfo(statEnum.getStateInfo())
                .build();
    }

    /** 兜底：任何未预期异常也返回结构化结果，避免直接抛 500 堆栈给前端。 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public SecKillExecution handleUnexpected(Exception e) {
        log.error("未预期异常", e);
        return SecKillExecution.builder()
                .state(SecKillStatEnum.INNER_ERROR.getState())
                .stateInfo(SecKillStatEnum.INNER_ERROR.getStateInfo())
                .build();
    }
}

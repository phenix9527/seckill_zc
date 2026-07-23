package com.example.seckillzc.exception;

/**
 * Spring 的事务，只能处理运行期异常，RuntimeException
 * 重复秒杀异常
 */
public class RepeatKillException extends SecKillException {
    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}

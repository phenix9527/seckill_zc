package com.example.seckillzc.exception;

/**
 * 用户未注册 / 登录态缺失。区别于 RepeatKillException（重复秒杀），
 * 让前端收到独立的业务码以便给出准确提示。
 */
public class NotRegisteredException extends SecKillException {

    public NotRegisteredException(String message) {
        super(message);
    }

    public NotRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }
}

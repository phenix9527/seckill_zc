package com.example.seckillzc.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 统一 API 返回结构。
 * <p>
 * code: 0 表示成功；非 0 表示业务/错误码（与 {@code SecKillStatEnum.state} 对齐，或 400/500 等 HTTP 码）。
 * message: 提示信息（成功时一般为 "success"）。
 * data: 业务数据；异常时通常为 null。
 * </p>
 */
@Data
@Builder
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return Result.<T>builder().code(0).message("success").data(data).build();
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder().code(code).message(message).data(null).build();
    }

    /** 业务失败但需返回结构化 data（如失败时的 SecKillExecution 对象）时使用 */
    public static <T> Result<T> error(int code, String message, T data) {
        return Result.<T>builder().code(code).message(message).data(data).build();
    }
}

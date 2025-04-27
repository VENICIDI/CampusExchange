// 文件路径: ...\src\main\java\org\campusmarket\exchange\dto\Result.java
package org.campusmarket.exchange.dto;

import lombok.Data;

// 通用 API 响应结果封装类
@Data
public class Result<T> {

    // 状态码 (例如 200 表示成功, 4xx/5xx 表示失败)
    private Integer code;

    // 响应消息 (成功消息或错误提示)
    private String message;

    // 响应数据 (成功时携带)
    private T data;

    // --- 构造函数 ---
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // --- 静态工厂方法 ---

    // 成功，并返回数据
    public static <T> Result<T> success(T data) {
        // 可以定义一个成功的状态码常量，例如 HttpStatus.OK.value() = 200
        return new Result<>(200, "success", data);
    }

    // 成功，不返回数据
    public static <T> Result<T> success() {
        return success(null);
    }

    // 成功，返回自定义消息和数据
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // 成功，只返回自定义消息
    public static <T> Result<T> success(String message) {
        return success(message, null);
    }


    // 失败，返回指定状态码和消息
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // 失败，返回默认的 500 状态码和消息
    public static <T> Result<T> error(String message) {
        // 可以定义一个失败的状态码常量，例如 HttpStatus.INTERNAL_SERVER_ERROR.value() = 500
        return error(500, message);
    }
}
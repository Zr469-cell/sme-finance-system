package com.finance.erp.common.exception;

import com.finance.erp.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常 (如: 借贷不平)
     * 这是我们主动抛出的错误，直接展示给用户
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 【新增】捕获 JSON 解析失败
     * 常见原因：日期格式不对（如前端发了空字符串，或者格式不是 yyyy-MM-dd）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleJsonError(HttpMessageNotReadableException e) {
        log.error("JSON解析失败", e);
        return Result.error("数据格式错误: 请检查日期或金额格式是否正确");
    }

    /**
     * 【新增】捕获参数缺失
     * 常见原因：前端没传必填的 Query 参数 (如 bookId)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleParamError(MissingServletRequestParameterException e) {
        return Result.error("缺少必要参数: " + e.getParameterName());
    }

    /**
     * 捕获所有系统异常 (兜底)
     * 开发阶段返回 e.getMessage() 方便调试
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        // 将具体的错误堆栈信息返回给前端，方便您截图排查
        return Result.error("系统错误: " + e.getMessage());
    }
}
package com.babytracker.exception;

import com.babytracker.constants.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBiz(BizException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", ex.getCode());
        body.put("message", ex.getMessage());
        if (ex.getData() != null) {
            body.put("data", ex.getData());
        }
        return body;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleUnreadable(HttpMessageNotReadableException ex) {
        return Map.of(
                "success", false,
                "code", ErrorCode.VALIDATION_FAILED,
                "message", "请求内容格式有误，请检查日期等字段");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handle(Exception ex) {
        log.error("未处理的服务器异常", ex);
        return Map.of("success", false, "code", ErrorCode.INTERNAL_ERROR, "message", "服务器内部错误");
    }
}

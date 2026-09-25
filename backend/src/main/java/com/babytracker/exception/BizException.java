package com.babytracker.exception;

public class BizException extends RuntimeException {
    private final String code;
    /** 需要一并返回给前端的数据，例如冲突时已存在的那条记录 */
    private final transient Object data;

    public BizException(String code, String message) {
        this(code, message, null);
    }

    public BizException(String code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }
}

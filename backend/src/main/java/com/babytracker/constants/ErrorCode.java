package com.babytracker.constants;

public final class ErrorCode {
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    /** 里程碑日期早于宝宝出生日 */
    public static final String MILESTONE_DATE_BEFORE_BIRTH = "MILESTONE_DATE_BEFORE_BIRTH";
    /** 里程碑日期晚于今天 */
    public static final String MILESTONE_DATE_AFTER_TODAY = "MILESTONE_DATE_AFTER_TODAY";
    /** 同一天同类型的里程碑已存在 */
    public static final String MILESTONE_DUPLICATE = "MILESTONE_DUPLICATE";

    private ErrorCode() {}
}

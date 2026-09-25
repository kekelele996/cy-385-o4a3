package com.babytracker.constants;

import java.util.Arrays;

/**
 * 成长里程碑类型。code 落库，label 用于页面展示。
 */
public enum MilestoneType {
    TURN_OVER("turn_over", "第一次翻身"),
    TOOTH("tooth", "第一颗牙"),
    CRAWL("crawl", "第一次爬行"),
    STAND("stand", "第一次站立"),
    WALK("walk", "第一步"),
    WORD("word", "第一次叫爸爸妈妈"),
    SOLID_FOOD("solid_food", "第一次吃辅食"),
    OTHER("other", "其他");

    private final String code;
    private final String label;

    MilestoneType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static MilestoneType fromCode(String code) {
        return Arrays.stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}

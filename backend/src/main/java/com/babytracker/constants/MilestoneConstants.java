package com.babytracker.constants;

/**
 * 成长里程碑类型常量，新增类型只需在此维护。
 */
public final class MilestoneConstants {

    public static final String[] MILESTONE_TYPES = {
            "第一次翻身", "第一颗牙", "第一次坐起", "第一次爬行",
            "第一次叫妈妈", "第一次叫爸爸", "第一次站立", "第一次走路",
            "第一次添加辅食", "其他"
    };

    private MilestoneConstants() {}

    public static boolean isValidType(String type) {
        if (type == null) {
            return false;
        }
        for (String t : MILESTONE_TYPES) {
            if (t.equals(type)) {
                return true;
            }
        }
        return false;
    }
}

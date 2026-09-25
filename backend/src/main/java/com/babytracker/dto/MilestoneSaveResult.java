package com.babytracker.dto;

import com.babytracker.entity.MilestoneRecord;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 保存（新增/编辑）里程碑后的结果。
 * saved=false 表示同一天同类型已存在更早提交的记录，本次未保存，existing 为已有记录。
 */
@Data
public class MilestoneSaveResult {
    private boolean saved;
    private String message;
    private MilestoneRecord record;
    private MilestoneRecord existing;

    public static MilestoneSaveResult saved(MilestoneRecord record) {
        MilestoneSaveResult r = new MilestoneSaveResult();
        r.saved = true;
        r.message = "保存成功";
        r.record = record;
        return r;
    }

    public static MilestoneSaveResult duplicate(MilestoneRecord existing) {
        MilestoneSaveResult r = new MilestoneSaveResult();
        r.saved = false;
        LocalDate d = existing.getMilestoneDate();
        r.message = existing.getType() + "在 " + d + " 已有一条更早提交的记录，本次未保存";
        r.existing = existing;
        return r;
    }
}

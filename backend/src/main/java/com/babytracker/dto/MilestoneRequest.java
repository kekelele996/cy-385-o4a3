package com.babytracker.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 新增/编辑里程碑时前端提交的数据。
 */
@Data
public class MilestoneRequest {
    private Long babyId;
    private String type;
    private LocalDate milestoneDate;
    private String description;
    private String photoUrl;
}

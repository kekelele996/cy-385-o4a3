package com.babytracker.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 新增 / 修改里程碑的请求体。
 */
@Data
public class MilestoneRequest {
    private Long babyId;
    private String typeCode;
    private LocalDate milestoneDate;
    private String note;
    private String photoUrl;
}

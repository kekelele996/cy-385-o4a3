package com.babytracker.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 时间线中展示的一条里程碑。
 */
@Data
public class MilestoneVO {
    private Long id;
    private Long babyId;
    private String typeCode;
    private String typeLabel;
    private LocalDate milestoneDate;
    private String note;
    private String photoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

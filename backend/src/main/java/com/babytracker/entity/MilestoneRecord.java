package com.babytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("milestone_record")
public class MilestoneRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long babyId;
    private String type;
    private LocalDate milestoneDate;
    private String description;
    private String photoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

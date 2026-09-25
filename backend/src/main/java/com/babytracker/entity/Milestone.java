package com.babytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成长里程碑记录。同一宝宝同一天同一类型仅保留最早提交的一条（见 milestone 表唯一索引）。
 */
@Data
@TableName("milestone")
public class Milestone {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long babyId;
    private String typeCode;
    private LocalDate milestoneDate;
    private String note;
    /** 已有图片的地址，不做文件上传 */
    private String photoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.babytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("baby")
public class Baby {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private LocalDate birthday;
    private String bloodType;
    private Double initialHeight;
    private Double initialWeight;
}

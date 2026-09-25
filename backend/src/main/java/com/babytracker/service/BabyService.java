package com.babytracker.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.babytracker.constants.ErrorCode;
import com.babytracker.entity.Baby;
import com.babytracker.exception.BizException;
import com.babytracker.mapper.BabyMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BabyService {
    private final BabyMapper mapper;

    public BabyService(BabyMapper mapper) {
        this.mapper = mapper;
    }

    public Baby create(Baby baby) {
        if (baby.getName() == null || baby.getName().isBlank()) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "宝宝姓名不能为空");
        }
        if (baby.getBirthday() == null) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "出生日期不能为空");
        }
        if (baby.getBirthday().isAfter(LocalDate.now())) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "出生日期不能晚于今天");
        }
        mapper.insert(baby);
        return baby;
    }

    public List<Baby> list() {
        return mapper.selectList(new QueryWrapper<Baby>().orderByDesc("birthday"));
    }

    /**
     * 按主键查询，找不到直接抛业务异常（里程碑校验出生日时必须拿到档案）。
     */
    public Baby getRequired(Long id) {
        Baby baby = mapper.selectById(id);
        if (baby == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "宝宝档案不存在");
        }
        return baby;
    }
}

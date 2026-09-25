package com.babytracker.service;

import com.babytracker.constants.ErrorCode;
import com.babytracker.entity.Baby;
import com.babytracker.exception.BizException;
import com.babytracker.mapper.BabyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BabyService {
    private final BabyMapper mapper;
    public BabyService(BabyMapper mapper) { this.mapper = mapper; }

    public Baby create(Baby baby) {
        if (baby.getBirthday() == null) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "出生日期不能为空");
        }
        mapper.insert(baby);
        return baby;
    }

    public List<Baby> list() { return mapper.selectList(null); }

    public Baby getById(Long id) {
        Baby baby = mapper.selectById(id);
        if (baby == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "未找到对应的宝宝档案");
        }
        return baby;
    }
}

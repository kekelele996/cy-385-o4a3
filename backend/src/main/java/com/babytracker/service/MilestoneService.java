package com.babytracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.babytracker.constants.ErrorCode;
import com.babytracker.constants.MilestoneConstants;
import com.babytracker.dto.MilestoneRequest;
import com.babytracker.dto.MilestoneSaveResult;
import com.babytracker.entity.Baby;
import com.babytracker.entity.MilestoneRecord;
import com.babytracker.exception.BizException;
import com.babytracker.mapper.MilestoneMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MilestoneService {

    private static final Logger log = LoggerFactory.getLogger(MilestoneService.class);

    private final MilestoneMapper mapper;
    private final BabyService babyService;

    public MilestoneService(MilestoneMapper mapper, BabyService babyService) {
        this.mapper = mapper;
        this.babyService = babyService;
    }

    public String[] types() {
        return MilestoneConstants.MILESTONE_TYPES;
    }

    /** 时间线：按里程碑日期倒序，同一天保留提交时间最早的一条排在前面。 */
    public List<MilestoneRecord> timeline(Long babyId) {
        babyService.getById(babyId);
        LambdaQueryWrapper<MilestoneRecord> qw = new LambdaQueryWrapper<MilestoneRecord>()
                .eq(MilestoneRecord::getBabyId, babyId)
                .orderByDesc(MilestoneRecord::getMilestoneDate)
                .orderByAsc(MilestoneRecord::getCreatedAt);
        return mapper.selectList(qw);
    }

    /** 最近一次记录：里程碑日期最晚的一条，日期相同时取提交时间最早的。 */
    public MilestoneRecord latest(Long babyId) {
        babyService.getById(babyId);
        LambdaQueryWrapper<MilestoneRecord> qw = new LambdaQueryWrapper<MilestoneRecord>()
                .eq(MilestoneRecord::getBabyId, babyId)
                .orderByDesc(MilestoneRecord::getMilestoneDate)
                .orderByAsc(MilestoneRecord::getCreatedAt)
                .last("LIMIT 1");
        return mapper.selectOne(qw);
    }

    @Transactional
    public MilestoneSaveResult create(MilestoneRequest req) {
        Baby baby = validate(req);

        MilestoneRecord existing = findSameDaySameType(baby.getId(), req.getType(), req.getMilestoneDate(), null);
        if (existing != null) {
            log.info("里程碑重复提交被保留规则拦截：babyId={}, type={}, date={}, existingId={}",
                    baby.getId(), req.getType(), req.getMilestoneDate(), existing.getId());
            return MilestoneSaveResult.duplicate(existing);
        }

        MilestoneRecord record = new MilestoneRecord();
        record.setBabyId(baby.getId());
        record.setType(req.getType());
        record.setMilestoneDate(req.getMilestoneDate());
        record.setDescription(trimToNull(req.getDescription()));
        record.setPhotoUrl(trimToNull(req.getPhotoUrl()));
        try {
            mapper.insert(record);
        } catch (DuplicateKeyException e) {
            // 并发下唯一索引兜底：同一天同类型只保留最早提交的一条
            return MilestoneSaveResult.duplicate(
                    findSameDaySameType(baby.getId(), req.getType(), req.getMilestoneDate(), null));
        }
        return MilestoneSaveResult.saved(record);
    }

    @Transactional
    public MilestoneRecord update(Long id, MilestoneRequest req) {
        MilestoneRecord record = mapper.selectById(id);
        if (record == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "要修改的里程碑记录不存在");
        }
        req.setBabyId(record.getBabyId());
        Baby baby = validate(req);

        // 改日期/类型后，若与已有记录构成“同一天同类型”冲突，保留提交更早的那条
        MilestoneRecord clash = findSameDaySameType(baby.getId(), req.getType(), req.getMilestoneDate(), id);
        if (clash != null) {
            throw new BizException(ErrorCode.MILESTONE_DUPLICATE,
                    clash.getType() + "在 " + clash.getMilestoneDate()
                            + " 已有一条更早提交的记录（提交时间 " + formatTime(clash.getCreatedAt()) + "），不能改成这一天");
        }

        record.setType(req.getType());
        record.setMilestoneDate(req.getMilestoneDate());
        record.setDescription(trimToNull(req.getDescription()));
        record.setPhotoUrl(trimToNull(req.getPhotoUrl()));
        try {
            mapper.updateById(record);
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.MILESTONE_DUPLICATE, "该日期同类型已有更早提交的记录");
        }
        return record;
    }

    /** 校验档案存在、类型合法、日期不早于出生日且不晚于今天，返回所属宝宝档案。 */
    private Baby validate(MilestoneRequest req) {
        if (req == null || req.getBabyId() == null) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "请先选择宝宝");
        }
        Baby baby = babyService.getById(req.getBabyId());
        if (!MilestoneConstants.isValidType(req.getType())) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "里程碑类型不正确，请从列表中选择");
        }
        LocalDate date = req.getMilestoneDate();
        if (date == null) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "实际日期不能为空");
        }
        LocalDate today = LocalDate.now();
        if (date.isBefore(baby.getBirthday())) {
            throw new BizException(ErrorCode.DATE_OUT_OF_RANGE,
                    "实际日期 " + date + " 早于宝宝出生日期 " + baby.getBirthday() + "，未保存");
        }
        if (date.isAfter(today)) {
            throw new BizException(ErrorCode.DATE_OUT_OF_RANGE,
                    "实际日期 " + date + " 晚于今天 " + today + "，未保存");
        }
        return baby;
    }

    private MilestoneRecord findSameDaySameType(Long babyId, String type, LocalDate date, Long excludeId) {
        LambdaQueryWrapper<MilestoneRecord> qw = new LambdaQueryWrapper<MilestoneRecord>()
                .eq(MilestoneRecord::getBabyId, babyId)
                .eq(MilestoneRecord::getType, type)
                .eq(MilestoneRecord::getMilestoneDate, date)
                .orderByAsc(MilestoneRecord::getCreatedAt)
                .last("LIMIT 1");
        if (excludeId != null) {
            qw.ne(MilestoneRecord::getId, excludeId);
        }
        return mapper.selectOne(qw);
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String formatTime(LocalDateTime t) {
        return t == null ? "" : t.toString().replace('T', ' ');
    }
}

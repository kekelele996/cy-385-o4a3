package com.babytracker.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.babytracker.constants.ErrorCode;
import com.babytracker.constants.MilestoneType;
import com.babytracker.dto.MilestoneRequest;
import com.babytracker.entity.Baby;
import com.babytracker.entity.Milestone;
import com.babytracker.exception.BizException;
import com.babytracker.mapper.MilestoneMapper;
import com.babytracker.vo.MilestoneTypeVO;
import com.babytracker.vo.MilestoneVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class MilestoneService {
    private static final Logger log = LoggerFactory.getLogger(MilestoneService.class);

    private final MilestoneMapper milestoneMapper;
    private final BabyService babyService;

    public MilestoneService(MilestoneMapper milestoneMapper, BabyService babyService) {
        this.milestoneMapper = milestoneMapper;
        this.babyService = babyService;
    }

    public List<MilestoneTypeVO> types() {
        return Arrays.stream(MilestoneType.values())
                .map(t -> new MilestoneTypeVO(t.getCode(), t.getLabel()))
                .toList();
    }

    /**
     * 时间线：按实际日期从早到晚排列，同一天保留提交顺序（createdAt 最早的在前）。
     */
    public List<MilestoneVO> timeline(Long babyId) {
        QueryWrapper<Milestone> query = new QueryWrapper<>();
        if (babyId != null) {
            query.eq("baby_id", babyId);
        }
        query.orderByAsc("milestone_date").orderByAsc("created_at");
        return milestoneMapper.selectList(query).stream().map(this::toVO).toList();
    }

    /**
     * 首页 / 宝宝档案展示的最近一次里程碑（实际日期最晚；同日取最后提交的那条）。
     */
    public MilestoneVO latest(Long babyId) {
        QueryWrapper<Milestone> query = new QueryWrapper<Milestone>()
                .eq("baby_id", babyId)
                .orderByDesc("milestone_date")
                .orderByDesc("created_at")
                .last("LIMIT 1");
        return Optional.ofNullable(milestoneMapper.selectOne(query)).map(this::toVO).orElse(null);
    }

    @Transactional
    public MilestoneVO create(MilestoneRequest request) {
        validate(request);
        Milestone milestone = new Milestone();
        fill(milestone, request);
        try {
            milestoneMapper.insert(milestone);
        } catch (DuplicateKeyException ex) {
            // 并发提交时由唯一索引兜底：同一天同类型只留最早提交的那条
            log.info("里程碑重复提交被唯一索引拦截 babyId={} type={} date={}",
                    request.getBabyId(), request.getTypeCode(), request.getMilestoneDate());
            throwDuplicate(request.getBabyId(), request.getTypeCode(), request.getMilestoneDate(), null);
        }
        log.info("新增里程碑 id={} babyId={} type={} date={}",
                milestone.getId(), milestone.getBabyId(), milestone.getTypeCode(), milestone.getMilestoneDate());
        return toVO(milestone);
    }

    @Transactional
    public MilestoneVO update(Long id, MilestoneRequest request) {
        validate(request);
        Milestone milestone = milestoneMapper.selectById(id);
        if (milestone == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "里程碑记录不存在");
        }
        fill(milestone, request);
        try {
            milestoneMapper.updateById(milestone);
        } catch (DuplicateKeyException ex) {
            // 改日期/类型后与已有记录撞在同一天同类型，同样只保留最早提交的那条
            log.info("里程碑修改与已有记录冲突 id={} babyId={} type={} date={}",
                    id, request.getBabyId(), request.getTypeCode(), request.getMilestoneDate());
            throwDuplicate(request.getBabyId(), request.getTypeCode(), request.getMilestoneDate(), id);
        }
        log.info("修改里程碑 id={} date={}", id, milestone.getMilestoneDate());
        return toVO(milestoneMapper.selectById(id));
    }

    @Transactional
    public void delete(Long id) {
        Milestone milestone = milestoneMapper.selectById(id);
        if (milestone == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "里程碑记录不存在");
        }
        milestoneMapper.deleteById(id);
        log.info("删除里程碑 id={}", id);
    }

    // ---------------- 内部方法 ----------------

    private void validate(MilestoneRequest request) {
        if (request.getBabyId() == null) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "请先选择宝宝档案");
        }
        Baby baby = babyService.getRequired(request.getBabyId());
        if (request.getTypeCode() == null || MilestoneType.fromCode(request.getTypeCode()) == null) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "里程碑类型不合适，请重新选择");
        }
        if (request.getMilestoneDate() == null) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "实际日期不能为空");
        }
        LocalDate date = request.getMilestoneDate();
        if (date.isBefore(baby.getBirthday())) {
            throw new BizException(ErrorCode.MILESTONE_DATE_BEFORE_BIRTH,
                    "日期 " + date + " 早于宝宝出生日 " + baby.getBirthday() + "，时间不合适，未保存");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new BizException(ErrorCode.MILESTONE_DATE_AFTER_TODAY,
                    "日期 " + date + " 晚于今天，时间不合适，未保存");
        }
        if (request.getPhotoUrl() != null && !request.getPhotoUrl().isBlank() && !isHttpUrl(request.getPhotoUrl())) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "照片地址需为 http(s):// 开头的图片链接");
        }
    }

    private boolean isHttpUrl(String url) {
        try {
            URI uri = URI.create(url.trim());
            return "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private void fill(Milestone milestone, MilestoneRequest request) {
        milestone.setBabyId(request.getBabyId());
        milestone.setTypeCode(request.getTypeCode());
        milestone.setMilestoneDate(request.getMilestoneDate());
        milestone.setNote(trimToNull(request.getNote()));
        milestone.setPhotoUrl(trimToNull(request.getPhotoUrl()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void throwDuplicate(Long babyId, String typeCode, LocalDate date, Long excludeId) {
        QueryWrapper<Milestone> query = new QueryWrapper<Milestone>()
                .eq("baby_id", babyId)
                .eq("type_code", typeCode)
                .eq("milestone_date", date);
        if (excludeId != null) {
            query.ne("id", excludeId);
        }
        query.orderByAsc("created_at").last("LIMIT 1");
        Milestone existing = milestoneMapper.selectOne(query);
        String typeLabel = MilestoneType.fromCode(typeCode).getLabel();
        String message = date + " 的「" + typeLabel + "」已有记录，同一天同类型只保留最早提交的一条";
        throw new BizException(ErrorCode.MILESTONE_DUPLICATE, message, existing == null ? null : toVO(existing));
    }

    private MilestoneVO toVO(Milestone milestone) {
        MilestoneVO vo = new MilestoneVO();
        vo.setId(milestone.getId());
        vo.setBabyId(milestone.getBabyId());
        vo.setTypeCode(milestone.getTypeCode());
        MilestoneType type = MilestoneType.fromCode(milestone.getTypeCode());
        vo.setTypeLabel(type == null ? milestone.getTypeCode() : type.getLabel());
        vo.setMilestoneDate(milestone.getMilestoneDate());
        vo.setNote(milestone.getNote());
        vo.setPhotoUrl(milestone.getPhotoUrl());
        vo.setCreatedAt(milestone.getCreatedAt());
        vo.setUpdatedAt(milestone.getUpdatedAt());
        return vo;
    }
}

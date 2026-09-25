package com.babytracker.service;

import com.babytracker.constants.ErrorCode;
import com.babytracker.dto.MilestoneRequest;
import com.babytracker.entity.Baby;
import com.babytracker.entity.Milestone;
import com.babytracker.exception.BizException;
import com.babytracker.mapper.MilestoneMapper;
import com.babytracker.vo.MilestoneVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 里程碑时间线核心规则验证：
 * 日期越界不保存、同日同类型保留最早提交、改日期后时间线重排、最近一次记录取日期最晚。
 */
@SpringBootTest
class MilestoneServiceTest {

    @Autowired
    private MilestoneService milestoneService;
    @Autowired
    private BabyService babyService;
    @Autowired
    private MilestoneMapper milestoneMapper;

    private Long babyId;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        milestoneMapper.delete(null);
        Baby baby = new Baby();
        baby.setName("测试宝宝");
        baby.setBirthday(today.minusDays(300));
        babyService.create(baby);
        babyId = baby.getId();
    }

    private MilestoneRequest request(String type, LocalDate date) {
        MilestoneRequest req = new MilestoneRequest();
        req.setBabyId(babyId);
        req.setTypeCode(type);
        req.setMilestoneDate(date);
        req.setNote("说明");
        return req;
    }

    @Test
    void dateBeforeBirth_isRejectedAndNotSaved() {
        LocalDate before = today.minusDays(301);
        BizException ex = assertThrows(BizException.class,
                () -> milestoneService.create(request("tooth", before)));
        assertEquals(ErrorCode.MILESTONE_DATE_BEFORE_BIRTH, ex.getCode());
        assertTrue(ex.getMessage().contains("早于宝宝出生日"));
        assertEquals(0, milestoneService.timeline(babyId).size());
    }

    @Test
    void dateAfterToday_isRejectedAndNotSaved() {
        BizException ex = assertThrows(BizException.class,
                () -> milestoneService.create(request("tooth", today.plusDays(1))));
        assertEquals(ErrorCode.MILESTONE_DATE_AFTER_TODAY, ex.getCode());
        assertTrue(ex.getMessage().contains("晚于今天"));
        assertEquals(0, milestoneService.timeline(babyId).size());
    }

    @Test
    void sameDaySameType_keepsEarliestAndReturnsExisting() throws InterruptedException {
        LocalDate day = today.minusDays(10);
        MilestoneVO first = milestoneService.create(request("turn_over", day));
        assertNotNull(first.getId());

        // 保证 created_at 有先后差异
        Thread.sleep(1100);

        BizException ex = assertThrows(BizException.class,
                () -> milestoneService.create(request("turn_over", day)));
        assertEquals(ErrorCode.MILESTONE_DUPLICATE, ex.getCode());
        assertTrue(ex.getMessage().contains("已有记录"));
        MilestoneVO existingVo = (MilestoneVO) ex.getData();
        assertEquals(first.getId(), existingVo.getId());

        // 仍然只有最早提交的一条
        List<MilestoneVO> timeline = milestoneService.timeline(babyId);
        assertEquals(1, timeline.size());
        assertEquals(first.getId(), timeline.get(0).getId());
    }

    @Test
    void sameDayDifferentType_bothSaved() {
        LocalDate day = today.minusDays(10);
        milestoneService.create(request("turn_over", day));
        milestoneService.create(request("tooth", day));
        assertEquals(2, milestoneService.timeline(babyId).size());
    }

    @Test
    void updateDate_timelineReordersAndLatestFollows() {
        MilestoneVO early = milestoneService.create(request("turn_over", today.minusDays(30)));
        MilestoneVO later = milestoneService.create(request("tooth", today.minusDays(10)));

        List<MilestoneVO> timeline = milestoneService.timeline(babyId);
        assertEquals(early.getId(), timeline.get(0).getId());
        assertEquals(later.getId(), timeline.get(1).getId());
        assertEquals(later.getId(), milestoneService.latest(babyId).getId());

        // 把“翻身”改到比“第一颗牙”更晚，时间线应重排，最近一次也跟着变
        MilestoneRequest moved = request("turn_over", today.minusDays(2));
        milestoneService.update(early.getId(), moved);

        timeline = milestoneService.timeline(babyId);
        assertEquals(later.getId(), timeline.get(0).getId());
        assertEquals(early.getId(), timeline.get(1).getId());
        assertEquals(early.getId(), milestoneService.latest(babyId).getId());
    }

    @Test
    void updateOntoExistingDayType_isRejected() {
        LocalDate day = today.minusDays(10);
        MilestoneVO turnOver = milestoneService.create(request("turn_over", day));
        milestoneService.create(request("tooth", day));

        // 把“翻身”改成同一天的“第一颗牙”，撞上已有记录，不允许
        BizException ex = assertThrows(BizException.class,
                () -> milestoneService.update(turnOver.getId(), request("tooth", day)));
        assertEquals(ErrorCode.MILESTONE_DUPLICATE, ex.getCode());
    }

    @Test
    void invalidType_isRejected() {
        BizException ex = assertThrows(BizException.class,
                () -> milestoneService.create(request("not_exist", today.minusDays(1))));
        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getCode());
    }
}

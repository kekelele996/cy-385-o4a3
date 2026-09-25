package com.babytracker.controller;

import com.babytracker.dto.MilestoneRequest;
import com.babytracker.dto.MilestoneSaveResult;
import com.babytracker.entity.MilestoneRecord;
import com.babytracker.service.MilestoneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 成长里程碑时间线。
 */
@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    private final MilestoneService service;

    public MilestoneController(MilestoneService service) {
        this.service = service;
    }

    /** 可选的里程碑类型，由后端常量统一维护。 */
    @GetMapping("/types")
    public Map<String, String[]> types() {
        return Map.of("types", service.types());
    }

    /** 某个宝宝的里程碑时间线，按日期倒序返回。 */
    @GetMapping
    public List<MilestoneRecord> timeline(@RequestParam Long babyId) {
        return service.timeline(babyId);
    }

    /** 最近一次里程碑记录（首页与宝宝档案共用）。 */
    @GetMapping("/latest")
    public MilestoneRecord latest(@RequestParam Long babyId) {
        return service.latest(babyId);
    }

    /**
     * 新增记录。日期早于出生日/晚于今天时返回错误；
     * 同一天同类型已有记录时不保存，返回已有记录供页面提示。
     */
    @PostMapping
    public MilestoneSaveResult create(@RequestBody MilestoneRequest request) {
        return service.create(request);
    }

    /** 修改记录（含改日期），保存后前端重新拉取时间线完成重排。 */
    @PutMapping("/{id}")
    public MilestoneRecord update(@PathVariable Long id, @RequestBody MilestoneRequest request) {
        return service.update(id, request);
    }
}

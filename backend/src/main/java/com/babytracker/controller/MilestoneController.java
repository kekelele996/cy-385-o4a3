package com.babytracker.controller;

import com.babytracker.dto.MilestoneRequest;
import com.babytracker.service.MilestoneService;
import com.babytracker.vo.MilestoneTypeVO;
import com.babytracker.vo.MilestoneVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /** 可选里程碑类型 */
    @GetMapping("/types")
    public List<MilestoneTypeVO> types() {
        return service.types();
    }

    /** 时间线，按实际日期从早到晚；可通过 babyId 只看某个宝宝 */
    @GetMapping
    public List<MilestoneVO> timeline(@RequestParam(name = "babyId", required = false) Long babyId) {
        return service.timeline(babyId);
    }

    /** 首页 / 宝宝档案使用的最近一次记录 */
    @GetMapping("/latest")
    public MilestoneVO latest(@RequestParam(name = "babyId") Long babyId) {
        return service.latest(babyId);
    }

    @PostMapping
    public MilestoneVO create(@RequestBody MilestoneRequest request) {
        return service.create(request);
    }

    /** 修改日期后时间线按 milestoneDate 重新排序 */
    @PutMapping("/{id}")
    public MilestoneVO update(@PathVariable Long id, @RequestBody MilestoneRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

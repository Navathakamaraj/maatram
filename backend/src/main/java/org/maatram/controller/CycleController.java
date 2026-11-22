package org.maatram.controller;

import org.maatram.model.AdmissionCycle;
import org.maatram.service.CycleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/cycles")
public class CycleController {

    private final CycleService cycleService;

    public CycleController(CycleService cycleService) {
        this.cycleService = cycleService;
    }

    @GetMapping
    public ResponseEntity<List<AdmissionCycle>> listCycles() {
        return ResponseEntity.ok(cycleService.listCycles());
    }

    @PostMapping
    public ResponseEntity<AdmissionCycle> createCycle(@RequestBody Map<String, String> data) {
        return ResponseEntity.ok(cycleService.createCycle(data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdmissionCycle> updateCycleStatus(@PathVariable Long id, @RequestBody Map<String, String> data) {
        return ResponseEntity.ok(cycleService.updateCycleStatus(id, data.get("status")));
    }

    @GetMapping("/compare")
    public ResponseEntity<List<Map<String, Object>>> compareCycles() {
        return ResponseEntity.ok(cycleService.compareCycles());
    }
}

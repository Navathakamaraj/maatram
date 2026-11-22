package org.maatram.controller;

import org.maatram.dto.PaginatedResponse;
import org.maatram.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<Map<String, Object>>> listApplications(
            @RequestParam(required = false) Long cycle_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ResponseEntity.ok(applicationService.listApplications(cycle_id, status, district, search, page, pageSize));
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> listApplicationIds(
            @RequestParam(required = false) Long cycle_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(applicationService.listApplicationIds(cycle_id, status, district, search));
    }

    @GetMapping("/rejected")
    public ResponseEntity<Map<String, Object>> listRejected(
            @RequestParam(required = false) Long cycle_id,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ResponseEntity.ok(applicationService.listRejected(cycle_id, search, page, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplication(id));
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<Map<String, Object>> bulkUpdateStatus(@RequestBody Map<String, Object> body) {
        List<?> ids = (List<?>) body.get("ids");
        String toStatus = (String) body.get("to_status");
        String reason = (String) body.get("reason");
        return ResponseEntity.ok(applicationService.bulkUpdateStatus(ids, toStatus, reason));
    }

    @PostMapping("/import-status")
    public ResponseEntity<Map<String, Object>> importStatus(@RequestBody Map<String, Object> body) {
        String toStatus = (String) body.get("to_status");
        String defaultReason = (String) body.get("default_reason");
        List<?> rows = (List<?>) body.get("rows");
        return ResponseEntity.ok(applicationService.importStatus(rows, toStatus, defaultReason));
    }
}

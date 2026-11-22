package org.maatram.controller;

import org.maatram.dto.PaginatedResponse;
import org.maatram.model.DomainEvent;
import org.maatram.service.DomainEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/events")
public class DomainEventController {

    private final DomainEventService domainEventService;

    public DomainEventController(DomainEventService domainEventService) {
        this.domainEventService = domainEventService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<DomainEvent>> listEvents(
            @RequestParam(required = false) String aggregate_type,
            @RequestParam(required = false) String event_type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize
    ) {
        return ResponseEntity.ok(domainEventService.listEvents(aggregate_type, event_type, page, pageSize));
    }

    @GetMapping("/consumers")
    public ResponseEntity<List<Map<String, Object>>> listConsumers() {
        return ResponseEntity.ok(domainEventService.listConsumers());
    }

    @PostMapping("/replay")
    public ResponseEntity<Map<String, Object>> replay(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(domainEventService.replay(body.get("consumer_name"), body.get("mode"), body.get("from_time")));
    }

    @PostMapping
    public ResponseEntity<DomainEvent> appendEvent(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(domainEventService.appendEvent(body.get("event_name"), body.get("occurred_at"), body.get("description")));
    }
}

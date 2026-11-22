package org.maatram.controller;

import org.maatram.model.DomainEvent;
import org.maatram.repository.DataStore;
import org.maatram.service.DomainEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/dev")
public class DevController {

    private final DataStore dataStore;
    private final DomainEventService domainEventService;

    public DevController(DataStore dataStore, DomainEventService domainEventService) {
        this.dataStore = dataStore;
        this.domainEventService = domainEventService;
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> reset() {
        dataStore.reset();
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/late-commit")
    public ResponseEntity<DomainEvent> lateCommit() {
        return ResponseEntity.ok(domainEventService.lateCommit());
    }
}

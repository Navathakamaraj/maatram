package org.maatram.controller;

import org.maatram.model.CollegeEvent;
import org.maatram.service.CollegeEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/college-events")
public class CollegeEventController {

    private final CollegeEventService collegeEventService;

    public CollegeEventController(CollegeEventService collegeEventService) {
        this.collegeEventService = collegeEventService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listEvents() {
        return ResponseEntity.ok(collegeEventService.listEvents());
    }

    @PostMapping
    public ResponseEntity<CollegeEvent> createEvent(@RequestBody Map<String, String> data) {
        return ResponseEntity.ok(collegeEventService.createEvent(data));
    }

    @GetMapping("/{id}/attendance")
    public ResponseEntity<Map<String, Object>> getAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(collegeEventService.getAttendance(id));
    }

    @PostMapping("/{id}/attendance")
    public ResponseEntity<Map<String, Object>> markAttendance(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<?> marks = (List<?>) body.get("marks");
        return ResponseEntity.ok(collegeEventService.markAttendance(id, marks));
    }
}

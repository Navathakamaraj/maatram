package org.maatram.controller;

import org.maatram.model.Alumni;
import org.maatram.model.AlumniRegistration;
import org.maatram.model.CareerHistory;
import org.maatram.service.AlumniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AlumniController {

    private final AlumniService alumniService;

    public AlumniController(AlumniService alumniService) {
        this.alumniService = alumniService;
    }

    @GetMapping("/alumni-registrations")
    public ResponseEntity<List<Map<String, Object>>> listRegistrations(@RequestParam(required = false) String registration_status) {
        return ResponseEntity.ok(alumniService.listRegistrations(registration_status));
    }

    @PostMapping("/alumni-registrations/{id}/review")
    public ResponseEntity<AlumniRegistration> reviewRegistration(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(alumniService.reviewRegistration(id, body.get("decision"), body.get("reason")));
    }

    @GetMapping("/alumni")
    public ResponseEntity<List<Map<String, Object>>> listAlumni(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Integer graduation_year
    ) {
        return ResponseEntity.ok(alumniService.listAlumni(search, company, graduation_year));
    }

    @GetMapping("/alumni/{id}")
    public ResponseEntity<Map<String, Object>> getAlumni(@PathVariable Long id) {
        return ResponseEntity.ok(alumniService.getAlumni(id));
    }

    @PostMapping("/alumni/{id}/careers")
    public ResponseEntity<CareerHistory> addCareerEntry(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data
    ) {
        return ResponseEntity.ok(alumniService.addCareerEntry(id, data));
    }

    @GetMapping("/alumni/candidates")
    public ResponseEntity<List<Map<String, Object>>> listCandidates(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(alumniService.listCandidates(q));
    }

    @PostMapping("/alumni")
    public ResponseEntity<Alumni> addAlumniFromStudent(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(alumniService.addAlumniFromStudent(body));
    }
}

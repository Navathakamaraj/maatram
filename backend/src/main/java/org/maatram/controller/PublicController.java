package org.maatram.controller;

import org.maatram.service.PublicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    private final PublicService publicService;

    public PublicController(PublicService publicService) {
        this.publicService = publicService;
    }

    @PostMapping("/applicant/lookup")
    public ResponseEntity<Map<String, Object>> lookup(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(publicService.applicantLookup(body.get("email")));
    }

    @PostMapping("/applications")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody Map<String, Object> f) {
        return ResponseEntity.ok(publicService.applicantSubmit(f));
    }
}

package org.maatram.controller;

import org.maatram.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listCompanies() {
        return ResponseEntity.ok(companyService.listCompanies());
    }

    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeCompanies(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(companyService.mergeCompanies(body.get("normalized_name")));
    }

    @GetMapping("/normalize")
    public ResponseEntity<Map<String, String>> normalizeCompany(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(Map.of("normalized_name", companyService.normalizePreview(name)));
    }
}

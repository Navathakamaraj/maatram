package org.maatram.controller;

import org.maatram.dto.PaginatedResponse;
import org.maatram.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<Map<String, Object>>> listStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String enrollment_status,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ResponseEntity.ok(studentService.listStudents(search, enrollment_status, department, page, pageSize));
    }

    @PostMapping("/graduate")
    public ResponseEntity<Map<String, Object>> graduateStudents(@RequestBody Map<String, Object> body) {
        List<?> ids = (List<?>) body.get("ids");
        Object year = body.get("graduation_year");
        return ResponseEntity.ok(studentService.graduateStudents(ids, year));
    }
}

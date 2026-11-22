package org.maatram.controller;

import org.maatram.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> searchPersons(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(personService.searchPersons(q));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<Map<String, Object>> getPersonTimeline(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getPersonTimeline(id));
    }
}

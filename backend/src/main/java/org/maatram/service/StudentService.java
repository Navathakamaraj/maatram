package org.maatram.service;

import org.maatram.dto.PaginatedResponse;
import org.maatram.exception.ApiException;
import org.maatram.model.Alumni;
import org.maatram.model.Student;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class StudentService {

    private final DataStore dataStore;

    public StudentService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public PaginatedResponse<Map<String, Object>> listStudents(String search, String enrollmentStatus, String department, int page, int pageSize) {
        String sLower = search != null ? search.trim().toLowerCase() : null;

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Student s : dataStore.students) {
            if (enrollmentStatus != null && !enrollmentStatus.equalsIgnoreCase(s.getEnrollment_status())) continue;
            if (department != null && !department.equalsIgnoreCase(s.getDepartment())) continue;

            String name = dataStore.personName(s.getPerson_id());
            if (sLower != null && !sLower.isEmpty()) {
                boolean match = name.toLowerCase().contains(sLower) || s.getStudent_number().toLowerCase().contains(sLower);
                if (!match) continue;
            }

            long attended = dataStore.eventAttendance.stream()
                    .filter(x -> x.getStudent_id().equals(s.getId()) && "PRESENT".equals(x.getAttendance_status()))
                    .count();

            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("person_id", s.getPerson_id());
            map.put("application_id", s.getApplication_id());
            map.put("student_number", s.getStudent_number());
            map.put("admission_date", s.getAdmission_date());
            map.put("department", s.getDepartment());
            map.put("enrollment_status", s.getEnrollment_status());
            map.put("created_at", s.getCreated_at());
            map.put("name", name);
            map.put("attended", attended);
            rows.add(map);
        }

        rows.sort((a, b) -> Long.compare((Long) b.get("id"), (Long) a.get("id")));
        return PaginatedResponse.of(rows, page, pageSize);
    }

    public Map<String, Object> graduateStudents(List<?> rawIds, Object yearObj) {
        if (rawIds == null || rawIds.isEmpty()) {
            throw new ApiException(422, "Select at least one student");
        }

        Integer year = null;
        try {
            if (yearObj != null) year = Integer.parseInt(yearObj.toString());
        } catch (Exception ignored) {}

        int currentYear = LocalDate.now().getYear();
        if (year == null || year < 2000 || year > currentYear + 1) {
            throw new ApiException(422, "Enter a valid pass-out year");
        }

        int moved = 0;
        int skipped = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        String now = Instant.now().toString();

        for (Object item : rawIds) {
            long id = ((Number) item).longValue();
            Student s = dataStore.students.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
            if (s == null) {
                errors.add(Map.of("id", id, "error", "Student not found"));
                continue;
            }
            if (dataStore.alumni.stream().anyMatch(a -> a.getStudent_id().equals(s.getId()))) {
                skipped++;
                continue;
            }
            if (!"ACTIVE".equals(s.getEnrollment_status())) {
                errors.add(Map.of("id", id, "student_number", s.getStudent_number(), "error", "Student is " + s.getEnrollment_status().toLowerCase()));
                continue;
            }

            s.setEnrollment_status("GRADUATED");
            dataStore.emit("student", s.getId(), "STUDENT_GRADUATED", Map.of("graduation_year", year, "changed_by", 1L));

            long aid = dataStore.alumni.stream().mapToLong(Alumni::getId).max().orElse(0) + 1;
            Alumni al = new Alumni(aid, s.getPerson_id(), s.getId(), null, year, s.getDepartment(), "ACTIVE", now);
            dataStore.alumni.add(al);
            dataStore.emit("alumni", aid, "ALUMNI_ADDED", Map.of("student_id", s.getId(), "graduation_year", year, "via", "pass_out"));

            moved++;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("moved", moved);
        res.put("skipped", skipped);
        res.put("errors", errors);

        return res;
    }
}

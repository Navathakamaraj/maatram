package org.maatram.service;

import org.maatram.exception.ApiException;
import org.maatram.model.CollegeEvent;
import org.maatram.model.EventAttendance;
import org.maatram.model.Student;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class CollegeEventService {

    private final DataStore dataStore;

    public CollegeEventService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Map<String, Object>> listEvents() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CollegeEvent e : dataStore.collegeEvents) {
            List<EventAttendance> rows = dataStore.eventAttendance.stream().filter(a -> a.getEvent_id().equals(e.getId())).toList();
            long present = rows.stream().filter(r -> "PRESENT".equals(r.getAttendance_status())).count();

            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("event_name", e.getEvent_name());
            map.put("description", e.getDescription());
            map.put("event_date", e.getEvent_date());
            map.put("location", e.getLocation());
            map.put("created_at", e.getCreated_at());
            map.put("marked", rows.size());
            map.put("present", present);
            list.add(map);
        }

        list.sort((a, b) -> String.valueOf(b.get("event_date")).compareTo(String.valueOf(a.get("event_date"))));
        return list;
    }

    public CollegeEvent createEvent(Map<String, String> data) {
        String name = data.get("event_name");
        String date = data.get("event_date");
        if (name == null || name.isBlank() || date == null || date.isBlank()) {
            throw new ApiException(422, "Event name and date are required");
        }

        long id = dataStore.collegeEvents.stream().mapToLong(CollegeEvent::getId).max().orElse(0) + 1;
        String now = Instant.now().toString();
        CollegeEvent e = new CollegeEvent(id, name, data.get("description"), date, data.get("location"), now);
        dataStore.collegeEvents.add(e);
        dataStore.emit("college_event", id, "EVENT_CREATED", Map.of("event_name", name));

        return e;
    }

    public Map<String, Object> getAttendance(Long id) {
        CollegeEvent ev = dataStore.collegeEvents.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
        if (ev == null) {
            throw new ApiException(404, "Event not found");
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Student s : dataStore.students) {
            if (!"ACTIVE".equals(s.getEnrollment_status())) continue;
            EventAttendance att = dataStore.eventAttendance.stream()
                    .filter(x -> x.getEvent_id().equals(id) && x.getStudent_id().equals(s.getId()))
                    .findFirst()
                    .orElse(null);

            Map<String, Object> map = new HashMap<>();
            map.put("student_id", s.getId());
            map.put("student_number", s.getStudent_number());
            map.put("name", dataStore.personName(s.getPerson_id()));
            map.put("department", s.getDepartment());
            map.put("attendance_status", att != null ? att.getAttendance_status() : null);
            rows.add(map);
        }

        return Map.of("event", ev, "rows", rows);
    }

    public Map<String, Object> markAttendance(Long id, List<?> marks) {
        if (marks == null) marks = List.of();

        int changed = 0;
        String now = Instant.now().toString();

        for (Object item : marks) {
            Map<?, ?> m = (Map<?, ?>) item;
            long studentId = ((Number) m.get("student_id")).longValue();
            String status = (String) m.get("attendance_status");

            EventAttendance row = dataStore.eventAttendance.stream()
                    .filter(x -> x.getEvent_id().equals(id) && x.getStudent_id().equals(studentId))
                    .findFirst()
                    .orElse(null);

            if (row != null && Objects.equals(row.getAttendance_status(), status)) {
                continue;
            }

            if (row == null) {
                long attId = dataStore.eventAttendance.stream().mapToLong(EventAttendance::getId).max().orElse(0) + 1;
                row = new EventAttendance(attId, id, studentId, status, 1L, now);
                dataStore.eventAttendance.add(row);
            } else {
                row.setAttendance_status(status);
                row.setMarked_by(1L);
                row.setMarked_at(now);
            }
            changed++;
        }

        if (changed > 0) {
            dataStore.emit("college_event", id, "ATTENDANCE_MARKED", Map.of("count", changed));
        }

        return Map.of("changed", changed);
    }
}

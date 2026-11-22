package org.maatram.service;

import org.maatram.exception.ApiException;
import org.maatram.model.*;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersonService {

    private final DataStore dataStore;

    public PersonService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Map<String, Object>> searchPersons(String q) {
        String query = q != null ? q.trim().toLowerCase() : "";

        List<Map<String, Object>> list = new ArrayList<>();
        for (Person p : dataStore.persons) {
            String name = p.getFirst_name() + " " + p.getLast_name();
            if (!query.isEmpty()) {
                boolean match = name.toLowerCase().contains(query) ||
                        p.getEmail().toLowerCase().contains(query) ||
                        p.getPhone().toLowerCase().contains(query);
                if (!match) continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("first_name", p.getFirst_name());
            map.put("last_name", p.getLast_name());
            map.put("email", p.getEmail());
            map.put("phone", p.getPhone());
            map.put("date_of_birth", p.getDate_of_birth());
            map.put("gender", p.getGender());
            map.put("created_at", p.getCreated_at());
            map.put("updated_at", p.getUpdated_at());
            map.put("stage", dataStore.stageOf(p.getId()));
            list.add(map);

            if (list.size() >= 25) break;
        }

        return list;
    }

    public Map<String, Object> getPersonTimeline(Long id) {
        Person p = dataStore.persons.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        if (p == null) {
            throw new ApiException(404, "Person not found");
        }

        Student student = dataStore.students.stream().filter(s -> s.getPerson_id().equals(id)).findFirst().orElse(null);
        Alumni alum = dataStore.alumni.stream().filter(a -> a.getPerson_id().equals(id)).findFirst().orElse(null);

        List<Map<String, Object>> apps = dataStore.applications.stream()
                .filter(a -> a.getPerson_id().equals(id))
                .map(a -> {
                    String cycleName = dataStore.admissionCycles.stream()
                            .filter(c -> c.getId().equals(a.getCycle_id()))
                            .findFirst()
                            .map(AdmissionCycle::getCycle_name)
                            .orElse(null);
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("person_id", a.getPerson_id());
                    map.put("cycle_id", a.getCycle_id());
                    map.put("application_number", a.getApplication_number());
                    map.put("status", a.getStatus());
                    map.put("application_data", a.getApplication_data());
                    map.put("submitted_at", a.getSubmitted_at());
                    map.put("reviewed_by", a.getReviewed_by());
                    map.put("created_at", a.getCreated_at());
                    map.put("updated_at", a.getUpdated_at());
                    map.put("cycle_name", cycleName);
                    return map;
                })
                .toList();

        List<Map<String, Object>> attList = student == null ? List.of() :
                dataStore.eventAttendance.stream()
                        .filter(x -> x.getStudent_id().equals(student.getId()))
                        .map(x -> {
                            String evName = dataStore.collegeEvents.stream()
                                    .filter(e -> e.getId().equals(x.getEvent_id()))
                                    .findFirst()
                                    .map(CollegeEvent::getEvent_name)
                                    .orElse(null);
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", x.getId());
                            map.put("event_id", x.getEvent_id());
                            map.put("student_id", x.getStudent_id());
                            map.put("attendance_status", x.getAttendance_status());
                            map.put("marked_by", x.getMarked_by());
                            map.put("marked_at", x.getMarked_at());
                            map.put("event_name", evName);
                            return map;
                        })
                        .toList();

        List<Map<String, Object>> careers = alum != null ? dataStore.careerRows(alum.getId()) : List.of();
        List<DocumentRecord> docs = dataStore.documents.stream().filter(d -> d.getPerson_id().equals(id)).toList();
        List<DomainEvent> events = dataStore.eventsForPerson(id);

        Map<String, Object> res = new HashMap<>();
        res.put("person", p);
        res.put("stage", dataStore.stageOf(id));
        res.put("applications", apps);
        res.put("student", student);
        res.put("attendance", attList);
        res.put("alumni", alum);
        res.put("careers", careers);
        res.put("documents", docs);
        res.put("events", events);

        return res;
    }
}

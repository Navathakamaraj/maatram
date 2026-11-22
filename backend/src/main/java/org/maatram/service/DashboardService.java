package org.maatram.service;

import org.maatram.model.AdmissionCycle;
import org.maatram.model.Application;
import org.maatram.model.DomainEvent;
import org.maatram.model.Student;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DataStore dataStore;

    public DashboardService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Map<String, Object> getDashboard() {
        AdmissionCycle open = dataStore.admissionCycles.stream()
                .filter(c -> "OPEN".equals(c.getStatus()))
                .findFirst()
                .orElse(null);

        List<Application> openApps = open == null ? List.of() :
                dataStore.applications.stream().filter(a -> open.getId().equals(a.getCycle_id())).toList();

        Map<String, Long> byStatus = new HashMap<>();
        for (Application a : openApps) {
            byStatus.put(a.getStatus(), byStatus.getOrDefault(a.getStatus(), 0L) + 1);
        }

        long activeStudents = dataStore.students.stream().filter(s -> "ACTIVE".equals(s.getEnrollment_status())).count();
        long rejectedOpen = openApps.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();

        List<DomainEvent> recentEvents = new ArrayList<>(dataStore.domainEvents);
        Collections.reverse(recentEvents);
        if (recentEvents.size() > 8) {
            recentEvents = recentEvents.subList(0, 8);
        }

        Map<String, Object> counts = Map.of(
                "persons", dataStore.persons.size(),
                "applicationsOpenCycle", openApps.size(),
                "activeStudents", activeStudents,
                "alumni", dataStore.alumni.size(),
                "rejectedOpenCycle", rejectedOpen,
                "events", dataStore.domainEvents.size()
        );

        Map<String, Object> res = new HashMap<>();
        res.put("openCycle", open);
        res.put("counts", counts);
        res.put("byStatus", byStatus);
        res.put("recentEvents", recentEvents);

        return res;
    }

    public Map<String, Object> getMeta() {
        Set<String> districts = new TreeSet<>();
        for (Application a : dataStore.applications) {
            if (a.getApplication_data() != null && a.getApplication_data().get("district") != null) {
                districts.add(String.valueOf(a.getApplication_data().get("district")));
            }
        }

        Set<String> departments = dataStore.students.stream()
                .map(Student::getDepartment)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        Set<String> aggregateTypes = dataStore.domainEvents.stream()
                .map(DomainEvent::getAggregate_type)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        Set<String> eventTypes = dataStore.domainEvents.stream()
                .map(DomainEvent::getEvent_type)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        return Map.of(
                "districts", districts,
                "departments", departments,
                "aggregate_types", aggregateTypes,
                "event_types", eventTypes
        );
    }
}

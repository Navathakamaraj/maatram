package org.maatram.service;

import org.maatram.exception.ApiException;
import org.maatram.model.AdmissionCycle;
import org.maatram.model.Application;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class CycleService {

    private final DataStore dataStore;

    public CycleService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<AdmissionCycle> listCycles() {
        List<AdmissionCycle> list = new ArrayList<>(dataStore.admissionCycles);
        list.sort(Comparator.comparing(AdmissionCycle::getId).reversed());
        return list;
    }

    public AdmissionCycle createCycle(Map<String, String> data) {
        String name = data.get("cycle_name");
        String aYear = data.get("academic_year");
        String start = data.get("start_date");
        String end = data.get("end_date");
        String status = data.getOrDefault("status", "UPCOMING");

        if (name == null || name.isBlank() || aYear == null || aYear.isBlank() ||
                start == null || start.isBlank() || end == null || end.isBlank()) {
            throw new ApiException(422, "All fields are required");
        }
        if (end.compareTo(start) <= 0) {
            throw new ApiException(422, "End date must be after start date");
        }
        if (dataStore.admissionCycles.stream().anyMatch(c -> aYear.equalsIgnoreCase(c.getAcademic_year()))) {
            throw new ApiException(409, "A cycle for this academic year already exists");
        }

        long id = dataStore.admissionCycles.stream().mapToLong(AdmissionCycle::getId).max().orElse(0) + 1;
        String now = Instant.now().toString();
        AdmissionCycle c = new AdmissionCycle(id, name, aYear, start, end, status, now);
        dataStore.admissionCycles.add(c);
        dataStore.emit("admission_cycle", id, "CYCLE_CREATED", Map.of("cycle_name", name));

        return c;
    }

    public AdmissionCycle updateCycleStatus(Long id, String status) {
        AdmissionCycle c = dataStore.admissionCycles.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        if (c == null) {
            throw new ApiException(404, "Cycle not found");
        }
        if ("OPEN".equals(status) && dataStore.admissionCycles.stream().anyMatch(x -> "OPEN".equals(x.getStatus()) && !x.getId().equals(id))) {
            throw new ApiException(409, "Close the current open cycle first");
        }
        String from = c.getStatus();
        c.setStatus(status);
        dataStore.emit("admission_cycle", id, "CYCLE_STATUS_CHANGED", Map.of("from", from, "to", status));
        return c;
    }

    public List<Map<String, Object>> compareCycles() {
        List<Map<String, Object>> res = new ArrayList<>();
        for (AdmissionCycle c : dataStore.admissionCycles) {
            List<Application> apps = dataStore.applications.stream().filter(a -> a.getCycle_id().equals(c.getId())).toList();
            long total = apps.size();
            long submitted = apps.stream().filter(a -> !"DRAFT".equals(a.getStatus())).count();
            long shortlisted = apps.stream().filter(a -> "SHORTLISTED".equals(a.getStatus())).count();
            long selected = apps.stream().filter(a -> "SELECTED".equals(a.getStatus())).count();
            long rejected = apps.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();
            double rate = submitted > 0 ? Math.round((selected * 1000.0) / submitted) / 10.0 : 0.0;

            Map<String, Object> map = new HashMap<>();
            map.put("cycle_id", c.getId());
            map.put("cycle_name", c.getCycle_name());
            map.put("academic_year", c.getAcademic_year());
            map.put("total", total);
            map.put("submitted", submitted);
            map.put("shortlisted", shortlisted);
            map.put("selected", selected);
            map.put("rejected", rejected);
            map.put("selection_rate", rate);
            res.add(map);
        }
        return res;
    }
}

package org.maatram.service;

import org.maatram.dto.PaginatedResponse;
import org.maatram.exception.ApiException;
import org.maatram.model.*;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class ApplicationService {

    private final DataStore dataStore;

    public static final Map<String, List<String>> TRANSITIONS = Map.of(
            "DRAFT", List.of(),
            "SUBMITTED", List.of("UNDER_REVIEW", "SHORTLISTED", "REJECTED"),
            "UNDER_REVIEW", List.of("SHORTLISTED", "REJECTED"),
            "SHORTLISTED", List.of("SELECTED", "REJECTED"),
            "SELECTED", List.of(),
            "REJECTED", List.of()
    );

    public ApplicationService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public PaginatedResponse<Map<String, Object>> listApplications(Long cycleId, String status, String district, String search, int page, int pageSize) {
        List<Map<String, Object>> rows = filterApplications(cycleId, status, district, search);
        return PaginatedResponse.of(rows, page, pageSize);
    }

    public List<Long> listApplicationIds(Long cycleId, String status, String district, String search) {
        List<Map<String, Object>> rows = filterApplications(cycleId, status, district, search);
        return rows.stream().map(r -> (Long) r.get("id")).toList();
    }

    private List<Map<String, Object>> filterApplications(Long cycleId, String status, String district, String search) {
        String sLower = search != null ? search.trim().toLowerCase() : null;

        List<Map<String, Object>> list = new ArrayList<>();
        for (Application a : dataStore.applications) {
            if (cycleId != null && !cycleId.equals(a.getCycle_id())) continue;
            if (status != null && !status.equalsIgnoreCase(a.getStatus())) continue;

            Object appDist = a.getApplication_data() != null ? a.getApplication_data().get("district") : null;
            if (district != null && !district.equalsIgnoreCase(String.valueOf(appDist))) continue;

            Person p = dataStore.persons.stream().filter(x -> x.getId().equals(a.getPerson_id())).findFirst().orElse(null);
            String name = p != null ? p.getFirst_name() + " " + p.getLast_name() : "-";
            String email = p != null ? p.getEmail() : "";
            String phone = p != null ? p.getPhone() : "";

            if (sLower != null && !sLower.isEmpty()) {
                boolean match = name.toLowerCase().contains(sLower) ||
                        a.getApplication_number().toLowerCase().contains(sLower) ||
                        email.toLowerCase().contains(sLower) ||
                        phone.toLowerCase().contains(sLower);
                if (!match) continue;
            }

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
            map.put("applicant_name", name);
            map.put("email", email);
            map.put("phone", phone);
            list.add(map);
        }

        list.sort((a, b) -> Long.compare((Long) b.get("id"), (Long) a.get("id")));
        return list;
    }

    public Map<String, Object> getApplication(Long id) {
        Application a = dataStore.applications.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        if (a == null) {
            throw new ApiException(404, "Application not found");
        }

        Person p = dataStore.persons.stream().filter(x -> x.getId().equals(a.getPerson_id())).findFirst().orElse(null);
        AdmissionCycle c = dataStore.admissionCycles.stream().filter(x -> x.getId().equals(a.getCycle_id())).findFirst().orElse(null);
        List<DocumentRecord> docs = dataStore.documents.stream().filter(d -> a.getId().equals(d.getApplication_id())).toList();
        Student st = dataStore.students.stream().filter(s -> a.getId().equals(s.getApplication_id())).findFirst().orElse(null);
        String reviewerEmail = a.getReviewed_by() != null ?
                dataStore.users.stream().filter(u -> u.getId().equals(a.getReviewed_by())).findFirst().map(User::getEmail).orElse(null) : null;

        List<DomainEvent> history = dataStore.domainEvents.stream()
                .filter(e -> "application".equals(e.getAggregate_type()) &&
                        String.valueOf(a.getId()).equals(String.valueOf(e.getAggregate_id())))
                .toList();

        Map<String, Object> res = new HashMap<>();
        res.put("id", a.getId());
        res.put("person_id", a.getPerson_id());
        res.put("cycle_id", a.getCycle_id());
        res.put("application_number", a.getApplication_number());
        res.put("status", a.getStatus());
        res.put("application_data", a.getApplication_data());
        res.put("submitted_at", a.getSubmitted_at());
        res.put("reviewed_by", a.getReviewed_by());
        res.put("created_at", a.getCreated_at());
        res.put("updated_at", a.getUpdated_at());
        res.put("person", p);
        res.put("cycle", c);
        res.put("documents", docs);
        res.put("student", st);
        res.put("reviewer", reviewerEmail);
        res.put("history", history);
        res.put("allowed", TRANSITIONS.getOrDefault(a.getStatus(), List.of()));

        return res;
    }

    public Map<String, Object> bulkUpdateStatus(List<?> rawIds, String toStatus, String reason) {
        if (rawIds == null || rawIds.isEmpty()) {
            throw new ApiException(422, "Select at least one application");
        }
        if ("REJECTED".equals(toStatus) && (reason == null || reason.trim().isEmpty())) {
            throw new ApiException(422, "A reason is required when rejecting");
        }

        int updated = 0;
        int skipped = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        String now = Instant.now().toString();

        for (Object item : rawIds) {
            long id = ((Number) item).longValue();
            Application a = dataStore.applications.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
            if (a == null) {
                errors.add(Map.of("id", id, "error", "Not found"));
                continue;
            }
            if (a.getStatus().equals(toStatus)) {
                skipped++;
                continue;
            }
            List<String> allowed = TRANSITIONS.getOrDefault(a.getStatus(), List.of());
            if (!allowed.contains(toStatus)) {
                errors.add(Map.of("id", id, "application_number", a.getApplication_number(), "error", "Cannot move " + a.getStatus() + " to " + toStatus));
                continue;
            }

            String from = a.getStatus();
            a.setStatus(toStatus);
            a.setReviewed_by(1L);
            a.setUpdated_at(now);

            Map<String, Object> evData = new HashMap<>();
            evData.put("from", from);
            evData.put("to", toStatus);
            evData.put("reason", reason != null ? reason.trim() : null);
            evData.put("changed_by", 1L);
            dataStore.emit("application", a.getId(), "APPLICATION_STATUS_CHANGED", evData);

            if ("SELECTED".equals(toStatus) && dataStore.students.stream().noneMatch(s -> s.getPerson_id().equals(a.getPerson_id()))) {
                long sid = dataStore.students.stream().mapToLong(Student::getId).max().orElse(0) + 1;
                int year = LocalDate.now().getYear();
                String dept = a.getApplication_data() != null ? String.valueOf(a.getApplication_data().get("preferred_course")) : "General";
                String studentNum = "MT" + year + String.format("%04d", sid);
                String todayDate = LocalDate.now().toString();
                Student s = new Student(sid, a.getPerson_id(), a.getId(), studentNum, todayDate, dept, "ACTIVE", now);
                dataStore.students.add(s);
                dataStore.emit("student", sid, "STUDENT_ENROLLED", Map.of("person_id", a.getPerson_id(), "application_id", a.getId()));
            }

            updated++;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("updated", updated);
        res.put("skipped", skipped);
        res.put("errors", errors);

        return res;
    }

    public Map<String, Object> listRejected(Long cycleId, String search, int page, int pageSize) {
        String sLower = search != null ? search.trim().toLowerCase() : null;

        List<Map<String, Object>> allRejected = new ArrayList<>();
        Map<String, Long> reasons = new HashMap<>();

        for (Application a : dataStore.applications) {
            if (!"REJECTED".equals(a.getStatus())) continue;
            if (cycleId != null && !cycleId.equals(a.getCycle_id())) continue;

            Person p = dataStore.persons.stream().filter(x -> x.getId().equals(a.getPerson_id())).findFirst().orElse(null);
            String name = p != null ? p.getFirst_name() + " " + p.getLast_name() : "-";
            String email = p != null ? p.getEmail() : "";
            String phone = p != null ? p.getPhone() : "";

            DomainEvent ev = null;
            for (int i = dataStore.domainEvents.size() - 1; i >= 0; i--) {
                DomainEvent e = dataStore.domainEvents.get(i);
                if ("application".equals(e.getAggregate_type()) &&
                        String.valueOf(a.getId()).equals(String.valueOf(e.getAggregate_id())) &&
                        "APPLICATION_STATUS_CHANGED".equals(e.getEvent_type()) &&
                        e.getEvent_data() != null && "REJECTED".equals(e.getEvent_data().get("to"))) {
                    ev = e;
                    break;
                }
            }

            String reason = ev != null && ev.getEvent_data().get("reason") != null ? String.valueOf(ev.getEvent_data().get("reason")) : "-";
            String rejAt = ev != null ? ev.getOccurred_at() : a.getUpdated_at();
            String rejFrom = ev != null && ev.getEvent_data().get("from") != null ? String.valueOf(ev.getEvent_data().get("from")) : "-";
            String cycleName = dataStore.admissionCycles.stream().filter(c -> c.getId().equals(a.getCycle_id())).findFirst().map(AdmissionCycle::getCycle_name).orElse("-");

            if (sLower != null && !sLower.isEmpty()) {
                boolean match = name.toLowerCase().contains(sLower) ||
                        a.getApplication_number().toLowerCase().contains(sLower) ||
                        email.toLowerCase().contains(sLower) ||
                        reason.toLowerCase().contains(sLower);
                if (!match) continue;
            }

            reasons.put(reason, reasons.getOrDefault(reason, 0L) + 1);

            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("person_id", a.getPerson_id());
            map.put("cycle_id", a.getCycle_id());
            map.put("application_number", a.getApplication_number());
            map.put("status", a.getStatus());
            map.put("application_data", a.getApplication_data() != null ? a.getApplication_data() : new HashMap<>());
            map.put("applicant_name", name);
            map.put("email", email);
            map.put("phone", phone);
            map.put("reason", reason);
            map.put("rejected_at", rejAt);
            map.put("rejected_from", rejFrom);
            map.put("cycle_name", cycleName);
            allRejected.add(map);
        }

        allRejected.sort((a, b) -> String.valueOf(b.get("rejected_at")).compareTo(String.valueOf(a.get("rejected_at"))));

        PaginatedResponse<Map<String, Object>> paginated = PaginatedResponse.of(allRejected, page, pageSize);
        Map<String, Object> res = new HashMap<>();
        res.put("items", paginated.items());
        res.put("total", paginated.total());
        res.put("page", paginated.page());
        res.put("pageSize", paginated.pageSize());
        res.put("reasons", reasons);

        return res;
    }

    public Map<String, Object> importStatus(List<?> rows, String toStatus, String defaultReason) {
        if (toStatus == null || !List.of("UNDER_REVIEW", "SHORTLISTED", "SELECTED", "REJECTED").contains(toStatus)) {
            throw new ApiException(422, "Choose a status");
        }
        if (rows == null || rows.isEmpty()) {
            throw new ApiException(422, "The file has no application rows");
        }

        int updated = 0;
        int skipped = 0;
        List<Map<String, Object>> notFound = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        String now = Instant.now().toString();

        for (Object rObj : rows) {
            Map<?, ?> r = (Map<?, ?>) rObj;
            Object appNoObj = r.get("application_number");
            String no = appNoObj != null ? appNoObj.toString().trim().toUpperCase() : "";
            Object line = r.get("line");
            String reason = (String) r.get("reason");

            if (no.isEmpty()) {
                errors.add(Map.of("line", line != null ? line : 0, "application_number", "(blank)", "error", "Missing application number"));
                continue;
            }
            if (seen.contains(no)) {
                skipped++;
                continue;
            }
            seen.add(no);

            Application a = dataStore.applications.stream().filter(x -> x.getApplication_number().equalsIgnoreCase(no)).findFirst().orElse(null);
            if (a == null) {
                notFound.add(Map.of("line", line != null ? line : 0, "application_number", no));
                continue;
            }

            String why = reason != null && !reason.trim().isEmpty() ? reason.trim() : (defaultReason != null ? defaultReason.trim() : "");
            if ("REJECTED".equals(toStatus) && why.isEmpty()) {
                errors.add(Map.of("line", line != null ? line : 0, "application_number", no, "error", "No reason (add a reason column or a default reason)"));
                continue;
            }

            if (toStatus.equals(a.getStatus())) {
                skipped++;
                continue;
            }

            List<String> allowed = TRANSITIONS.getOrDefault(a.getStatus(), List.of());
            if (!allowed.contains(toStatus)) {
                errors.add(Map.of("line", line != null ? line : 0, "application_number", no, "error", "Cannot move " + a.getStatus() + " to " + toStatus));
                continue;
            }

            String from = a.getStatus();
            a.setStatus(toStatus);
            a.setReviewed_by(1L);
            a.setUpdated_at(now);

            Map<String, Object> evData = new HashMap<>();
            evData.put("from", from);
            evData.put("to", toStatus);
            evData.put("reason", !why.isEmpty() ? why : null);
            evData.put("changed_by", 1L);
            evData.put("via", "csv_import");
            dataStore.emit("application", a.getId(), "APPLICATION_STATUS_CHANGED", evData);

            if ("SELECTED".equals(toStatus) && dataStore.students.stream().noneMatch(s -> s.getPerson_id().equals(a.getPerson_id()))) {
                long sid = dataStore.students.stream().mapToLong(Student::getId).max().orElse(0) + 1;
                int year = LocalDate.now().getYear();
                String dept = a.getApplication_data() != null ? String.valueOf(a.getApplication_data().get("preferred_course")) : "General";
                String studentNum = "MT" + year + String.format("%04d", sid);
                String todayDate = LocalDate.now().toString();
                Student s = new Student(sid, a.getPerson_id(), a.getId(), studentNum, todayDate, dept, "ACTIVE", now);
                dataStore.students.add(s);
                dataStore.emit("student", sid, "STUDENT_ENROLLED", Map.of("person_id", a.getPerson_id(), "application_id", a.getId()));
            }

            updated++;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("total", rows.size());
        res.put("updated", updated);
        res.put("skipped", skipped);
        res.put("not_found", notFound);
        res.put("errors", errors);

        return res;
    }
}

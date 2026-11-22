package org.maatram.service;

import org.maatram.exception.ApiException;
import org.maatram.model.*;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class AlumniService {

    private final DataStore dataStore;

    public AlumniService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Map<String, Object>> listRegistrations(String registrationStatus) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AlumniRegistration r : dataStore.alumniRegistrations) {
            if (registrationStatus != null && !registrationStatus.equalsIgnoreCase(r.getRegistration_status())) continue;

            String stNum = dataStore.students.stream()
                    .filter(s -> s.getId().equals(r.getStudent_id()))
                    .findFirst()
                    .map(Student::getStudent_number)
                    .orElse("-");

            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("person_id", r.getPerson_id());
            map.put("student_id", r.getStudent_id());
            map.put("graduation_year", r.getGraduation_year());
            map.put("department", r.getDepartment());
            map.put("current_company", r.getCurrent_company());
            map.put("current_job_title", r.getCurrent_job_title());
            map.put("registration_status", r.getRegistration_status());
            map.put("verified_by", r.getVerified_by());
            map.put("verified_at", r.getVerified_at());
            map.put("registered_at", r.getRegistered_at());
            map.put("name", dataStore.personName(r.getPerson_id()));
            map.put("student_number", stNum);
            list.add(map);
        }

        list.sort((a, b) -> String.valueOf(b.get("registered_at")).compareTo(String.valueOf(a.get("registered_at"))));
        return list;
    }

    public AlumniRegistration reviewRegistration(Long id, String decision, String reason) {
        AlumniRegistration r = dataStore.alumniRegistrations.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        if (r == null) {
            throw new ApiException(404, "Registration not found");
        }
        if (!"PENDING".equals(r.getRegistration_status())) {
            throw new ApiException(409, "Already reviewed");
        }
        if ("REJECTED".equals(decision) && (reason == null || reason.trim().isEmpty())) {
            throw new ApiException(422, "A reason is required when rejecting");
        }

        String now = Instant.now().toString();
        r.setRegistration_status(decision);
        r.setVerified_by(1L);
        r.setVerified_at(now);

        if ("APPROVED".equals(decision)) {
            long aid = dataStore.alumni.stream().mapToLong(Alumni::getId).max().orElse(0) + 1;
            dataStore.alumni.add(new Alumni(aid, r.getPerson_id(), r.getStudent_id(), r.getId(), r.getGraduation_year(), r.getDepartment(), "ACTIVE", now));

            Student s = dataStore.students.stream().filter(x -> x.getId().equals(r.getStudent_id())).findFirst().orElse(null);
            if (s != null) s.setEnrollment_status("GRADUATED");

            dataStore.emit("alumni", aid, "ALUMNI_APPROVED", Map.of("registration_id", r.getId()));

            if (r.getCurrent_company() != null && !r.getCurrent_company().isBlank()) {
                Company comp = dataStore.findOrCreateCompany(r.getCurrent_company());
                long chId = dataStore.careerHistory.stream().mapToLong(CareerHistory::getId).max().orElse(0) + 1;
                dataStore.careerHistory.add(new CareerHistory(
                        chId, aid, comp.getId(), r.getCurrent_job_title(), "FULL_TIME",
                        LocalDate.now().toString(), null, true, "SELF_REPORTED", now
                ));
                Map<String, Object> cMap = new HashMap<>();
                cMap.put("company_id", comp.getId());
                cMap.put("job_title", r.getCurrent_job_title());
                cMap.put("source", "SELF_REPORTED");
                dataStore.emit("alumni", aid, "CAREER_ENTRY_ADDED", cMap);
            }
        } else {
            dataStore.emit("alumni_registration", r.getId(), "ALUMNI_REGISTRATION_REJECTED", Map.of("reason", reason != null ? reason : ""));
        }

        return r;
    }

    public List<Map<String, Object>> listAlumni(String search, String company, Integer graduationYear) {
        String sLower = search != null ? search.trim().toLowerCase() : null;
        String wantCompany = company != null ? CompanyNormalizer.normalize(company) : null;

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Alumni a : dataStore.alumni) {
            String name = dataStore.personName(a.getPerson_id());
            if (sLower != null && !sLower.isEmpty()) {
                boolean match = name.toLowerCase().contains(sLower) || (a.getDepartment() != null && a.getDepartment().toLowerCase().contains(sLower));
                if (!match) continue;
            }
            if (graduationYear != null && !graduationYear.equals(a.getGraduation_year())) continue;

            List<Map<String, Object>> careers = dataStore.careerRows(a.getId());
            Map<String, Object> cur = careers.stream().filter(c -> Boolean.TRUE.equals(c.get("is_current"))).findFirst().orElse(null);

            String curComp = cur != null && cur.get("company_name") != null ? String.valueOf(cur.get("company_name")) : "-";
            String canonComp = cur != null && cur.get("canonical_name") != null ? String.valueOf(cur.get("canonical_name")) : "";
            String curTitle = cur != null && cur.get("job_title") != null ? String.valueOf(cur.get("job_title")) : "-";

            if (wantCompany != null && !wantCompany.isEmpty() && !wantCompany.equalsIgnoreCase(canonComp)) {
                continue;
            }

            String stNum = dataStore.students.stream().filter(s -> s.getId().equals(a.getStudent_id())).findFirst().map(Student::getStudent_number).orElse("-");

            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("person_id", a.getPerson_id());
            map.put("student_id", a.getStudent_id());
            map.put("registration_id", a.getRegistration_id());
            map.put("graduation_year", a.getGraduation_year());
            map.put("department", a.getDepartment());
            map.put("alumni_status", a.getAlumni_status());
            map.put("created_at", a.getCreated_at());
            map.put("name", name);
            map.put("student_number", stNum);
            map.put("current_company", curComp);
            map.put("canonical_company", canonComp);
            map.put("current_title", curTitle);
            rows.add(map);
        }

        rows.sort((a, b) -> Integer.compare((Integer) b.get("graduation_year"), (Integer) a.get("graduation_year")));
        return rows;
    }

    public Map<String, Object> getAlumni(Long id) {
        Alumni a = dataStore.alumni.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        if (a == null) {
            throw new ApiException(404, "Alumnus not found");
        }

        Person p = dataStore.persons.stream().filter(x -> x.getId().equals(a.getPerson_id())).findFirst().orElse(null);
        List<Map<String, Object>> careers = dataStore.careerRows(a.getId());

        Map<String, Object> res = new HashMap<>();
        res.put("id", a.getId());
        res.put("person_id", a.getPerson_id());
        res.put("student_id", a.getStudent_id());
        res.put("registration_id", a.getRegistration_id());
        res.put("graduation_year", a.getGraduation_year());
        res.put("department", a.getDepartment());
        res.put("alumni_status", a.getAlumni_status());
        res.put("created_at", a.getCreated_at());
        res.put("person", p);
        res.put("careers", careers);
        res.put("companies", dataStore.companies);

        return res;
    }

    public CareerHistory addCareerEntry(Long alumniId, Map<String, Object> data) {
        String compName = (String) data.get("company_name");
        String jobTitle = (String) data.get("job_title");
        String startDate = (String) data.get("start_date");

        if (compName == null || compName.isBlank() || jobTitle == null || jobTitle.isBlank() || startDate == null || startDate.isBlank()) {
            throw new ApiException(422, "Company, job title and start date are required");
        }

        Company company = dataStore.findOrCreateCompany(compName);
        boolean isCurrent = Boolean.TRUE.equals(data.get("is_current"));
        if (isCurrent) {
            dataStore.careerHistory.stream()
                    .filter(c -> c.getAlumni_id().equals(alumniId) && Boolean.TRUE.equals(c.getIs_current()))
                    .forEach(c -> {
                        c.setIs_current(false);
                        if (c.getEnd_date() == null) c.setEnd_date(startDate);
                    });
        }

        long chId = dataStore.careerHistory.stream().mapToLong(CareerHistory::getId).max().orElse(0) + 1;
        String now = Instant.now().toString();
        CareerHistory ch = new CareerHistory(
                chId,
                alumniId,
                company.getId(),
                jobTitle.trim(),
                (String) data.getOrDefault("employment_type", "FULL_TIME"),
                startDate,
                (String) data.get("end_date"),
                isCurrent,
                (String) data.getOrDefault("source", "SELF_REPORTED"),
                now
        );
        dataStore.careerHistory.add(ch);

        Map<String, Object> evData = new HashMap<>();
        evData.put("company_id", company.getId());
        evData.put("job_title", ch.getJob_title());
        evData.put("source", ch.getSource());
        dataStore.emit("alumni", alumniId, "CAREER_ENTRY_ADDED", evData);

        return ch;
    }

    public List<Map<String, Object>> listCandidates(String q) {
        Set<Long> taken = dataStore.alumni.stream().map(Alumni::getStudent_id).collect(HashSet::new, Set::add, Set::addAll);
        String qLower = q != null ? q.trim().toLowerCase() : "";

        List<Map<String, Object>> list = new ArrayList<>();
        for (Student s : dataStore.students) {
            if (taken.contains(s.getId())) continue;
            String name = dataStore.personName(s.getPerson_id());
            if (!qLower.isEmpty()) {
                boolean match = name.toLowerCase().contains(qLower) || s.getStudent_number().toLowerCase().contains(qLower);
                if (!match) continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("person_id", s.getPerson_id());
            map.put("student_number", s.getStudent_number());
            map.put("admission_date", s.getAdmission_date());
            map.put("department", s.getDepartment());
            map.put("enrollment_status", s.getEnrollment_status());
            map.put("created_at", s.getCreated_at());
            map.put("name", name);
            list.add(map);

            if (list.size() >= 20) break;
        }

        return list;
    }

    public Alumni addAlumniFromStudent(Map<String, Object> body) {
        Long studentId = ((Number) body.get("student_id")).longValue();
        Student s = dataStore.students.stream().filter(x -> x.getId().equals(studentId)).findFirst().orElse(null);
        if (s == null) {
            throw new ApiException(404, "Pick a student from the list");
        }
        if (dataStore.alumni.stream().anyMatch(a -> a.getStudent_id().equals(s.getId()))) {
            throw new ApiException(409, "This student is already an alumnus");
        }

        int year = Integer.parseInt(body.get("graduation_year").toString());
        int currentYear = LocalDate.now().getYear();
        if (year < 2000 || year > currentYear + 1) {
            throw new ApiException(422, "Enter a valid graduation year");
        }

        long aid = dataStore.alumni.stream().mapToLong(Alumni::getId).max().orElse(0) + 1;
        String now = Instant.now().toString();
        Alumni al = new Alumni(aid, s.getPerson_id(), s.getId(), null, year, s.getDepartment(), "ACTIVE", now);
        dataStore.alumni.add(al);
        s.setEnrollment_status("GRADUATED");

        dataStore.emit("alumni", aid, "ALUMNI_ADDED", Map.of("student_id", s.getId(), "added_by", 1L));

        String compName = (String) body.get("company_name");
        String jobTitle = (String) body.get("job_title");
        if (compName != null && !compName.isBlank() && jobTitle != null && !jobTitle.isBlank()) {
            Company comp = dataStore.findOrCreateCompany(compName);
            long chId = dataStore.careerHistory.stream().mapToLong(CareerHistory::getId).max().orElse(0) + 1;
            String src = (String) body.getOrDefault("source", "SELF_REPORTED");
            CareerHistory ch = new CareerHistory(
                    chId, aid, comp.getId(), jobTitle.trim(), "FULL_TIME",
                    LocalDate.now().toString(), null, true, src, now
            );
            dataStore.careerHistory.add(ch);
            Map<String, Object> evData = new HashMap<>();
            evData.put("company_id", comp.getId());
            evData.put("job_title", jobTitle.trim());
            evData.put("source", src);
            dataStore.emit("alumni", aid, "CAREER_ENTRY_ADDED", evData);
        }

        return al;
    }
}

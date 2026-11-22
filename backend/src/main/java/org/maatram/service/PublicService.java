package org.maatram.service;

import org.maatram.exception.ApiException;
import org.maatram.model.AdmissionCycle;
import org.maatram.model.Application;
import org.maatram.model.Person;
import org.maatram.model.Student;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class PublicService {

    private final DataStore dataStore;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    public PublicService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Map<String, Object> applicantLookup(String email) {
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new ApiException(422, "Enter a valid email address");
        }

        AdmissionCycle openCycle = dataStore.admissionCycles.stream()
                .filter(c -> "OPEN".equals(c.getStatus()))
                .findFirst()
                .orElse(null);

        Person person = dataStore.persons.stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(cleanEmail))
                .findFirst()
                .orElse(null);

        List<Application> userApps = person == null ? List.of() :
                dataStore.applications.stream()
                        .filter(a -> a.getPerson_id().equals(person.getId()) && !"DRAFT".equals(a.getStatus()))
                        .sorted(Comparator.comparing(Application::getId).reversed())
                        .toList();

        List<Map<String, Object>> withHistory = new ArrayList<>();
        for (Application a : userApps) {
            String cycleName = dataStore.admissionCycles.stream()
                    .filter(c -> c.getId().equals(a.getCycle_id()))
                    .findFirst()
                    .map(AdmissionCycle::getCycle_name)
                    .orElse(null);

            List<Map<String, Object>> steps = dataStore.domainEvents.stream()
                    .filter(ev -> "application".equals(ev.getAggregate_type()) &&
                            String.valueOf(a.getId()).equals(String.valueOf(ev.getAggregate_id())) &&
                            List.of("APPLICATION_SUBMITTED", "APPLICATION_STATUS_CHANGED").contains(ev.getEvent_type()))
                    .map(ev -> {
                        Map<String, Object> step = new HashMap<>();
                        step.put("at", ev.getOccurred_at());
                        if ("APPLICATION_SUBMITTED".equals(ev.getEvent_type())) {
                            step.put("status", "SUBMITTED");
                        } else {
                            step.put("status", ev.getEvent_data() != null ? ev.getEvent_data().get("to") : null);
                        }
                        step.put("reason", ev.getEvent_data() != null ? ev.getEvent_data().get("reason") : null);
                        return step;
                    })
                    .toList();

            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("application_number", a.getApplication_number());
            map.put("status", a.getStatus());
            map.put("submitted_at", a.getSubmitted_at());
            map.put("cycle_name", cycleName);
            map.put("cycle_id", a.getCycle_id());
            map.put("steps", steps);
            withHistory.add(map);
        }

        Student student = person == null ? null :
                dataStore.students.stream().filter(s -> s.getPerson_id().equals(person.getId())).findFirst().orElse(null);

        Map<String, Object> studentMap = null;
        if (student != null) {
            studentMap = Map.of(
                    "student_number", student.getStudent_number(),
                    "department", student.getDepartment(),
                    "enrollment_status", student.getEnrollment_status(),
                    "admission_date", student.getAdmission_date()
            );
        }

        boolean canApply = openCycle != null && withHistory.stream().noneMatch(a -> openCycle.getId().equals(a.get("cycle_id")));

        Map<String, Object> res = new HashMap<>();
        res.put("email", cleanEmail);
        res.put("person", person != null ? Map.of("first_name", person.getFirst_name(), "last_name", person.getLast_name()) : null);
        res.put("openCycle", openCycle);
        res.put("student", studentMap);
        res.put("applications", withHistory);
        res.put("canApply", canApply);

        return res;
    }

    public Map<String, Object> applicantSubmit(Map<String, Object> f) {
        String email = String.valueOf(f.getOrDefault("email", "")).trim().toLowerCase();
        AdmissionCycle openCycle = dataStore.admissionCycles.stream()
                .filter(c -> "OPEN".equals(c.getStatus()))
                .findFirst()
                .orElse(null);

        if (openCycle == null) {
            throw new ApiException(409, "Applications are closed right now");
        }

        String today = LocalDate.now().toString();
        if (today.compareTo(openCycle.getEnd_date()) > 0) {
            throw new ApiException(409, "The " + openCycle.getCycle_name() + " cycle closed on " + openCycle.getEnd_date());
        }

        List<String> errs = new ArrayList<>();
        String firstName = (String) f.get("first_name");
        String lastName = (String) f.get("last_name");
        String phone = (String) f.get("phone");
        String dob = (String) f.get("date_of_birth");
        String district = (String) f.get("district");
        String school = (String) f.get("school");
        Object marksObj = f.get("hsc_marks");
        Object incObj = f.get("family_income");
        String preferredCourse = (String) f.get("preferred_course");

        if (firstName == null || firstName.trim().isEmpty()) errs.add("first name");
        if (lastName == null || lastName.trim().isEmpty()) errs.add("last name");
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) errs.add("10-digit phone");
        if (dob == null || dob.trim().isEmpty()) errs.add("date of birth");
        if (district == null || district.trim().isEmpty()) errs.add("district");
        if (school == null || school.trim().isEmpty()) errs.add("school");

        Double marks = null;
        try {
            if (marksObj != null) marks = Double.parseDouble(marksObj.toString());
        } catch (Exception ignored) {}
        if (marks == null || marks < 0 || marks > 100) errs.add("HSC marks between 0 and 100");

        Double income = null;
        try {
            if (incObj != null) income = Double.parseDouble(incObj.toString());
        } catch (Exception ignored) {}
        if (income == null || income < 0) errs.add("family income");

        if (preferredCourse == null || preferredCourse.trim().isEmpty()) errs.add("preferred course");

        if (!errs.isEmpty()) {
            throw new ApiException(422, "Please check: " + String.join(", ", errs));
        }

        Person existing = dataStore.persons.stream().filter(x -> x.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
        if (existing != null) {
            final Long existingPersonId = existing.getId();
            boolean already = dataStore.applications.stream()
                    .anyMatch(a -> a.getPerson_id().equals(existingPersonId) && a.getCycle_id().equals(openCycle.getId()) && !"DRAFT".equals(a.getStatus()));
            if (already) {
                throw new ApiException(409, "You have already applied in this cycle");
            }
        }

        String now = Instant.now().toString();
        Person p = existing;
        if (p == null) {
            long pid = dataStore.persons.stream().mapToLong(Person::getId).max().orElse(0) + 1;
            p = new Person(
                    pid,
                    firstName.trim(),
                    lastName.trim(),
                    email,
                    phone.trim(),
                    dob,
                    (String) f.getOrDefault("gender", "Prefer not to say"),
                    now,
                    now
            );
            dataStore.persons.add(p);
            dataStore.emit("person", pid, "PERSON_CREATED", Map.of("name", firstName.trim() + " " + lastName.trim()));
        }

        long aid = dataStore.applications.stream().mapToLong(Application::getId).max().orElse(0) + 1;
        String appNum = "MH" + openCycle.getAcademic_year().substring(2, 4) + String.format("%05d", aid);

        Map<String, Object> appData = new HashMap<>();
        appData.put("district", district);
        appData.put("school", school.trim());
        appData.put("hsc_marks", marks);
        appData.put("family_income", income);
        appData.put("preferred_course", preferredCourse);

        Application app = new Application(
                aid,
                p.getId(),
                openCycle.getId(),
                appNum,
                "SUBMITTED",
                appData,
                now,
                null,
                now,
                now
        );
        dataStore.applications.add(app);
        dataStore.emit("application", aid, "APPLICATION_SUBMITTED", Map.of("application_number", appNum));

        return Map.of(
                "application_number", appNum,
                "status", "SUBMITTED"
        );
    }
}

package org.maatram.repository;

import jakarta.annotation.PostConstruct;
import org.maatram.model.*;
import org.maatram.service.CompanyNormalizer;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DataStore {

    public final List<Person> persons = new CopyOnWriteArrayList<>();
    public final List<User> users = new CopyOnWriteArrayList<>();
    public final List<AdmissionCycle> admissionCycles = new CopyOnWriteArrayList<>();
    public final List<Application> applications = new CopyOnWriteArrayList<>();
    public final List<DocumentRecord> documents = new CopyOnWriteArrayList<>();
    public final List<Student> students = new CopyOnWriteArrayList<>();
    public final List<CollegeEvent> collegeEvents = new CopyOnWriteArrayList<>();
    public final List<EventAttendance> eventAttendance = new CopyOnWriteArrayList<>();
    public final List<AlumniRegistration> alumniRegistrations = new CopyOnWriteArrayList<>();
    public final List<Alumni> alumni = new CopyOnWriteArrayList<>();
    public final List<CareerHistory> careerHistory = new CopyOnWriteArrayList<>();
    public final List<Company> companies = new CopyOnWriteArrayList<>();
    public final List<DomainEvent> domainEvents = new CopyOnWriteArrayList<>();
    public final List<EventCheckpoint> eventCheckpoints = new CopyOnWriteArrayList<>();
    public final List<ProcessedEvent> processedEvents = new CopyOnWriteArrayList<>();

    private final PersonRepository personRepo;
    private final UserRepository userRepo;
    private final AdmissionCycleRepository cycleRepo;
    private final ApplicationRepository applicationRepo;
    private final DocumentRecordRepository documentRepo;
    private final StudentRepository studentRepo;
    private final CollegeEventRepository eventRepo;
    private final EventAttendanceRepository attendanceRepo;
    private final AlumniRegistrationRepository registrationRepo;
    private final AlumniRepository alumniRepo;
    private final CareerHistoryRepository careerRepo;
    private final CompanyRepository companyRepo;
    private final DomainEventRepository domainEventRepo;
    private final EventCheckpointRepository checkpointRepo;
    private final ProcessedEventRepository processedEventRepo;

    private final AtomicLong nextEventId = new AtomicLong(1);
    private long seed = 20261112L;

    public DataStore(
            PersonRepository personRepo,
            UserRepository userRepo,
            AdmissionCycleRepository cycleRepo,
            ApplicationRepository applicationRepo,
            DocumentRecordRepository documentRepo,
            StudentRepository studentRepo,
            CollegeEventRepository eventRepo,
            EventAttendanceRepository attendanceRepo,
            AlumniRegistrationRepository registrationRepo,
            AlumniRepository alumniRepo,
            CareerHistoryRepository careerRepo,
            CompanyRepository companyRepo,
            DomainEventRepository domainEventRepo,
            EventCheckpointRepository checkpointRepo,
            ProcessedEventRepository processedEventRepo
    ) {
        this.personRepo = personRepo;
        this.userRepo = userRepo;
        this.cycleRepo = cycleRepo;
        this.applicationRepo = applicationRepo;
        this.documentRepo = documentRepo;
        this.studentRepo = studentRepo;
        this.eventRepo = eventRepo;
        this.attendanceRepo = attendanceRepo;
        this.registrationRepo = registrationRepo;
        this.alumniRepo = alumniRepo;
        this.careerRepo = careerRepo;
        this.companyRepo = companyRepo;
        this.domainEventRepo = domainEventRepo;
        this.checkpointRepo = checkpointRepo;
        this.processedEventRepo = processedEventRepo;
    }

    private static final String[] FIRST = {
            "Aarav", "Divya", "Karthik", "Meena", "Priya", "Rahul", "Sneha", "Vignesh", "Anitha", "Arun",
            "Deepa", "Gokul", "Harini", "Janani", "Kavya", "Lokesh", "Madhu", "Naveen", "Pooja", "Ramya",
            "Sanjay", "Swetha", "Tamil", "Uma", "Varun", "Yamini", "Bala", "Charan", "Dharani", "Esakki"
    };
    private static final String[] LAST = {
            "Kumar", "Raj", "Devi", "Murugan", "Selvam", "Krishnan", "Lakshmi", "Pandian", "Subramani", "Rani",
            "Natarajan", "Ganesh", "Prakash", "Sundar", "Velu"
    };
    private static final String[] DISTRICTS = {
            "Chennai", "Madurai", "Trichy", "Coimbatore", "Salem", "Tirunelveli", "Thanjavur", "Vellore", "Erode", "Dindigul"
    };
    private static final String[] DEPTS = {
            "CSE", "ECE", "EEE", "Mechanical", "Civil", "B.Com", "B.Sc Physics", "BBA"
    };
    private static final String[] EVENT_NAMES = {
            "Orientation Day", "Resume Workshop", "Alumni Meet", "Coding Bootcamp", "Mock Interview Drive",
            "Career Guidance Talk", "Soft Skills Session", "Leadership Camp", "Mentor Connect", "Annual Day"
    };
    private static final String[][] RAW_COMPANIES = {
            {"Tata Consultancy Services", "IT Services", "tcs.com"}, {"TCS", "IT Services", "tcs.com"},
            {"Infosys Ltd", "IT Services", "infosys.com"}, {"Infosys Limited", "IT Services", "infosys.com"},
            {"Zoho Corporation Pvt. Ltd.", "Software", "zoho.com"}, {"ZOHO Corp", "Software", "zoho.com"},
            {"Wipro", "IT Services", "wipro.com"}, {"HCL Technologies", "IT Services", "hcltech.com"},
            {"Freshworks Inc.", "Software", "freshworks.com"}, {"Accenture", "Consulting", "accenture.com"},
            {"Cognizant", "IT Services", "cognizant.com"}, {"L&T", "Engineering", "larsentoubro.com"}
    };

    @PostConstruct
    public synchronized void init() {
        try {
            if (userRepo.count() > 0) {
                loadFromDb();
                System.out.println("[PostgreSQL] Loaded existing records from database.");
                return;
            }
        } catch (Exception e) {
            System.out.println("[PostgreSQL] Database not reachable (" + e.getMessage() + "). Running with in-memory store until PostgreSQL is connected.");
        }
        reset();
        try {
            syncToDb();
            System.out.println("[PostgreSQL] Initialized database with seed data.");
        } catch (Exception ignored) {}
    }

    private void loadFromDb() {
        persons.clear(); persons.addAll(personRepo.findAll());
        users.clear(); users.addAll(userRepo.findAll());
        admissionCycles.clear(); admissionCycles.addAll(cycleRepo.findAll());
        applications.clear(); applications.addAll(applicationRepo.findAll());
        documents.clear(); documents.addAll(documentRepo.findAll());
        students.clear(); students.addAll(studentRepo.findAll());
        collegeEvents.clear(); collegeEvents.addAll(eventRepo.findAll());
        eventAttendance.clear(); eventAttendance.addAll(attendanceRepo.findAll());
        alumniRegistrations.clear(); alumniRegistrations.addAll(registrationRepo.findAll());
        alumni.clear(); alumni.addAll(alumniRepo.findAll());
        careerHistory.clear(); careerHistory.addAll(careerRepo.findAll());
        companies.clear(); companies.addAll(companyRepo.findAll());
        domainEvents.clear(); domainEvents.addAll(domainEventRepo.findAll());
        eventCheckpoints.clear(); eventCheckpoints.addAll(checkpointRepo.findAll());
        processedEvents.clear(); processedEvents.addAll(processedEventRepo.findAll());

        long maxEv = domainEvents.stream().mapToLong(DomainEvent::getId).max().orElse(0L);
        nextEventId.set(maxEv);
    }

    public synchronized void syncToDb() {
        try {
            personRepo.saveAll(persons);
            userRepo.saveAll(users);
            cycleRepo.saveAll(admissionCycles);
            applicationRepo.saveAll(applications);
            documentRepo.saveAll(documents);
            studentRepo.saveAll(students);
            eventRepo.saveAll(collegeEvents);
            attendanceRepo.saveAll(eventAttendance);
            registrationRepo.saveAll(alumniRegistrations);
            alumniRepo.saveAll(alumni);
            careerRepo.saveAll(careerHistory);
            companyRepo.saveAll(companies);
            domainEventRepo.saveAll(domainEvents);
            checkpointRepo.saveAll(eventCheckpoints);
            processedEventRepo.saveAll(processedEvents);
        } catch (Exception e) {
            // Silently ignore if DB is offline; in-memory store continues functioning
        }
    }

    public synchronized void reset() {
        persons.clear();
        users.clear();
        admissionCycles.clear();
        applications.clear();
        documents.clear();
        students.clear();
        collegeEvents.clear();
        eventAttendance.clear();
        alumniRegistrations.clear();
        alumni.clear();
        careerHistory.clear();
        companies.clear();
        domainEvents.clear();
        eventCheckpoints.clear();
        processedEvents.clear();

        seed = 20261112L;
        nextEventId.set(0);

        // Staff
        for (int i = 0; i < 2; i++) {
            String when = "2023-01-" + String.format("%02d", 5 + i) + "T10:00:00.000Z";
            Person p = createPersonInternal(when);
            User u = new User(
                    (long) (i + 1),
                    p.getId(),
                    i == 0 ? "admin@maatram.org" : "reviewer@maatram.org",
                    "<bcrypt-hash>",
                    i == 0 ? "ADMIN" : "STAFF",
                    true,
                    when
            );
            users.add(u);
        }

        // Companies
        for (int i = 0; i < RAW_COMPANIES.length; i++) {
            String name = RAW_COMPANIES[i][0];
            String industry = RAW_COMPANIES[i][1];
            String site = RAW_COMPANIES[i][2];
            companies.add(new Company(
                    (long) (i + 1),
                    name,
                    CompanyNormalizer.normalize(name),
                    industry,
                    "https://" + site,
                    "2023-02-01T00:00:00.000Z"
            ));
        }

        // Older cohort (40 alumni)
        for (int i = 0; i < 40; i++) {
            int admitYear = pick(new Integer[]{2019, 2020, 2021});
            int day = intVal(1, 28);
            String when = admitYear + "-07-" + String.format("%02d", day) + "T10:00:00.000Z";
            Person p = createPersonInternal(when);
            long sid = students.size() + 1;
            String dept = pick(DEPTS);
            Student s = new Student(
                    sid,
                    p.getId(),
                    null,
                    "MT" + admitYear + String.format("%04d", sid),
                    admitYear + "-07-" + String.format("%02d", day),
                    dept,
                    "GRADUATED",
                    when
            );
            students.add(s);
            emitInternal("student", sid, "STUDENT_ENROLLED", Map.of("person_id", p.getId(), "department", dept), when);

            int gradYear = admitYear + 4;
            int regDay = intVal(1, 28);
            String regWhen = gradYear + "-06-" + String.format("%02d", regDay) + "T10:00:00.000Z";
            Company comp = pick(companies.toArray(new Company[0]));
            String title = pick(new String[]{"Software Engineer", "Analyst", "Associate", "Design Engineer", "Developer", "Accountant"});
            String status = i < 30 ? "APPROVED" : (i < 36 ? "PENDING" : "REJECTED");
            long rid = alumniRegistrations.size() + 1;
            AlumniRegistration reg = new AlumniRegistration(
                    rid,
                    p.getId(),
                    sid,
                    gradYear,
                    dept,
                    comp.getCompany_name(),
                    title,
                    status,
                    "PENDING".equals(status) ? null : 1L,
                    "PENDING".equals(status) ? null : regWhen,
                    regWhen
            );
            alumniRegistrations.add(reg);
            emitInternal("alumni_registration", rid, "ALUMNI_REGISTRATION_REQUESTED", Map.of("person_id", p.getId()), regWhen);

            if ("APPROVED".equals(status)) {
                long aid = alumni.size() + 1;
                alumni.add(new Alumni(aid, p.getId(), sid, rid, gradYear, dept, "ACTIVE", regWhen));
                emitInternal("alumni", aid, "ALUMNI_APPROVED", Map.of("registration_id", rid), regWhen);

                int jobs = intVal(1, 3);
                int startYear = gradYear;
                int startMonth = 7;
                for (int j = 0; j < jobs; j++) {
                    Company c = (j == jobs - 1) ? comp : pick(companies.toArray(new Company[0]));
                    boolean isLast = (j == jobs - 1);
                    String startDateStr = String.format("%04d-%02d-01", startYear, startMonth);
                    String endDateStr = isLast ? null : String.format("%04d-%02d-01", startYear + 1, intVal(1, 12));
                    String jobTitle = isLast ? title : pick(new String[]{"Intern", "Trainee", "Junior Developer", "Associate"});
                    String empType = (j == 0 && jobs > 1) ? "INTERNSHIP" : "FULL_TIME";
                    String source = pick(new String[]{"SELF_REPORTED", "REFERRAL", "PLACEMENT"});

                    CareerHistory ch = new CareerHistory(
                            (long) (careerHistory.size() + 1),
                            aid,
                            c.getId(),
                            jobTitle,
                            empType,
                            startDateStr,
                            endDateStr,
                            isLast,
                            source,
                            startDateStr + "T00:00:00.000Z"
                    );
                    careerHistory.add(ch);
                    emitInternal("alumni", aid, "CAREER_ENTRY_ADDED", Map.of("company_id", c.getId(), "job_title", jobTitle, "source", source), startDateStr + "T00:00:00.000Z");

                    startYear++;
                }
            } else if ("REJECTED".equals(status)) {
                emitInternal("alumni_registration", rid, "ALUMNI_REGISTRATION_REJECTED", Map.of("reason", "Could not verify graduation record"), regWhen);
            }
        }

        // Admission Cycles & Applications
        Object[][] cycleData = {
                {"2024-25 Intake", "2024-25", "2024-03-01", "2024-05-31", "CLOSED", 70, 16},
                {"2025-26 Intake", "2025-26", "2025-03-01", "2025-05-31", "CLOSED", 90, 20},
                {"2026-27 Intake", "2026-27", "2026-10-01", "2026-12-15", "OPEN", 110, 0}
        };

        for (int ci = 0; ci < cycleData.length; ci++) {
            long cid = ci + 1;
            String cName = (String) cycleData[ci][0];
            String aYear = (String) cycleData[ci][1];
            String sDate = (String) cycleData[ci][2];
            String eDate = (String) cycleData[ci][3];
            String cStatus = (String) cycleData[ci][4];
            int count = (Integer) cycleData[ci][5];
            int selectCount = (Integer) cycleData[ci][6];

            admissionCycles.add(new AdmissionCycle(cid, cName, aYear, sDate, eDate, cStatus, sDate + "T00:00:00.000Z"));
            emitInternal("admission_cycle", cid, "CYCLE_CREATED", Map.of("cycle_name", cName), sDate + "T00:00:00.000Z");

            for (int i = 0; i < count; i++) {
                int dDay = intVal(1, 28);
                int dMonth = "OPEN".equals(cStatus) ? 10 : 4;
                int dYear = Integer.parseInt(aYear.substring(0, 4));
                String when = String.format("%04d-%02d-%02dT10:00:00.000Z", dYear, dMonth, dDay);
                Person p = createPersonInternal(when);
                long aid = applications.size() + 1;
                int marks = intVal(55, 99);
                String st;
                if ("OPEN".equals(cStatus)) {
                    st = pick(new String[]{"DRAFT", "SUBMITTED", "SUBMITTED", "SUBMITTED", "UNDER_REVIEW", "SHORTLISTED"});
                } else {
                    st = (i < selectCount) ? "SELECTED" : pick(new String[]{"REJECTED", "REJECTED", "REJECTED", "SHORTLISTED"});
                    if ("SHORTLISTED".equals(st)) st = "REJECTED";
                }

                Map<String, Object> appData = new HashMap<>();
                String district = pick(DISTRICTS);
                appData.put("district", district);
                appData.put("school", "Govt. Hr. Sec. School, " + district);
                appData.put("hsc_marks", marks);
                appData.put("family_income", intVal(4, 25) * 10000);
                String prefCourse = pick(DEPTS);
                appData.put("preferred_course", prefCourse);

                String appNum = "MH" + aYear.substring(2, 4) + String.format("%05d", aid);
                Application app = new Application(
                        aid,
                        p.getId(),
                        cid,
                        appNum,
                        st,
                        appData,
                        "DRAFT".equals(st) ? null : when,
                        List.of("DRAFT", "SUBMITTED").contains(st) ? null : 2L,
                        when,
                        when
                );
                applications.add(app);
                emitInternal("application", aid, "APPLICATION_DRAFTED", Map.of("cycle_id", cid), when);

                if (!"DRAFT".equals(st)) {
                    emitInternal("application", aid, "APPLICATION_SUBMITTED", Map.of("application_number", appNum), when);
                    String[] docs = {"marksheet.pdf", "income_certificate.pdf"};
                    for (int k = 0; k < docs.length; k++) {
                        boolean big = rnd() < 0.04;
                        long docId = documents.size() + 1;
                        documents.add(new DocumentRecord(
                                docId,
                                p.getId(),
                                aid,
                                docs[k],
                                "application/pdf",
                                big ? intVal(6, 9) * 1024L * 1024L : intVal(120, 1800) * 1024L,
                                "applications/" + aid + "/" + docs[k],
                                big ? "REJECTED" : "UPLOADED",
                                when
                        ));
                        if (k == 0 && big) {
                            emitInternal("document", docId, "UPLOAD_REJECTED", Map.of("reason", "File larger than 5 MB"), when);
                        }
                    }
                }

                String later = String.format("%04d-%02d-%02dT14:00:00.000Z", dYear, Math.min(12, dMonth + 1), Math.min(28, dDay + 5));
                List<String> flow = switch (st) {
                    case "UNDER_REVIEW" -> List.of("UNDER_REVIEW");
                    case "SHORTLISTED" -> List.of("UNDER_REVIEW", "SHORTLISTED");
                    case "SELECTED" -> List.of("UNDER_REVIEW", "SHORTLISTED", "SELECTED");
                    case "REJECTED" -> List.of("UNDER_REVIEW", "REJECTED");
                    default -> List.of();
                };

                String prev = "SUBMITTED";
                for (String to : flow) {
                    Map<String, Object> evData = new HashMap<>();
                    evData.put("from", prev);
                    evData.put("to", to);
                    evData.put("changed_by", 2);
                    evData.put("reason", "REJECTED".equals(to) ? pick(new String[]{"Income above eligibility limit", "Marks below cut-off", "Incomplete documents"}) : null);
                    emitInternal("application", aid, "APPLICATION_STATUS_CHANGED", evData, later);
                    prev = to;
                }

                if ("SELECTED".equals(st)) {
                    long sid = students.size() + 1;
                    String ad = dYear + "-07-15";
                    students.add(new Student(
                            sid,
                            p.getId(),
                            aid,
                            "MT" + dYear + String.format("%04d", sid),
                            ad,
                            prefCourse,
                            "ACTIVE",
                            ad + "T00:00:00.000Z"
                    ));
                    emitInternal("student", sid, "STUDENT_ENROLLED", Map.of("person_id", p.getId(), "application_id", aid), later);
                }
            }
        }

        // College events & attendance
        List<Student> activeStudents = students.stream().filter(s -> "ACTIVE".equals(s.getEnrollment_status())).toList();
        for (int i = 0; i < EVENT_NAMES.length; i++) {
            long eid = i + 1;
            int month = 7 + (i / 2);
            int day = 3 + (i % 2) * 12;
            String when = String.format("2025-%02d-%02d", month, day);
            collegeEvents.add(new CollegeEvent(
                    eid,
                    EVENT_NAMES[i],
                    EVENT_NAMES[i] + " for Maatram scholars",
                    when,
                    pick(new String[]{"Chennai HQ", "Online", "Trichy Centre", "Madurai Centre"}),
                    when + "T09:00:00.000Z"
            ));

            if (when.compareTo("2026-11-12") < 0) {
                for (Student s : activeStudents) {
                    if (rnd() < 0.15) continue;
                    String status = rnd() < 0.82 ? "PRESENT" : (rnd() < 0.5 ? "ABSENT" : "EXCUSED");
                    eventAttendance.add(new EventAttendance(
                            (long) (eventAttendance.size() + 1),
                            eid,
                            s.getId(),
                            status,
                            1L,
                            when + "T10:00:00.000Z"
                    ));
                }
                emitInternal("college_event", eid, "ATTENDANCE_MARKED", Map.of("count", activeStudents.size()), when + "T12:00:00.000Z");
            }
        }

        // Checkpoints & Processed events
        domainEvents.sort(Comparator.comparing(DomainEvent::getId));
        long maxId = domainEvents.isEmpty() ? 0 : domainEvents.get(domainEvents.size() - 1).getId();
        long mid = domainEvents.isEmpty() ? 0 : domainEvents.get((int) (domainEvents.size() * 0.85)).getId();

        String[] consumers = {"email_notifier", "report_builder"};
        for (int i = 0; i < consumers.length; i++) {
            String cName = consumers[i];
            long upto = (i == 0) ? mid : maxId;
            eventCheckpoints.add(new EventCheckpoint((long) (i + 1), cName, upto, "2026-11-10T10:00:00.000Z"));
            for (DomainEvent e : domainEvents) {
                if (e.getId() <= upto) {
                    processedEvents.add(new ProcessedEvent((long) (processedEvents.size() + 1), cName, e.getEvent_uuid(), "2026-11-10T10:00:00.000Z"));
                }
            }
        }
    }

    private Person createPersonInternal(String when) {
        String first = pick(FIRST);
        String last = pick(LAST);
        long id = persons.size() + 1;
        int birthYear = intVal(1998, 2009);
        int birthMonth = intVal(1, 12);
        int birthDay = intVal(1, 28);
        String dob = String.format("%04d-%02d-%02d", birthYear, birthMonth, birthDay);
        String phone = "9" + intVal(100000000, 999999999);
        String email = (first + "." + last + id + "@example.org").toLowerCase();
        Person p = new Person(
                id,
                first,
                last,
                email,
                phone,
                dob,
                pick(new String[]{"Female", "Male", "Female", "Male", "Other"}),
                when,
                when
        );
        persons.add(p);
        emitInternal("person", id, "PERSON_CREATED", Map.of("name", first + " " + last), when);
        return p;
    }

    private void emitInternal(String aggregate_type, Object aggregate_id, String event_type, Map<String, Object> event_data, String when) {
        long id = nextEventId.incrementAndGet();
        if (id % 37 == 0) {
            id = nextEventId.incrementAndGet();
        }
        DomainEvent e = new DomainEvent(
                id,
                UUID.randomUUID().toString(),
                aggregate_type,
                aggregate_id,
                event_type,
                event_data != null ? new HashMap<>(event_data) : new HashMap<>(),
                1,
                when,
                when
        );
        domainEvents.add(e);
    }

    public DomainEvent emit(String aggregate_type, Object aggregate_id, String event_type, Map<String, Object> event_data) {
        String now = Instant.now().toString();
        long id = nextEventId.incrementAndGet();
        DomainEvent e = new DomainEvent(
                id,
                UUID.randomUUID().toString(),
                aggregate_type,
                aggregate_id,
                event_type,
                event_data != null ? new HashMap<>(event_data) : new HashMap<>(),
                1,
                now,
                now
        );
        domainEvents.add(e);
        try {
            domainEventRepo.save(e);
        } catch (Exception ignored) {}
        return e;
    }

    public String personName(Long id) {
        if (id == null) return "-";
        return persons.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(p -> p.getFirst_name() + " " + p.getLast_name())
                .orElse("-");
    }

    public String stageOf(Long pid) {
        if (pid == null) return "PERSON";
        if (alumni.stream().anyMatch(a -> pid.equals(a.getPerson_id()))) return "ALUMNUS";
        if (students.stream().anyMatch(s -> pid.equals(s.getPerson_id()))) return "STUDENT";
        if (applications.stream().anyMatch(a -> pid.equals(a.getPerson_id()))) return "APPLICANT";
        if (users.stream().anyMatch(u -> pid.equals(u.getPerson_id()))) return "STAFF";
        return "PERSON";
    }

    public synchronized Company findOrCreateCompany(String name) {
        String norm = CompanyNormalizer.normalize(name);
        Optional<Company> found = companies.stream().filter(c -> norm.equals(c.getNormalized_name())).findFirst();
        if (found.isPresent()) {
            return found.get();
        }
        long id = companies.stream().mapToLong(Company::getId).max().orElse(0) + 1;
        Company c = new Company(id, name.trim(), norm, null, null, Instant.now().toString());
        companies.add(c);
        try {
            companyRepo.save(c);
        } catch (Exception ignored) {}
        return c;
    }

    public List<Map<String, Object>> careerRows(Long alumniId) {
        return careerHistory.stream()
                .filter(c -> c.getAlumni_id().equals(alumniId))
                .sorted(Comparator.comparing(CareerHistory::getStart_date, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(c -> {
                    Company co = companies.stream().filter(x -> x.getId().equals(c.getCompany_id())).findFirst().orElse(null);
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("alumni_id", c.getAlumni_id());
                    map.put("company_id", c.getCompany_id());
                    map.put("job_title", c.getJob_title());
                    map.put("employment_type", c.getEmployment_type());
                    map.put("start_date", c.getStart_date());
                    map.put("end_date", c.getEnd_date());
                    map.put("is_current", c.getIs_current());
                    map.put("source", c.getSource());
                    map.put("created_at", c.getCreated_at());
                    map.put("company_name", co != null ? co.getCompany_name() : null);
                    map.put("canonical_name", co != null ? co.getNormalized_name() : null);
                    return map;
                })
                .toList();
    }

    public List<DomainEvent> eventsForPerson(Long pid) {
        Set<String> keys = new HashSet<>();
        keys.add("person:" + pid);
        applications.stream().filter(a -> a.getPerson_id().equals(pid)).forEach(a -> keys.add("application:" + a.getId()));
        students.stream().filter(s -> s.getPerson_id().equals(pid)).forEach(s -> keys.add("student:" + s.getId()));
        alumniRegistrations.stream().filter(r -> r.getPerson_id().equals(pid)).forEach(r -> keys.add("alumni_registration:" + r.getId()));
        alumni.stream().filter(a -> a.getPerson_id().equals(pid)).forEach(a -> keys.add("alumni:" + a.getId()));
        documents.stream().filter(d -> d.getPerson_id().equals(pid)).forEach(d -> keys.add("document:" + d.getId()));

        return domainEvents.stream()
                .filter(e -> keys.contains(e.getAggregate_type() + ":" + e.getAggregate_id()))
                .sorted((a, b) -> {
                    int c = String.valueOf(a.getOccurred_at()).compareTo(String.valueOf(b.getOccurred_at()));
                    return c != 0 ? c : Long.compare(a.getId(), b.getId());
                })
                .toList();
    }

    private double rnd() {
        seed = (seed * 1664525L + 1013904223L) % 4294967296L;
        return (double) seed / 4294967296L;
    }

    private int intVal(int min, int max) {
        return min + (int) Math.floor(rnd() * (max - min + 1));
    }

    private <T> T pick(T[] arr) {
        return arr[(int) Math.floor(rnd() * arr.length)];
    }
}

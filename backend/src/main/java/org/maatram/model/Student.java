package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long person_id;
    private Long application_id;
    private String student_number;
    private String admission_date;
    private String department;
    private String enrollment_status;
    private String created_at;

    public Student() {}

    public Student(Long id, Long person_id, Long application_id, String student_number, String admission_date, String department, String enrollment_status, String created_at) {
        this.id = id;
        this.person_id = person_id;
        this.application_id = application_id;
        this.student_number = student_number;
        this.admission_date = admission_date;
        this.department = department;
        this.enrollment_status = enrollment_status;
        this.created_at = created_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPerson_id() { return person_id; }
    public void setPerson_id(Long person_id) { this.person_id = person_id; }
    public Long getApplication_id() { return application_id; }
    public void setApplication_id(Long application_id) { this.application_id = application_id; }
    public String getStudent_number() { return student_number; }
    public void setStudent_number(String student_number) { this.student_number = student_number; }
    public String getAdmission_date() { return admission_date; }
    public void setAdmission_date(String admission_date) { this.admission_date = admission_date; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getEnrollment_status() { return enrollment_status; }
    public void setEnrollment_status(String enrollment_status) { this.enrollment_status = enrollment_status; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

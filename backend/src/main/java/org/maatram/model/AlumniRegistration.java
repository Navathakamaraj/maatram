package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alumni_registrations")
public class AlumniRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long person_id;
    private Long student_id;
    private Integer graduation_year;
    private String department;
    private String current_company;
    private String current_job_title;
    private String registration_status;
    private Long verified_by;
    private String verified_at;
    private String registered_at;

    public AlumniRegistration() {}

    public AlumniRegistration(Long id, Long person_id, Long student_id, Integer graduation_year, String department, String current_company, String current_job_title, String registration_status, Long verified_by, String verified_at, String registered_at) {
        this.id = id;
        this.person_id = person_id;
        this.student_id = student_id;
        this.graduation_year = graduation_year;
        this.department = department;
        this.current_company = current_company;
        this.current_job_title = current_job_title;
        this.registration_status = registration_status;
        this.verified_by = verified_by;
        this.verified_at = verified_at;
        this.registered_at = registered_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPerson_id() { return person_id; }
    public void setPerson_id(Long person_id) { this.person_id = person_id; }
    public Long getStudent_id() { return student_id; }
    public void setStudent_id(Long student_id) { this.student_id = student_id; }
    public Integer getGraduation_year() { return graduation_year; }
    public void setGraduation_year(Integer graduation_year) { this.graduation_year = graduation_year; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCurrent_company() { return current_company; }
    public void setCurrent_company(String current_company) { this.current_company = current_company; }
    public String getCurrent_job_title() { return current_job_title; }
    public void setCurrent_job_title(String current_job_title) { this.current_job_title = current_job_title; }
    public String getRegistration_status() { return registration_status; }
    public void setRegistration_status(String registration_status) { this.registration_status = registration_status; }
    public Long getVerified_by() { return verified_by; }
    public void setVerified_by(Long verified_by) { this.verified_by = verified_by; }
    public String getVerified_at() { return verified_at; }
    public void setVerified_at(String verified_at) { this.verified_at = verified_at; }
    public String getRegistered_at() { return registered_at; }
    public void setRegistered_at(String registered_at) { this.registered_at = registered_at; }
}

package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alumni")
public class Alumni {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long person_id;
    private Long student_id;
    private Long registration_id;
    private Integer graduation_year;
    private String department;
    private String alumni_status;
    private String created_at;

    public Alumni() {}

    public Alumni(Long id, Long person_id, Long student_id, Long registration_id, Integer graduation_year, String department, String alumni_status, String created_at) {
        this.id = id;
        this.person_id = person_id;
        this.student_id = student_id;
        this.registration_id = registration_id;
        this.graduation_year = graduation_year;
        this.department = department;
        this.alumni_status = alumni_status;
        this.created_at = created_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPerson_id() { return person_id; }
    public void setPerson_id(Long person_id) { this.person_id = person_id; }
    public Long getStudent_id() { return student_id; }
    public void setStudent_id(Long student_id) { this.student_id = student_id; }
    public Long getRegistration_id() { return registration_id; }
    public void setRegistration_id(Long registration_id) { this.registration_id = registration_id; }
    public Integer getGraduation_year() { return graduation_year; }
    public void setGraduation_year(Integer graduation_year) { this.graduation_year = graduation_year; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getAlumni_status() { return alumni_status; }
    public void setAlumni_status(String alumni_status) { this.alumni_status = alumni_status; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

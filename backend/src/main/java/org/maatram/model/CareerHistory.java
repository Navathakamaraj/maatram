package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "career_history")
public class CareerHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long alumni_id;
    private Long company_id;
    private String job_title;
    private String employment_type;
    private String start_date;
    private String end_date;
    private Boolean is_current;
    private String source;
    private String created_at;

    public CareerHistory() {}

    public CareerHistory(Long id, Long alumni_id, Long company_id, String job_title, String employment_type, String start_date, String end_date, Boolean is_current, String source, String created_at) {
        this.id = id;
        this.alumni_id = alumni_id;
        this.company_id = company_id;
        this.job_title = job_title;
        this.employment_type = employment_type;
        this.start_date = start_date;
        this.end_date = end_date;
        this.is_current = is_current;
        this.source = source;
        this.created_at = created_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAlumni_id() { return alumni_id; }
    public void setAlumni_id(Long alumni_id) { this.alumni_id = alumni_id; }
    public Long getCompany_id() { return company_id; }
    public void setCompany_id(Long company_id) { this.company_id = company_id; }
    public String getJob_title() { return job_title; }
    public void setJob_title(String job_title) { this.job_title = job_title; }
    public String getEmployment_type() { return employment_type; }
    public void setEmployment_type(String employment_type) { this.employment_type = employment_type; }
    public String getStart_date() { return start_date; }
    public void setStart_date(String start_date) { this.start_date = start_date; }
    public String getEnd_date() { return end_date; }
    public void setEnd_date(String end_date) { this.end_date = end_date; }
    public Boolean getIs_current() { return is_current; }
    public void setIs_current(Boolean is_current) { this.is_current = is_current; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

package org.maatram.model;

import jakarta.persistence.*;
import java.util.Map;

@Entity
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long person_id;
    private Long cycle_id;
    private String application_number;
    private String status;

    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> application_data;

    private String submitted_at;
    private Long reviewed_by;
    private String created_at;
    private String updated_at;

    public Application() {}

    public Application(Long id, Long person_id, Long cycle_id, String application_number, String status, Map<String, Object> application_data, String submitted_at, Long reviewed_by, String created_at, String updated_at) {
        this.id = id;
        this.person_id = person_id;
        this.cycle_id = cycle_id;
        this.application_number = application_number;
        this.status = status;
        this.application_data = application_data;
        this.submitted_at = submitted_at;
        this.reviewed_by = reviewed_by;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPerson_id() { return person_id; }
    public void setPerson_id(Long person_id) { this.person_id = person_id; }
    public Long getCycle_id() { return cycle_id; }
    public void setCycle_id(Long cycle_id) { this.cycle_id = cycle_id; }
    public String getApplication_number() { return application_number; }
    public void setApplication_number(String application_number) { this.application_number = application_number; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, Object> getApplication_data() { return application_data; }
    public void setApplication_data(Map<String, Object> application_data) { this.application_data = application_data; }
    public String getSubmitted_at() { return submitted_at; }
    public void setSubmitted_at(String submitted_at) { this.submitted_at = submitted_at; }
    public Long getReviewed_by() { return reviewed_by; }
    public void setReviewed_by(Long reviewed_by) { this.reviewed_by = reviewed_by; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
}

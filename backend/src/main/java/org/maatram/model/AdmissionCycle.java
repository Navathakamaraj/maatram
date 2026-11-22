package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admission_cycles")
public class AdmissionCycle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cycle_name;
    private String academic_year;
    private String start_date;
    private String end_date;
    private String status;
    private String created_at;

    public AdmissionCycle() {}

    public AdmissionCycle(Long id, String cycle_name, String academic_year, String start_date, String end_date, String status, String created_at) {
        this.id = id;
        this.cycle_name = cycle_name;
        this.academic_year = academic_year;
        this.start_date = start_date;
        this.end_date = end_date;
        this.status = status;
        this.created_at = created_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCycle_name() { return cycle_name; }
    public void setCycle_name(String cycle_name) { this.cycle_name = cycle_name; }
    public String getAcademic_year() { return academic_year; }
    public void setAcademic_year(String academic_year) { this.academic_year = academic_year; }
    public String getStart_date() { return start_date; }
    public void setStart_date(String start_date) { this.start_date = start_date; }
    public String getEnd_date() { return end_date; }
    public void setEnd_date(String end_date) { this.end_date = end_date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

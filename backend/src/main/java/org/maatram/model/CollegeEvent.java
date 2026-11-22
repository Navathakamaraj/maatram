package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "college_events")
public class CollegeEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String event_name;
    private String description;
    private String event_date;
    private String location;
    private String created_at;

    public CollegeEvent() {}

    public CollegeEvent(Long id, String event_name, String description, String event_date, String location, String created_at) {
        this.id = id;
        this.event_name = event_name;
        this.description = description;
        this.event_date = event_date;
        this.location = location;
        this.created_at = created_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEvent_name() { return event_name; }
    public void setEvent_name(String event_name) { this.event_name = event_name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEvent_date() { return event_date; }
    public void setEvent_date(String event_date) { this.event_date = event_date; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

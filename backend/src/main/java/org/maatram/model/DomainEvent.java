package org.maatram.model;

import jakarta.persistence.*;
import java.util.Map;

@Entity
@Table(name = "domain_events")
public class DomainEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String event_uuid;
    private String aggregate_type;
    private String aggregate_id;
    private String event_type;

    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> event_data;

    private Integer event_version;
    private String occurred_at;
    private String created_at;

    public DomainEvent() {}

    public DomainEvent(Long id, String event_uuid, String aggregate_type, Object aggregate_id, String event_type, Map<String, Object> event_data, Integer event_version, String occurred_at, String created_at) {
        this.id = id;
        this.event_uuid = event_uuid;
        this.aggregate_type = aggregate_type;
        this.aggregate_id = aggregate_id != null ? String.valueOf(aggregate_id) : null;
        this.event_type = event_type;
        this.event_data = event_data;
        this.event_version = event_version;
        this.occurred_at = occurred_at;
        this.created_at = created_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEvent_uuid() { return event_uuid; }
    public void setEvent_uuid(String event_uuid) { this.event_uuid = event_uuid; }
    public String getAggregate_type() { return aggregate_type; }
    public void setAggregate_type(String aggregate_type) { this.aggregate_type = aggregate_type; }
    public Object getAggregate_id() { return aggregate_id; }
    public void setAggregate_id(Object aggregate_id) { this.aggregate_id = aggregate_id != null ? String.valueOf(aggregate_id) : null; }
    public String getEvent_type() { return event_type; }
    public void setEvent_type(String event_type) { this.event_type = event_type; }
    public Map<String, Object> getEvent_data() { return event_data; }
    public void setEvent_data(Map<String, Object> event_data) { this.event_data = event_data; }
    public Integer getEvent_version() { return event_version; }
    public void setEvent_version(Integer event_version) { this.event_version = event_version; }
    public String getOccurred_at() { return occurred_at; }
    public void setOccurred_at(String occurred_at) { this.occurred_at = occurred_at; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

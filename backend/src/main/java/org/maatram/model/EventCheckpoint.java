package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "event_checkpoints")
public class EventCheckpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String consumer_name;
    private Long last_processed_event_id;
    private String updated_at;

    public EventCheckpoint() {}

    public EventCheckpoint(Long id, String consumer_name, Long last_processed_event_id, String updated_at) {
        this.id = id;
        this.consumer_name = consumer_name;
        this.last_processed_event_id = last_processed_event_id;
        this.updated_at = updated_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConsumer_name() { return consumer_name; }
    public void setConsumer_name(String consumer_name) { this.consumer_name = consumer_name; }
    public Long getLast_processed_event_id() { return last_processed_event_id; }
    public void setLast_processed_event_id(Long last_processed_event_id) { this.last_processed_event_id = last_processed_event_id; }
    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
}

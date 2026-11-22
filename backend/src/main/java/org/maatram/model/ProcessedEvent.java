package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String consumer_name;
    private String event_uuid;
    private String processed_at;

    public ProcessedEvent() {}

    public ProcessedEvent(Long id, String consumer_name, String event_uuid, String processed_at) {
        this.id = id;
        this.consumer_name = consumer_name;
        this.event_uuid = event_uuid;
        this.processed_at = processed_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConsumer_name() { return consumer_name; }
    public void setConsumer_name(String consumer_name) { this.consumer_name = consumer_name; }
    public String getEvent_uuid() { return event_uuid; }
    public void setEvent_uuid(String event_uuid) { this.event_uuid = event_uuid; }
    public String getProcessed_at() { return processed_at; }
    public void setProcessed_at(String processed_at) { this.processed_at = processed_at; }
}

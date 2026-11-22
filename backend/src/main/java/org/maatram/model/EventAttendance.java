package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "event_attendance")
public class EventAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long event_id;
    private Long student_id;
    private String attendance_status;
    private Long marked_by;
    private String marked_at;

    public EventAttendance() {}

    public EventAttendance(Long id, Long event_id, Long student_id, String attendance_status, Long marked_by, String marked_at) {
        this.id = id;
        this.event_id = event_id;
        this.student_id = student_id;
        this.attendance_status = attendance_status;
        this.marked_by = marked_by;
        this.marked_at = marked_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEvent_id() { return event_id; }
    public void setEvent_id(Long event_id) { this.event_id = event_id; }
    public Long getStudent_id() { return student_id; }
    public void setStudent_id(Long student_id) { this.student_id = student_id; }
    public String getAttendance_status() { return attendance_status; }
    public void setAttendance_status(String attendance_status) { this.attendance_status = attendance_status; }
    public Long getMarked_by() { return marked_by; }
    public void setMarked_by(Long marked_by) { this.marked_by = marked_by; }
    public String getMarked_at() { return marked_at; }
    public void setMarked_at(String marked_at) { this.marked_at = marked_at; }
}

package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long person_id;
    private String email;
    private String password_hash;
    private String user_type;
    private Boolean is_active;
    private String created_at;

    public User() {}

    public User(Long id, Long person_id, String email, String password_hash, String user_type, Boolean is_active, String created_at) {
        this.id = id;
        this.person_id = person_id;
        this.email = email;
        this.password_hash = password_hash;
        this.user_type = user_type;
        this.is_active = is_active;
        this.created_at = created_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPerson_id() { return person_id; }
    public void setPerson_id(Long person_id) { this.person_id = person_id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword_hash() { return password_hash; }
    public void setPassword_hash(String password_hash) { this.password_hash = password_hash; }
    public String getUser_type() { return user_type; }
    public void setUser_type(String user_type) { this.user_type = user_type; }
    public Boolean getIs_active() { return is_active; }
    public void setIs_active(Boolean is_active) { this.is_active = is_active; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

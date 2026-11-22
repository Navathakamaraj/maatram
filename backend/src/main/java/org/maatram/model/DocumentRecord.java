package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "documents")
public class DocumentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long person_id;
    private Long application_id;
    private String file_name;
    private String content_type;
    private Long file_size;
    private String storage_key;
    private String upload_status;
    private String uploaded_at;

    public DocumentRecord() {}

    public DocumentRecord(Long id, Long person_id, Long application_id, String file_name, String content_type, Long file_size, String storage_key, String upload_status, String uploaded_at) {
        this.id = id;
        this.person_id = person_id;
        this.application_id = application_id;
        this.file_name = file_name;
        this.content_type = content_type;
        this.file_size = file_size;
        this.storage_key = storage_key;
        this.upload_status = upload_status;
        this.uploaded_at = uploaded_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPerson_id() { return person_id; }
    public void setPerson_id(Long person_id) { this.person_id = person_id; }
    public Long getApplication_id() { return application_id; }
    public void setApplication_id(Long application_id) { this.application_id = application_id; }
    public String getFile_name() { return file_name; }
    public void setFile_name(String file_name) { this.file_name = file_name; }
    public String getContent_type() { return content_type; }
    public void setContent_type(String content_type) { this.content_type = content_type; }
    public Long getFile_size() { return file_size; }
    public void setFile_size(Long file_size) { this.file_size = file_size; }
    public String getStorage_key() { return storage_key; }
    public void setStorage_key(String storage_key) { this.storage_key = storage_key; }
    public String getUpload_status() { return upload_status; }
    public void setUpload_status(String upload_status) { this.upload_status = upload_status; }
    public String getUploaded_at() { return uploaded_at; }
    public void setUploaded_at(String uploaded_at) { this.uploaded_at = uploaded_at; }
}

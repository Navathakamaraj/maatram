package org.maatram.model;

import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String company_name;
    private String normalized_name;
    private String industry;
    private String website;
    private String created_at;

    public Company() {}

    public Company(Long id, String company_name, String normalized_name, String industry, String website, String created_at) {
        this.id = id;
        this.company_name = company_name;
        this.normalized_name = normalized_name;
        this.industry = industry;
        this.website = website;
        this.created_at = created_at;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCompany_name() { return company_name; }
    public void setCompany_name(String company_name) { this.company_name = company_name; }
    public String getNormalized_name() { return normalized_name; }
    public void setNormalized_name(String normalized_name) { this.normalized_name = normalized_name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

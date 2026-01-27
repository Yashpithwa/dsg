package com.example.Datadog.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    // 🔥 Trial System Fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate;

    @Column(name = "trial_expired")
    private boolean trialExpired = false;


    // Auto set when user is created
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.trialEndDate = this.createdAt.plusDays(30);
        this.trialExpired = false;
    }

    // ===== getters & setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(LocalDateTime trialEndDate) { this.trialEndDate = trialEndDate; }

    public boolean isTrialExpired() { return trialExpired; }
    public void setTrialExpired(boolean trialExpired) { this.trialExpired = trialExpired; }
}


package com.example.e4_collab.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Set;

@Entity
@Table(name = "SESSIONS")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    @JsonProperty("username")
    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }

    private String deviceName;
    private Long startTimestamp;
    private Long endTimestamp;

    @Transient
    private Long duration;

    @Transient
    @OneToMany(mappedBy = "session", orphanRemoval = true)
    private Set<SensorSession> sensorSessions;

    public Session() {}

    public Session(User user, String deviceName, Long startTimestamp, Long endTimestamp) {
        this.user = user;
        this.deviceName = deviceName;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public Long getDuration() {
        return this.startTimestamp != null && this.endTimestamp != null ? this.endTimestamp - this.startTimestamp : null;
    }

    public Set<SensorSession> getSensorSessions() {
        return this.sensorSessions;
    }

    public void setSensorSessions(Set<SensorSession> sensorSessions) {
        this.sensorSessions = sensorSessions;
    }
}

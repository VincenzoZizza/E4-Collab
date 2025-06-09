package com.example.e4_collab.entity;

import com.example.e4_collab.entity.id.SensorSessionId;
import com.example.e4_collab.enums.SensorType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "SENSOR_SESSIONS")
@IdClass(SensorSessionId.class)
public class SensorSession {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Session session;

    @JsonProperty("session_id")
    public Long getSessionId() {
        return session != null ? session.getId() : null;
    }

    @Id
    private SensorType sensorType;

    private Integer sampleRate;

    @Lob
    private String data;

    public SensorSession() {
    }

    public SensorSession(Session session, SensorType sensorType, Integer sampleRate, String data) {
        this.session = session;
        this.sensorType = sensorType;
        this.sampleRate = sampleRate;
        this.data = data;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public Integer getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(Integer sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}



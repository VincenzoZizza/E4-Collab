package com.example.e4_collab.entity.id;

import com.example.e4_collab.entity.Session;
import com.example.e4_collab.enums.SensorType;

import java.util.Objects;

public class SensorSessionId {
    private Session session;
    private SensorType sensorType;

    public SensorSessionId() {
    }

    public SensorSessionId(Session session, SensorType sensorType) {
        this.session = session;
        this.sensorType = sensorType;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorSessionId that = (SensorSessionId) o;
        return Objects.equals(session, that.session) && sensorType == that.sensorType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, sensorType);
    }
}

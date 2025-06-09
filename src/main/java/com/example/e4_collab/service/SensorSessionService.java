package com.example.e4_collab.service;

import com.example.e4_collab.entity.SensorSession;
import com.example.e4_collab.entity.Session;
import com.example.e4_collab.enums.SensorType;
import com.example.e4_collab.repository.SensorSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Locale;

@Service
public class SensorSessionService {
    private final SensorSessionRepository sensorSessionRepository;

    @Autowired
    public SensorSessionService(SensorSessionRepository sensorSessionRepository) {
        this.sensorSessionRepository = sensorSessionRepository;
    }

    public SensorSession createSensorSession(Session session, SensorType sensorType, Integer sampleRate, String data) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }

        if (sensorType == null) {
            throw new IllegalArgumentException("SensorType cannot be null");
        }

        if (sampleRate == null || sampleRate < 0) {
            throw new RuntimeException("Invalid sample rate");
        }

        if (data == null) {
            throw new RuntimeException("Data cannot be null");
        }

        return sensorSessionRepository.save(new SensorSession(session, sensorType, sampleRate, data));
    }

    public String getCSVFromSensorSession(SensorSession sensorSession, Long startTimestamp) {
        StringBuilder csv = new StringBuilder();
        int columnsCount = sensorSession.getSensorType().equals(SensorType.ACC) ? 3 : 1;
        String sampleRate = sensorSession.getSampleRate().toString();

        if (!sensorSession.getSensorType().equals(SensorType.TAGS)) {
            String startTimestamps = String.join(",", Collections.nCopies(columnsCount, String.format(Locale.US, "%.2f", (startTimestamp / 1000.0))));
            csv.append(startTimestamps);

            if (sensorSession.getSensorType().equals(SensorType.IBI)) {
                csv.append(", IBI");
            }

            csv.append('\n');
        }

        if (!sensorSession.getSensorType().equals(SensorType.TAGS) && !sensorSession.getSensorType().equals(SensorType.IBI)) {
            String sampleRates = String.join(",", Collections.nCopies(columnsCount, sampleRate));
            csv.append(sampleRates).append('\n');
        }

        csv.append(sensorSession.getData());

        return csv.toString();
    }


    public SensorSession createSensorSessionFromCSV(Session session, SensorType sensorType, String csv) {
        if (sensorType.equals(SensorType.TAGS) || csv.isEmpty()) {
            return createSensorSession(session, sensorType, 0, csv);
        }

        int startIndex = 0;
        int endIndex = csv.indexOf("\n", startIndex);
        Long timestamp = Long.parseLong(csv.substring(startIndex, endIndex).split(",")[0].split("\\.")[0].trim());

        int sampleRate = 0;
        if (!sensorType.equals(SensorType.IBI)) {
            startIndex = endIndex + 1;
            endIndex = csv.indexOf("\n", startIndex);
            sampleRate = Integer.parseInt(csv.substring(startIndex, endIndex).split(",")[0].split("\\.")[0].trim());
        }

        startIndex = endIndex + 1;
        endIndex = csv.length();
        String data = csv.substring(startIndex, endIndex);

        return createSensorSession(session, sensorType, sampleRate, data);
    }
}
    
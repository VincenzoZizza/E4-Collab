package com.example.e4_collab.service;

import com.example.e4_collab.dto.AggregatedSessionDataDto;
import com.example.e4_collab.dto.SessionZipDto;
import com.example.e4_collab.entity.SensorSession;
import com.example.e4_collab.entity.Session;
import com.example.e4_collab.entity.User;
import com.example.e4_collab.enums.SensorType;
import com.example.e4_collab.repository.SensorSessionRepository;
import com.example.e4_collab.repository.SessionRepository;
import com.example.e4_collab.zip.ZipArchiveReader;
import com.example.e4_collab.zip.ZipArchiveWriter;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final SensorSessionRepository sensorSessionRepository;
    private final SensorSessionService sensorSessionService;
    private final UserService userService;

    @Autowired
    public SessionService(SessionRepository sessionRepository, SensorSessionRepository sensorSessionRepository, SensorSessionService sensorSessionService, UserService userService) {
        this.sessionRepository = sessionRepository;
        this.sensorSessionRepository = sensorSessionRepository;
        this.sensorSessionService = sensorSessionService;
        this.userService = userService;
    }

    public Session getSession(Long sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }

    public Collection<Session> getSessions(User user, Long from, Long to) {
        if(from != null || to != null) {
            from = from == null ? Long.MIN_VALUE : from;
            to = to == null ? Long.MAX_VALUE : to;

            return  sessionRepository.findAllByUserAndStartTimestampIsBetween(user, from, to);
        }
        else{
            return sessionRepository.findAllByUser(user);
        }
    }

    public AggregatedSessionDataDto getAggregatedSessionData(Session session) {
        Collection<SensorSession> sensorSessions = sensorSessionRepository.findAllBySession(session);

        Optional<SensorSession> accSensorSession = sensorSessions.stream().filter(sensorSession -> sensorSession.getSensorType().equals(SensorType.ACC)).findFirst();
        Optional<SensorSession> bvpSensorSession = sensorSessions.stream().filter(sensorSession -> sensorSession.getSensorType().equals(SensorType.BVP)).findFirst();
        Optional<SensorSession> edaSensorSession = sensorSessions.stream().filter(sensorSession -> sensorSession.getSensorType().equals(SensorType.EDA)).findFirst();
        Optional<SensorSession> hrSensorSession = sensorSessions.stream().filter(sensorSession -> sensorSession.getSensorType().equals(SensorType.HR)).findFirst();
        Optional<SensorSession> ibiSensorSession = sensorSessions.stream().filter(sensorSession -> sensorSession.getSensorType().equals(SensorType.IBI)).findFirst();
        Optional<SensorSession> tagsBtnSensorSession = sensorSessions.stream().filter(sensorSession -> sensorSession.getSensorType().equals(SensorType.TAGS)).findFirst();
        Optional<SensorSession> tempSensorSession = sensorSessions.stream().filter(sensorSession -> sensorSession.getSensorType().equals(SensorType.TEMP)).findFirst();

        return new AggregatedSessionDataDto(
                session,
                accSensorSession.orElse(null),
                bvpSensorSession.orElse(null),
                edaSensorSession.orElse(null),
                hrSensorSession.orElse(null),
                ibiSensorSession.orElse(null),
                tagsBtnSensorSession.orElse(null),
                tempSensorSession.orElse(null)
        );
    }

    public SessionZipDto getZipFromSession(Session session) {
        ZipArchiveWriter zipBuilder = new ZipArchiveWriter();

        Collection<SensorSession> sensorSessions = sensorSessionRepository.findAllBySession(session);

        for (SensorSession sensorSession : sensorSessions) {
            String sensorSessionCSV = sensorSessionService.getCSVFromSensorSession(sensorSession, session.getStartTimestamp());
            String fileName = sensorSession.getSensorType().name() + ".csv";
            zipBuilder.addFile(fileName, sensorSessionCSV.getBytes(StandardCharsets.UTF_8));
        }

        byte[] zipContent = zipBuilder.close();
        return new SessionZipDto(session.getId(), session.getUsername(), session.getDeviceName(), session.getStartTimestamp(), session.getEndTimestamp(), zipContent);
    }

    @Transactional
    public Session createSessionFromZip(SessionZipDto sessionZip) {
        ZipArchiveReader zipArchiveReader = new ZipArchiveReader(sessionZip.getContent());

        User user = userService.getUser(sessionZip.getUsername());
        if(user == null) {
            throw new RuntimeException("User not found");
        }

        Session session = sessionRepository.save(new Session(user, sessionZip.getDeviceName(), sessionZip.getStartTimestamp(), sessionZip.getEndTimestamp()));

        Map.Entry<String, byte[]> zipEntry;
        while ((zipEntry = zipArchiveReader.readNextFile()) != null){
            String fileName = zipEntry.getKey();
            if(!fileName.endsWith(".csv")) {
                continue;
            }

            SensorType sensorType;
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            try {
                sensorType = SensorType.valueOf(baseName.toUpperCase());
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }

            String csv = new String(zipEntry.getValue(), StandardCharsets.UTF_8);
            sensorSessionService.createSensorSessionFromCSV(session, sensorType, csv);
        }
        zipArchiveReader.close();

        return session;
    }

    public void deleteSession(Long sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}
    
package com.example.e4_collab_rest.util;

import com.example.e4_collab_rest.entity.Session;
import com.example.e4_collab_rest.entity.User;
import com.example.e4_collab_rest.enums.UserRole;
import com.example.e4_collab_rest.repository.SessionRepository;
import com.example.e4_collab_rest.service.SessionService;
import com.example.e4_collab_rest.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final SessionRepository sessionRepository;

    public DataInitializer(UserService userService, SessionRepository sessionRepository) {
        this.userService = userService;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create a regular test user if not exists
        User testUser = userService.createUserIfNotExists("testuser", "testuser@example.com", "Password123!", UserRole.ROLE_USER);

        // Generate some fake sessions for the test user
        if (testUser != null && sessionRepository.findAllByUser(testUser).isEmpty()) {
            Random random = new Random();
            long now = Instant.now().toEpochMilli();

            for (int i = 0; i < 20; i++) {
                long startTime = now - TimeUnit.DAYS.toMillis(random.nextInt(365)) - TimeUnit.HOURS.toMillis(random.nextInt(24)) - TimeUnit.MINUTES.toMillis(random.nextInt(60));
                long endTime = startTime + TimeUnit.MINUTES.toMillis(random.nextInt(60) + 1);
                String deviceName = "Device " + (random.nextInt(5) + 1);
                Session session = new Session();
                session.setStartTimestamp(startTime);
                session.setEndTimestamp(endTime);
                session.setDeviceName(deviceName);
                session.setUser(testUser);
                sessionRepository.save(session);
            }
        }
    }
} 
package com.example.e4_collab.service;

import com.example.e4_collab.entity.User;
import com.example.e4_collab.enums.UserRole;
import com.example.e4_collab.repository.SessionRepository;
import com.example.e4_collab.repository.UserRepository;
import com.example.e4_collab.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthorityService authorityService;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepository sessionRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUser(username);
    }

    @Autowired
    public UserService(UserRepository userRepository, AuthorityService authorityService, PasswordEncoder passwordEncoder, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.authorityService = authorityService;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public User createUser(String username, String email, String password, UserRole userRole) {
        if (!isValidUsername(username) || !isValidEmail(email) || !isValidPassword(password)) {
            throw new RuntimeException("Invalid username, email or password!");
        }

        if (userRepository.findById(username).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = userRepository.save(new User(username, email, encodedPassword, null));

        if (userRole != null) {
            setUserRole(user, userRole);
        }

        refreshAccessToken(user);
        return user;
    }

    @Transactional
    public User createUserIfNotExists(String username, String email, String password, UserRole userRole) {
        if (!isValidUsername(username) || !isValidEmail(email) || !isValidPassword(password)) {
            throw new RuntimeException("Invalid username, email or password!");
        }

        // Check if user already exists and return existing user
        User existingUser = userRepository.findById(username).orElse(null);
        if (existingUser != null) {
            return existingUser;
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = userRepository.save(new User(username, email, encodedPassword, null));

        if (userRole != null) {
            setUserRole(user, userRole);
        }

        refreshAccessToken(user);
        return user;
    }

    public Map<String, Object> getUserSummary(User user) {
        long sessionsCount = sessionRepository.countSessionByUser(user);
        Long minSessionDuration = sessionRepository.getMinSessionDurationByUser(user).orElse(0L);
        Long maxSessionDuration = sessionRepository.getMaxSessionDurationByUser(user).orElse(0L);
        Long averageSessionDuration = sessionRepository.getAverageSessionDurationByUser(user).orElse(0L);
        ArrayList<Map<String, Object>> monthlySessionsCounts = sessionRepository.countMonthlySessionsByUser(user.getUsername());
        Map<String, Long> monthlySessionsCountsMap = monthlySessionsCounts.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("MONTH_DATE"),
                        m -> ((Number) m.get("SESSIONS_COUNT")).longValue(),
                        Long::sum
                ));
        ArrayList<Map<String, Object>> hourlySessionsCounts = sessionRepository.countHourlySessionsByUser(user.getUsername());
        Map<String, Long> hourlySessionsCountsMap = hourlySessionsCounts.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("TIME_SLOT"),
                        m -> ((Number) m.get("SESSIONS_COUNT")).longValue(),
                        Long::sum
                ));

        Map<String, Object> userSummary = new HashMap<>();
        userSummary.put("username", user.getUsername());
        userSummary.put("sessionsCount", sessionsCount);
        userSummary.put("minSessionDuration", minSessionDuration);
        userSummary.put("maxSessionDuration", maxSessionDuration);
        userSummary.put("averageSessionDuration", averageSessionDuration);
        userSummary.put("monthlySessionsCounts", monthlySessionsCountsMap);
        userSummary.put("hourlySessionsCounts", hourlySessionsCountsMap);

        return userSummary;
    }

    public String getDecryptedAccessToken(User user) {
        try {
            return SecurityUtils.decryptAES(user.getAccessToken());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public String refreshAccessToken(User user) {
        String accessToken = UUID.randomUUID().toString().replace("-", "");
        String encryptedAccessToken = null;

        try {
            encryptedAccessToken = SecurityUtils.encryptAES(accessToken);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        user.setAccessToken(encryptedAccessToken);
        userRepository.save(user);

        return accessToken;
    }

    @Transactional
    public void setUserRole(User user, UserRole userRole) {
        authorityService.setUserAuthority(user, userRole);
    }

    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(String username) {
        return userRepository.findById(username).orElse(null);
    }

    @Transactional
    public User getUserByAccessToken(String accessToken) {
        return userRepository.findByAccessToken(accessToken).orElse(null);
    }

    public User login(String username, String password) {
        User user = getUser(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        String usernameRegex = "^[a-zA-Z0-9_]{3,}$";
        return username.matches(usernameRegex);
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])$";
        return email.matches(emailRegex);
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])(?=.*\\d).{8,}$";
        return password.matches(passwordRegex);

    }
}

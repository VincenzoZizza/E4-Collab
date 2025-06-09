package com.example.e4_collab.controller;

import com.example.e4_collab.dto.AggregatedSessionDataDto;
import com.example.e4_collab.dto.SessionZipDto;
import com.example.e4_collab.entity.Session;
import com.example.e4_collab.entity.User;
import com.example.e4_collab.enums.UserRole;
import com.example.e4_collab.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.e4_collab.security.SecurityUtils.fileMD5;

//@Profile("backend")
@RestController
public class SessionController {
    private final SessionService sessionService;
    private final UserController userController;

    @Autowired
    public SessionController(SessionService sessionService, UserController userController) {
        this.sessionService = sessionService;
        this.userController = userController;
    }

    @GetMapping(path = "/api/sessions/{id}")
    public AggregatedSessionDataDto getSession(@AuthenticationPrincipal User principal, @PathVariable("id") Long sessionId) {
        Session session = checkAccessAndGetSession(principal, sessionId);
        return sessionService.getAggregatedSessionData(session);
    }

    @DeleteMapping(path = "/api/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@AuthenticationPrincipal User principal, @PathVariable("id") Long sessionId) {
        if(principal != null && principal.getRole().equals(UserRole.ROLE_USER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        checkAccessAndGetSession(principal, sessionId);
        sessionService.deleteSession(sessionId);
    }

    @PostMapping
    public Session uploadSession(@AuthenticationPrincipal User principal, @RequestBody SessionZipDto sessionZip) {
        if (sessionZip.getStartTimestamp() == null || sessionZip.getEndTimestamp() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start timestamp and end timestamp cannot be null");
        }

        if (sessionZip.getStartTimestamp() > sessionZip.getEndTimestamp()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start timestamp cannot be greater than end timestamp");
        }

        String username = sessionZip.getUsername();
        if (username == null) {
            username = principal.getUsername();
        }

        if (username == null || username.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be empty");
        }

        if (sessionZip.getContent() == null || sessionZip.getContent().length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content cannot be empty");
        }

        if (!Objects.equals(principal.getUsername(), username)) {
            userController.getUser(principal, username);
        }
        sessionZip.setUsername(username);
        Session session = sessionService.createSessionFromZip(sessionZip);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Session creation failed");
        }

        return session;
    }

    @GetMapping(path = "/api/sessions/{id}/download")
    public SessionZipDto downloadZip(@AuthenticationPrincipal User principal, @PathVariable("id") Long sessionId) {
        if(principal != null && principal.getRole().equals(UserRole.ROLE_USER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Session session = checkAccessAndGetSession(principal, sessionId);
        return sessionService.getZipFromSession(session);
    }

    @PostMapping(value = "/collab/upload_session_chunk", consumes = MediaType.ALL_VALUE)
    public String uploadSessionChunk(@RequestPart MultipartFile file) throws IOException {
        String fileMd5 = fileMD5(file.getInputStream());
        System.out.println("Dump received: " + fileMd5);

//        try {
//            File destFile = new File("uploads/dump_" + fileMd5 + ".zip");
//            destFile.getParentFile().mkdirs();
//            file.transferTo(destFile.toPath());
//            System.out.println("Dump saved: " + fileMd5);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }

        return fileMd5;
    }

    @PostMapping(value = "/collab/new_signal_session")
    public Map<String, Object> uploadSessionChunk(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> header = new HashMap<>();
        header.put("status", "ok");
        response.put("header", header);
        return response;
    }

    private boolean hasAccess(User principal, String username) {
        return principal != null && (principal.getRole() == UserRole.ROLE_ADMIN || principal.getRole() == UserRole.ROLE_EDITOR || principal.getUsername().equals(username));
    }

    private boolean hasAccess(User principal, Session session) {
        return hasAccess(principal, session.getUsername());
    }

    private Session checkAccessAndGetSession(User principal, Long sessionId) {
        Session session = sessionService.getSession(sessionId);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }

        if (!hasAccess(principal, session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return session;
    }
}

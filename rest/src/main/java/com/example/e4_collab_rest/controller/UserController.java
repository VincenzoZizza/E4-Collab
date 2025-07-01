package com.example.e4_collab_rest.controller;

import com.example.e4_collab_rest.dto.AppLoginResultDto;
import com.example.e4_collab_rest.entity.Session;
import com.example.e4_collab_rest.entity.User;
import com.example.e4_collab_rest.enums.UserRole;
import com.example.e4_collab_rest.service.SessionService;
import com.example.e4_collab_rest.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.example.e4_collab_rest.security.SecurityUtils.decryptAES;

//@Profile({"backend", "frontend"})
@RestController
public class UserController {
    private final UserService userService;
    private final SessionService sessionService;

    @Autowired
    public UserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @GetMapping("/api/users")
    public Iterable<User> getUsers(@AuthenticationPrincipal User principal) {
        if (principal == null || (principal.getRole() != UserRole.ROLE_ADMIN.getValue() && principal.getRole() != UserRole.ROLE_EDITOR.getValue())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return userService.getAllUsers();
    }

    @GetMapping("/api/users/{username}")
    public User getUser(@AuthenticationPrincipal User principal, @PathVariable("username") String username) {
        return checkAccessAndGetUser(principal, username);
    }

    @GetMapping("/api/users/{username}/summary")
    public Map<String, Object> getUserSummary(@AuthenticationPrincipal User principal, @PathVariable("username") String username) {
        User user = checkAccessAndGetUser(principal, username);
        return userService.getUserSummary(user);
    }

    @GetMapping("/api/users/{username}/access-token")
    public Map<String, String> getAccessToken(@AuthenticationPrincipal User principal, @PathVariable("username") String username) {
        User user = checkAccessAndGetUser(principal, username);
        return Map.of("accessToken", userService.getDecryptedAccessToken(user));
    }

    @PostMapping("/api/users/{username}/access-token/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Map<String, String> refreshAccessToken(@AuthenticationPrincipal User principal, @PathVariable("username") String username) {
        User user = checkAccessAndGetUser(principal, username);
        return Map.of("accessToken", userService.refreshAccessToken(user));
    }

    @PostMapping("/api/users/{username}/role/{role}")
    public void setUserRole(@AuthenticationPrincipal User principal, @PathVariable("username") String username, @PathVariable("role") String role) {
        if(principal != null && !principal.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(role);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        }

        User user = checkAccessAndGetUser(principal, username);
        userService.setUserRole(user, userRole);
    }

    @GetMapping("/api/users/{username}/sessions")
    @Transactional
    public Collection<Session> getUserSessions(@AuthenticationPrincipal User principal, @PathVariable("username") String username, @RequestParam(value = "from", required = false) Long from, @RequestParam(value = "to", required = false) Long to) {
        User user = checkAccessAndGetUser(principal, username);
        return sessionService.getSessions(user, from, to);
    }

    @PostMapping(value = "/api/signup")
    public User createUser(@RequestBody User user) {
        String username = user.getUsername();
        String email = user.getEmail();
        String password = user.getPassword();

        if (!userService.isValidUsername(username) || !userService.isValidEmail(email) || !userService.isValidPassword(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username, email or password!");
        }

        if (userService.getUser(username) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken!");
        }

        return userService.createUser(username, email, password, UserRole.ROLE_USER);
    }

    @PostMapping(value = "/collab/app_login")
    public Map<String, Object> login(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        String username = (String) body.get("user");
        String password = (String) body.get("password");

        if(username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password cannot be empty!");
        }

        User user = userService.login(username, password);

        if(user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username or password");
        }


        try {
            String sessionId = user.getAccessToken();

            AppLoginResultDto resultDto = new AppLoginResultDto(
                    sessionId,
                    String.valueOf(Math.round((Math.random() * 9000) + 1000)),
                    user.getEmail(),
                    decryptAES(user.getAccessToken())
            );

            Cookie cookie = new Cookie("session_id", sessionId);
            cookie.setPath("/");
            cookie.setMaxAge(3600);
            cookie.setSecure(false);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return resultDto.getResponse();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/collab/auth", consumes = MediaType.ALL_VALUE)
    public Map<String, Object>  uploadSessionChunk(HttpServletRequest request, HttpServletResponse response) {

        String accessToken = null;
        String sessionId = null;
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("session_id".equals(cookie.getName())) {
                        sessionId = cookie.getValue();
                        break;
                    }
                }
            }

            if (sessionId == null || sessionId.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session id");
            }

            accessToken = decryptAES(sessionId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session id");
        }

        User user = userService.getUserByAccessToken(sessionId);



        AppLoginResultDto resultDto = new AppLoginResultDto(
                sessionId,
                String.valueOf(Math.round((Math.random() * 9000) + 1000)),
                user.getEmail(),
                accessToken
        );

        Cookie cookie = new Cookie("session_id", sessionId);
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return resultDto.getResponse();
    }

    @PostMapping(value = "/collab/check_authentication", consumes = MediaType.ALL_VALUE)
    public Map<String, Object> checkAuthentication() {
        Map<String, Object> header = new HashMap<>();
        header.put("status", "authenticated");

        Map<String, Object> response = new HashMap<>();
        response.put("header", header);
        return response;
    }

    private boolean hasAccess(User principal, String username) {
        return principal != null && (principal.getRole() == UserRole.ROLE_ADMIN.getValue() || principal.getRole() == UserRole.ROLE_EDITOR.getValue() || principal.getUsername().equals(username));
    }

    private User checkAccessAndGetUser(User principal, String username) {
        if (!hasAccess(principal, username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User user = userService.getUser(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return user;
    }
}

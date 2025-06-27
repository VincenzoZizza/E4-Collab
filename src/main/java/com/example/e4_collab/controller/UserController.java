package com.example.e4_collab.controller;

import com.example.e4_collab.dto.AppLoginResultDto;
import com.example.e4_collab.dto.UserDTO;
import com.example.e4_collab.dto.UserJSON;
import com.example.e4_collab.entity.Session;
import com.example.e4_collab.entity.User;
import com.example.e4_collab.enums.UserRole;
import com.example.e4_collab.security.CustomUserDetail;
import com.example.e4_collab.service.SessionService;
import com.example.e4_collab.service.UserService;
import com.example.e4_collab.util.DecryptUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.example.e4_collab.security.SecurityUtils.decryptAES;

//@Profile({"backend", "frontend"})
@RestController
@RequestMapping("/security")
@Log4j2
public class UserController {
    private final UserService userService;
    private final SessionService sessionService;
    
    @Autowired
    private DecryptUtil decrypt;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    public UserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }
    
    @PostMapping(value = "/signup")
    public ResponseEntity<String> createUser(@RequestBody UserDTO user) throws Exception {
    	
    	String decrtyptedPassword = decrypt.decrypt(user.getPassword());

        if (!userService.isValidUsername(user.getUsername()) || !userService.isValidEmail(user.getEmail()) || !userService.isValidPassword(decrtyptedPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username, email or password!");
        }

        if (userService.getUser(user.getUsername()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken!");
        }

        try {
        	userService.createUser(user.getUsername(), user.getEmail(), decrtyptedPassword, UserRole.ROLE_USER);
        }
        catch (Exception e) {
        	log.error(e);
        	return ResponseEntity.internalServerError().body("Errore durante la registrazione");
        }
        return ResponseEntity.ok("Registrazione effettuata");
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO request, HttpServletRequest req) throws Exception {
        try {
        	
        	String decryptedPassword = decrypt.decrypt(request.getPassword());
        	
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(request.getUsername(), decryptedPassword);

            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Crea la sessione e associa il contesto
            HttpSession session = req.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                 SecurityContextHolder.getContext());
            
            CustomUserDetail principal = (CustomUserDetail) authentication.getPrincipal();
            
            UserJSON user = new UserJSON();
            user.setUsername(principal.getUsername());
            user.setRole(principal.getRole());
            
            
            return ResponseEntity.ok(user);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali errate");
        }
    }

    @GetMapping("/api/users")
    public Iterable<User> getUsers(@AuthenticationPrincipal CustomUserDetail principal) {
        if (principal == null || (principal.getRole() != UserRole.ROLE_ADMIN.getValue() && principal.getRole() != UserRole.ROLE_EDITOR.getValue())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return userService.getAllUsers();
    }

    
    @GetMapping("/checkSession")
    public ResponseEntity<?> checkSession(@AuthenticationPrincipal CustomUserDetail principal) {
    	if(principal != null && principal.getUsername() != null && principal.getRole() != null)
    	{
            UserJSON user = new UserJSON();
            user.setUsername(principal.getUsername());
            user.setRole(principal.getRole());
            
            
            return ResponseEntity.ok(user);
    	}
    	
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sessione non valida o scaduta");
    }
    
    @GetMapping("/api/users/{username}")
    public User getUser(@AuthenticationPrincipal CustomUserDetail principal, @PathVariable("username") String username) {
        return checkAccessAndGetUser(principal, username);
    }

    @GetMapping("/users/{username}/summary")
    public Map<String, Object> getUserSummary(@AuthenticationPrincipal CustomUserDetail principal, @PathVariable("username") String username) {
        User user = checkAccessAndGetUser(principal, username);
        return userService.getUserSummary(user);
    }

    @GetMapping("/api/users/{username}/access-token")
    public Map<String, String> getAccessToken(@AuthenticationPrincipal CustomUserDetail principal, @PathVariable("username") String username) {
        User user = checkAccessAndGetUser(principal, username);
        return Map.of("accessToken", userService.getDecryptedAccessToken(user));
    }

    @PostMapping("/api/users/{username}/access-token/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Map<String, String> refreshAccessToken(@AuthenticationPrincipal CustomUserDetail principal, @PathVariable("username") String username) {
        User user = checkAccessAndGetUser(principal, username);
        return Map.of("accessToken", userService.refreshAccessToken(user));
    }

    @PostMapping("/api/users/{username}/role/{role}")
    public void setUserRole(@AuthenticationPrincipal CustomUserDetail principal, @PathVariable("username") String username, @PathVariable("role") String role) {
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

    @GetMapping("/users/{username}/sessions")
    @Transactional
    public Collection<Session> getUserSessions(@AuthenticationPrincipal CustomUserDetail principal, @PathVariable("username") String username, @RequestParam(value = "from", required = false) Long from, @RequestParam(value = "to", required = false) Long to) {
        User user = checkAccessAndGetUser(principal, username);
        return sessionService.getSessions(user, from, to);
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

    private boolean hasAccess(CustomUserDetail principal, String username) {
        return principal != null && (principal.getRole() == UserRole.ROLE_ADMIN.getValue() || principal.getRole() == UserRole.ROLE_EDITOR.getValue() || principal.getUsername().equals(username));
    }

    private User checkAccessAndGetUser(CustomUserDetail principal, String username) {
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


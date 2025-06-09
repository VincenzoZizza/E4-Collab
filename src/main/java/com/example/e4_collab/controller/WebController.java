package com.example.e4_collab.controller;

import com.example.e4_collab.entity.User;
import com.example.e4_collab.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

//@Profile("frontend")
@Controller
public class WebController {

    private final UserService userService;
    private final UserController userController;

    public WebController(UserService userService, UserController userController) {
        this.userService = userService;
        this.userController = userController;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index(@AuthenticationPrincipal User principal, Model model, HttpServletRequest request) {
        model.addAttribute("user", principal);
        model.addAttribute("username", principal.getUsername());

        return "index";
    }

    @GetMapping(value = "/sessions", produces = MediaType.TEXT_HTML_VALUE)
    public String sessions(@AuthenticationPrincipal User principal, Model model, HttpServletRequest request) {
        String username = request.getParameter("user");

        if (username != null) {
            try {
                userController.getUser(principal, username);
            } catch (Exception e) {
                return "redirect:/";
            }
        }

        model.addAttribute("user", principal);
        model.addAttribute("username", principal.getUsername());

        return "sessions";
    }

    @GetMapping(value = "/users", produces = MediaType.TEXT_HTML_VALUE)
    public String users(@AuthenticationPrincipal User principal, Model model, HttpServletRequest request) {
        model.addAttribute("user", principal);
        model.addAttribute("username", principal.getUsername());
        return "users";
    }


    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String login(@AuthenticationPrincipal User principal, Model model) {

        if (principal == null) {
            return "login";
        }
        return "redirect:/";
    }

    @GetMapping(value = "/logout", produces = MediaType.TEXT_HTML_VALUE)
    public String logout(@AuthenticationPrincipal User principal) {

        if (principal == null) {
            return "redirect:/login";
        }
        return "logout";
    }

    @GetMapping(value = "/signup", produces = MediaType.TEXT_HTML_VALUE)
    public String signup(@AuthenticationPrincipal User principal, Model model) {
        if (principal == null) {
            return "signup";
        }
        return "redirect:/";
    }
}

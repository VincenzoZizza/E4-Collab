package com.example.e4_collab.security;

import com.example.e4_collab.entity.User;
import com.example.e4_collab.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
//@Profile("backend")
public class AccessTokenAuthFilter extends OncePerRequestFilter {

    private final UserService userService;

    public AccessTokenAuthFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String encryptedAccessToken = "";

        String authorization = request.getHeader("Authorization");
        if(authorization != null && authorization.startsWith("Bearer ")){
            String accessToken = authorization.substring("Bearer ".length());
            try {
                encryptedAccessToken = SecurityUtils.encryptAES(accessToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (encryptedAccessToken != null) {
            User user = userService.getUserByAccessToken(encryptedAccessToken);

            if (user != null) {
                PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(user, null, user.getAuthorities());

                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(authentication);
                authentication.setAuthenticated(true);
            }
        }

        filterChain.doFilter(request, response);
    }
}

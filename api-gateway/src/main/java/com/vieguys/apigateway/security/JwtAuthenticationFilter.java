package com.vieguys.apigateway.security;

import com.vieguys.apigateway.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/eureka/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "Missing token");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            writeUnauthorized(response, "Invalid or expired token");
            return;
        }

        Claims claims = jwtService.extractAllClaims(token);
        String userId = jwtService.extractUserId(token);
        String userEmail = jwtService.extractUserEmail(token);
        String userName = jwtService.extractUserName(token);
        String role = claims.get("role", String.class);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        role == null
                                ? List.of()
                                : List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(new IdentityHeaderRequestWrapper(request, userEmail, userId, userName), response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    private static class IdentityHeaderRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> customHeaders = new HashMap<>();

        private IdentityHeaderRequestWrapper(
                HttpServletRequest request,
                String userEmail,
                String userId,
                String userName
        ) {
            super(request);
            putHeader("X-User-Email", userEmail);
            putHeader("X-User-Id", userId);
            putHeader("X-User-Name", userName);
        }

        @Override
        public String getHeader(String name) {
            String customHeader = customHeaders.get(name);
            return customHeader != null ? customHeader : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String customHeader = customHeaders.get(name);
            if (customHeader != null) {
                return Collections.enumeration(List.of(customHeader));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> headerNames = Collections.list(super.getHeaderNames());
            for (String customHeaderName : customHeaders.keySet()) {
                if (!headerNames.contains(customHeaderName)) {
                    headerNames.add(customHeaderName);
                }
            }
            return Collections.enumeration(headerNames);
        }

        private void putHeader(String name, String value) {
            if (value != null && !value.isBlank()) {
                customHeaders.put(name, value);
            }
        }
    }
}

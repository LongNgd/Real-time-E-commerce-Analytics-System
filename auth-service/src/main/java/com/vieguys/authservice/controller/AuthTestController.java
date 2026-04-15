package com.vieguys.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class AuthTestController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        assert authentication != null;
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        assert oAuth2User != null;
        String name = oAuth2User.getAttribute("name");

        assert name != null;
        return ResponseEntity.ok(Map.of(
                "authenticated", authentication.isAuthenticated(),
                "name", name,
                "authorities", oAuth2User.getAuthorities()
        ));
    }
}
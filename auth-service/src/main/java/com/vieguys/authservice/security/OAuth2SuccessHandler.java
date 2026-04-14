package com.vieguys.authservice.security;

import com.vieguys.authservice.model.User;
import com.vieguys.authservice.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 1. Save user nếu chưa tồn tại
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        new User(email, name, "USER")
                ));

        // 2. Generate JWT
        String token = jwtService.generateToken(user);

        // 3. Redirect về frontend với token
        response.sendRedirect("http://localhost:3000/oauth2/success?token=" + token);
    }
}
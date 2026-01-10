package com.example.Datadog.user;

import com.example.Datadog.jwt.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository repo, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
    }

    public String login(String email, String password) {
        User user = repo.findByEmail(email).orElseThrow();
        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtUtil.generateToken(email);
    }
}

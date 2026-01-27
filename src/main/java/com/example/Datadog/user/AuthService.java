package com.example.Datadog.user;

import com.example.Datadog.jwt.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Object> login(String email, String password) {

        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 🔥 Trial Expiry Check
        if (!user.isTrialExpired() &&
                LocalDateTime.now().isAfter(user.getTrialEndDate())) {

            user.setTrialExpired(true);
            repo.save(user);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.isTrialExpired());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("trialExpired", user.isTrialExpired());
        response.put("trialEndDate", user.getTrialEndDate());

        return response;
    }
}

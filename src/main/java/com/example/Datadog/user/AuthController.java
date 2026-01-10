package com.example.Datadog.user;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        service.register(user);
        return "REGISTERED";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {
        String token = service.login(user.getEmail(), user.getPassword());
        return Map.of("token", token);
    }

    // ✅ FIX HERE
    @GetMapping("/check")
    public String checkJwt() {
        return "JWT WORKING BRO 🔥";
    }

}

package com.example.Datadog.alert;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertRepository alertRepository;

    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public List<Alert> myAlerts(Authentication auth) {
        return alertRepository
                .findByService_User_EmailOrderByCreatedAtDesc(auth.getName());
    }
}

package com.example.Datadog.alert;

import com.example.Datadog.email.EmailService;
import com.example.Datadog.health.HealthCheckResult;
import com.example.Datadog.history.HealthHistory;
import com.example.Datadog.history.HealthHistoryRepository;
import com.example.Datadog.service.MointoredService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final HealthHistoryRepository historyRepository;
    private final EmailService emailService;

    public AlertService(AlertRepository alertRepository,
                        HealthHistoryRepository historyRepository,
                        EmailService emailService) {
        this.alertRepository = alertRepository;
        this.historyRepository = historyRepository;
        this.emailService = emailService;
    }

    public void evaluate(MointoredService service,
                         HealthCheckResult result) {

        List<HealthHistory> recent =
                historyRepository.findByService_IdOrderByCheckAtDesc(service.getId());

        // 🚨 DOWN ALERT
        if (recent.size() >= 2 &&
                "DOWN".equals(recent.get(0).getStatus()) &&
                "DOWN".equals(recent.get(1).getStatus())) {

            openAlert(service, "DOWN", "Service is DOWN continuously");
        }

        // 🐢 SLOW ALERT
        if (result.getResponseTime() > 2000) {
            openAlert(service, "SLOW", "Service response is slow");
        }

        // ✅ RECOVERY
        if ("UP".equals(result.getStatus())) {
            closeAlert(service, "DOWN");
            closeAlert(service, "SLOW");
        }
    }

    private void openAlert(MointoredService service, String type, String msg) {

        alertRepository
                .findByService_IdAndTypeAndStatus(service.getId(), type, "OPEN")
                .orElseGet(() -> {
                    Alert alert = new Alert();
                    alert.setService(service);
                    alert.setType(type);
                    alert.setStatus("OPEN");
                    alert.setMessage(msg);
                    alertRepository.save(alert);

                    emailService.send(
                            service.getUser().getEmail(),
                            "🚨 Alert: " + type,
                            msg + "\nService: " + service.getServiceName()
                    );

                    return alert;
                });
    }

    private void closeAlert(MointoredService service, String type) {
        alertRepository
                .findByService_IdAndTypeAndStatus(service.getId(), type, "OPEN")
                .ifPresent(alert -> {
                    alert.close();
                    alertRepository.save(alert);

                    emailService.send(
                            service.getUser().getEmail(),
                            "✅ Service Recovered",
                            service.getServiceName() + " is back UP."
                    );
                });
    }
}

package com.example.Datadog.ai;

import com.example.Datadog.ErrorRate.ErrorRateService;
import com.example.Datadog.P95.P95LatencyService;
import com.example.Datadog.RPS.RpsSerivce;
import com.example.Datadog.UpTime.UptimeService;
import com.example.Datadog.alert.AlertRepository;
import com.example.Datadog.history.HealthHistory;
import com.example.Datadog.history.HealthHistoryRepository;
import com.example.Datadog.service.MointoredService;
import com.example.Datadog.service.ServiceRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiInsightService {

    private final GroqService groqService;
    private final HealthHistoryRepository healthHistoryRepository;
    private final AlertRepository alertRepository;
    private final ServiceRepository serviceRepository;
    private final UptimeService uptimeService;
    private final ErrorRateService errorRateService;
    private final P95LatencyService p95LatencyService;
    private final RpsSerivce rpsSerivce;

    public AiInsightService(GroqService groqService,
                            HealthHistoryRepository healthHistoryRepository,
                            AlertRepository alertRepository,
                            ServiceRepository serviceRepository,
                            UptimeService uptimeService,
                            ErrorRateService errorRateService,
                            P95LatencyService p95LatencyService,
                            RpsSerivce rpsSerivce) {

        this.groqService = groqService;
        this.healthHistoryRepository = healthHistoryRepository;
        this.alertRepository = alertRepository;
        this.serviceRepository = serviceRepository;
        this.uptimeService = uptimeService;
        this.errorRateService = errorRateService;
        this.p95LatencyService = p95LatencyService;
        this.rpsSerivce = rpsSerivce;
    }

    public String generateInsight(Long serviceId) {

        MointoredService service = serviceRepository.findById(serviceId).orElseThrow();

        LocalDateTime from = LocalDateTime.now().minusHours(24);
        List<HealthHistory> history = healthHistoryRepository.findRecentHistory(serviceId, from);

        long total = history.size();
        long upCount = history.stream().filter(h -> "UP".equals(h.getStatus())).count();
        long downCount = total - upCount;

        double uptimeVal = uptimeService.calculateUpTime(serviceId, 24);
        double errorRateVal = errorRateService.calculateErrorRate(serviceId, 24);
        double p95Val = p95LatencyService.calculateP95(serviceId, 24);
        double rpsVal = rpsSerivce.calculateRPS(serviceId);

        String summary = """
                Service: %s
                Checks: %d
                UP: %d
                DOWN: %d
                Uptime: %.2f%%
                Error Rate: %.2f%%
                P95 Latency: %.2f ms
                RPS: %.2f req/s
                """.formatted(
                service.getServiceName(),
                total,
                upCount,
                downCount,
                uptimeVal,
                errorRateVal,
                p95Val,
                rpsVal
        );

        String prompt = """
You are an AI observability analyst.

Analyze this monitoring data:

%s

Return ONLY valid JSON. No explanation.

{
  "anomalies": [
    { "title": "string", "description": "string", "severity": "low | medium | high" }
  ],
  "predictions": [
    { "title": "string", "description": "string", "confidence": "0-100%%" }
  ],
  "recommendations": [
    { "title": "string", "description": "string", "priority": "low | medium | high" }
  ],
  "patterns": [
    { "day": "Monday", "level": "normal | high | low" }
  ],
  "summary": "short system analysis summary"
}
""".formatted(summary);


        return groqService.askLLM(prompt);
    }


    public String generateReason(Long serviceId) {

        MointoredService service = serviceRepository.findById(serviceId).orElseThrow();

        LocalDateTime from = LocalDateTime.now().minusHours(24);
        List<HealthHistory> history = healthHistoryRepository.findRecentHistory(serviceId, from);

        long total = history.size();
        long upCount = history.stream().filter(h -> "UP".equals(h.getStatus())).count();
        long downCount = total - upCount;

        double uptimeVal = uptimeService.calculateUpTime(serviceId, 24);
        double errorRateVal = errorRateService.calculateErrorRate(serviceId, 24);
        double p95Val = p95LatencyService.calculateP95(serviceId, 24);
        double rpsVal = rpsSerivce.calculateRPS(serviceId);

        String metrics = """
            Service Name: %s
            Total Checks: %d
            UP Count: %d
            DOWN Count: %d
            Uptime: %.2f%%
            Error Rate: %.2f%%
            P95 Latency: %.2f ms
            Requests/sec: %.2f
            """.formatted(
                service.getServiceName(),
                total,
                upCount,
                downCount,
                uptimeVal,
                errorRateVal,
                p95Val,
                rpsVal
        );

        String prompt = """
You are an AI Site Reliability Engineer (SRE) assistant.

Your task is to determine the MOST PROBABLE reason why a monitored service is DOWN.

Use the monitoring metrics below and infer the root cause.

Monitoring Data:
%s

Return ONLY valid JSON in this format:

{
  "summary": "short root cause explanation",
  "category": "Database | Network | CPU | Memory | Application | Unknown",
  "confidence": "0-100%%",
  "recommendation": "specific action engineers should take"
}

Do NOT include explanations outside JSON.
""".formatted(metrics);

        return groqService.askLLM(prompt);
    }


}

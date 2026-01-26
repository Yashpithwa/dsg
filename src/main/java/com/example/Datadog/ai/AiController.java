package com.example.Datadog.ai;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiController {


    private final AiInsightService aiInsightService;

    public AiController(AiInsightService aiInsightService)
    {
        this.aiInsightService=aiInsightService;
    }

    @GetMapping("/insight/{serviceId}")
    public String insight(@PathVariable Long serviceId){
        return aiInsightService.generateInsight(serviceId);
    }

    @GetMapping("/reason/{serviceId}")
    public String reason(@PathVariable("serviceId") Long serviceId) {
        return aiInsightService.generateReason(serviceId);
    }

}

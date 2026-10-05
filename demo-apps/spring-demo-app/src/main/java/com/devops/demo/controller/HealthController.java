package com.devops.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Spring Boot Demo App");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Jenkins CI/CD Pipeline with Shared Library");
        return response;
    }

    @GetMapping("/api/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("appName", "Spring Boot Demo App");
        response.put("version", "1.0.0");
        response.put("java", System.getProperty("java.version"));
        response.put("os", System.getProperty("os.name"));
        response.put("timestamp", LocalDateTime.now().toString());
        
        Map<String, String> features = new HashMap<>();
        features.put("jenkins", "Shared Library Integration");
        features.put("docker", "Multi-stage Build");
        features.put("deployment", "Container Deployment");
        features.put("notifications", "Telegram Integration");
        
        response.put("features", features);
        return response;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}

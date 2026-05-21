
//This is not replacing Actuator. It is just a simple application-level health endpoint for early testing.

package com.sentinelpay.common.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/api/v1/health")
    public Map<String, Object> health() {
        return Map.of(
                "application", "sentinelpay",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }
}
package ge.ticketebi.ticketebi_backend.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/status")
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);

    private final Counter requestsTotal;
    private final Counter errorsTotal;
    private final Random random = new Random();

    public MetricsController(MeterRegistry registry) {
        this.requestsTotal = Counter.builder("app_requests_total")
                .description("Total number of requests")
                .register(registry);
        this.errorsTotal = Counter.builder("app_errors_total")
                .description("Total number of errors")
                .register(registry);
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        requestsTotal.increment();

        // 30% chance of simulated error — useful for triggering the alert
        if (random.nextDouble() < 0.3) {
            errorsTotal.increment();
            logger.error("Simulated error on /api/status/ping");
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "simulated failure"));
        }

        logger.info("Ping handled successfully");
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // Call this endpoint in a loop to trigger the CRITICAL alert
    @GetMapping("/error")
    public ResponseEntity<Map<String, String>> forceError() {
        requestsTotal.increment();
        errorsTotal.increment();
        logger.error("Forced error triggered via /api/status/error");
        return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", "message", "forced error for alert testing"));
    }
}
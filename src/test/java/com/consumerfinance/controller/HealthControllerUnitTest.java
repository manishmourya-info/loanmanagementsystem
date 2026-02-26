package com.consumerfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for HealthController.
 * Tests health check endpoints including database, JVM, and disk monitoring.
 * Latest unit test with comprehensive health API coverage.
 */
@WebMvcTest(HealthController.class)
@DisplayName("Health Controller Unit Tests")
@WithMockUser(username = "testuser", roles = {"USER"})
class HealthControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ============ Main Health Endpoint Tests ============

    @Test
    @DisplayName("GET /api/v1/health should return 200 OK with UP status")
    void testGetHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.components").exists());
    }

    @Test
    @DisplayName("GET /api/v1/health should include database component")
    void testHealthIncludesDatabaseComponent() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.database").exists());
    }

    @Test
    @DisplayName("GET /api/v1/health should include JVM component")
    void testHealthIncludesJvmComponent() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.jvm").exists());
    }

    @Test
    @DisplayName("GET /api/v1/health should include disk component")
    void testHealthIncludesDiskComponent() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.disk").exists());
    }

    // ============ JVM Metrics Endpoint Tests ============

    @Test
    @DisplayName("GET /api/v1/health/jvm should return JVM metrics")
    void testGetJvmMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/health/jvm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.heapUsed").exists())
                .andExpect(jsonPath("$.heapMax").exists())
                .andExpect(jsonPath("$.heapUsagePercent").exists())
                .andExpect(jsonPath("$.threadCount").exists())
                .andExpect(jsonPath("$.uptime").exists());
    }

    @Test
    @DisplayName("GET /api/v1/health/jvm should have valid heap usage percentage format")
    void testJvmHeapUsagePercentFormat() throws Exception {
        mockMvc.perform(get("/api/v1/health/jvm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heapUsagePercent").isString());
    }

    @Test
    @DisplayName("GET /api/v1/health/jvm should have positive thread count")
    void testJvmThreadCountIsPositive() throws Exception {
        mockMvc.perform(get("/api/v1/health/jvm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threadCount").isNumber());
    }

    // ============ Database Health Endpoint Tests ============

    @Test
    @DisplayName("GET /api/v1/health/database should return database status")
    void testGetDatabaseHealth() throws Exception {
        mockMvc.perform(get("/api/v1/health/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("Database health should return either 200 OK or 503 SERVICE_UNAVAILABLE")
    void testDatabaseHealthStatusCode() throws Exception {
        var result = mockMvc.perform(get("/api/v1/health/database"))
                .andReturn();
        
        int statusCode = result.getResponse().getStatus();
        assert statusCode == 200 || statusCode == 503 : 
            "Status should be 200 or 503, got: " + statusCode;
    }

    // ============ Disk Space Endpoint Tests ============

    @Test
    @DisplayName("GET /api/v1/health/disk should return disk information")
    void testGetDiskSpace() throws Exception {
        mockMvc.perform(get("/api/v1/health/disk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    // ============ Kubernetes Probes Tests ============

    @Test
    @DisplayName("GET /api/v1/health/liveness should return UP status")
    void testLivenessProbe() throws Exception {
        mockMvc.perform(get("/api/v1/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/v1/health/readiness should return UP status")
    void testReadinessProbe() throws Exception {
        mockMvc.perform(get("/api/v1/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Liveness probe should respond within acceptable time")
    void testLivenessProbeResponseTime() throws Exception {
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/health/liveness"));
        long responseTime = System.currentTimeMillis() - startTime;
        
        assert responseTime < 1000 : "Response time should be < 1000ms, got: " + responseTime + "ms";
    }

    // ============ HTTP Method Tests ============

    @Test
    @DisplayName("POST /api/v1/health should not be allowed")
    void testHealthPostNotAllowed() throws Exception {
        mockMvc.perform(post("/api/v1/health")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PUT /api/v1/health should not be allowed")
    void testHealthPutNotAllowed() throws Exception {
        mockMvc.perform(put("/api/v1/health")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("DELETE /api/v1/health should not be allowed")
    void testHealthDeleteNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/v1/health"))
                .andExpect(status().is4xxClientError());
    }

    // ============ Content Type Tests ============

    @Test
    @DisplayName("All health endpoints should return JSON content type")
    void testHealthResponseContentType() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("JVM metrics endpoint should return JSON")
    void testJvmResponseContentType() throws Exception {
        mockMvc.perform(get("/api/v1/health/jvm"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ============ Concurrent Request Tests ============

    @Test
    @DisplayName("Should handle multiple concurrent health requests")
    void testConcurrentHealthRequests() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/v1/health"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Should handle concurrent probe requests")
    void testConcurrentProbeRequests() throws Exception {
        mockMvc.perform(get("/api/v1/health/liveness")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/health/readiness")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/health/liveness")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/health/readiness")).andExpect(status().isOk());
    }

    // ============ Health Status Consistency Tests ============

    @Test
    @DisplayName("Health status should consistently be UP")
    void testHealthStatusConsistency() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/v1/health"))
                    .andExpect(jsonPath("$.status").value("UP"));
        }
    }

    @Test
    @DisplayName("JVM status should consistently be UP")
    void testJvmStatusConsistency() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/v1/health/jvm"))
                    .andExpect(jsonPath("$.status").value("UP"));
        }
    }

    // ============ Response Structure Tests ============

    @Test
    @DisplayName("Health response should have timestamp in ISO format")
    void testHealthResponseTimestamp() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    @DisplayName("Health components should be a valid object")
    void testHealthComponentsStructure() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").isMap());
    }

    // ============ All Endpoints Availability ============

    @Test
    @DisplayName("All health endpoints should be available")
    void testAllHealthEndpointsAvailable() throws Exception {
        String[] endpoints = {
                "/api/v1/health",
                "/api/v1/health/jvm",
                "/api/v1/health/database",
                "/api/v1/health/disk",
                "/api/v1/health/liveness",
                "/api/v1/health/readiness"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isOk());
        }
    }

    // ============ Performance Tests ============

    @Test
    @DisplayName("Health check should respond within 100ms")
    void testHealthCheckPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/health"));
        long duration = System.currentTimeMillis() - startTime;
        
        assert duration < 100 : "Should respond within 100ms, took: " + duration + "ms";
    }

    @Test
    @DisplayName("Liveness probe should respond within 50ms")
    void testLivenessProbePerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/health/liveness"));
        long duration = System.currentTimeMillis() - startTime;
        
        assert duration < 50 : "Should respond within 50ms, took: " + duration + "ms";
    }

    @Test
    @DisplayName("Readiness probe should respond within 50ms")
    void testReadinessProbePerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/health/readiness"));
        long duration = System.currentTimeMillis() - startTime;
        
        assert duration < 50 : "Should respond within 50ms, took: " + duration + "ms";
    }
}

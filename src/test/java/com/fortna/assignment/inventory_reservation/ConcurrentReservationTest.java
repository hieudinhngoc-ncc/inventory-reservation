package com.fortna.assignment.inventory_reservation;

import com.fortna.assignment.inventory_reservation.api.dto.request.CreateReservationRequest;
import com.fortna.assignment.inventory_reservation.api.dto.request.ReservationItemRequest;
import com.fortna.assignment.inventory_reservation.api.dto.response.ApiResponse;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jpa.hibernate.ddl-auto=none"
})
@Testcontainers
class ConcurrentReservationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("inventory_db")
            .withUsername("fortna_user")
            .withPassword("fortna_password");

    @SuppressWarnings("resource")
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @TestConfiguration
    static class LiquibaseConfig {
        @Bean
        public SpringLiquibase liquibase(DataSource dataSource) {
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource);
            liquibase.setChangeLog("classpath:database/changelog/db.changelog-master.yaml");
            return liquibase;
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void concurrentReservations_whenCombinedQuantityExceedsStock_exactlyOneSucceeds() throws Exception {
        // A100 has 100 units from seed data. Two concurrent requests each requesting 60
        // (total 120 > 100).
        String orderId1 = "ORD-CONC-" + UUID.randomUUID();
        String orderId2 = "ORD-CONC-" + UUID.randomUUID();
        String url = "http://localhost:" + port + "/api/v1/reservations";

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);

        Future<ResponseEntity<ApiResponse>> future1 = executor.submit(() -> {
            startGate.await();
            return restTemplate.postForEntity(url, buildRequest(orderId1, "A100", 60), ApiResponse.class);
        });

        Future<ResponseEntity<ApiResponse>> future2 = executor.submit(() -> {
            startGate.await();
            return restTemplate.postForEntity(url, buildRequest(orderId2, "A100", 60), ApiResponse.class);
        });

        startGate.countDown(); // release both threads simultaneously

        ResponseEntity<ApiResponse> r1 = future1.get(15, TimeUnit.SECONDS);
        ResponseEntity<ApiResponse> r2 = future2.get(15, TimeUnit.SECONDS);
        executor.shutdown();

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        for (ResponseEntity<ApiResponse> r : List.of(r1, r2)) {
            if (r.getStatusCode() == HttpStatus.CREATED)
                successCount.incrementAndGet();
            if (r.getStatusCode() == HttpStatus.CONFLICT)
                conflictCount.incrementAndGet();
        }

        assertThat(successCount.get()).as("exactly one request should succeed").isEqualTo(1);
        assertThat(conflictCount.get()).as("exactly one request should be rejected").isEqualTo(1);

        // Verify the database reflects exactly one reservation (60 units deducted)
        ResponseEntity<ApiResponse> inventoryResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/inventory/A100", ApiResponse.class);
        assertThat(inventoryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private CreateReservationRequest buildRequest(String orderId, String sku, int quantity) {
        ReservationItemRequest item = new ReservationItemRequest();
        item.setSku(sku);
        item.setQuantity(quantity);

        CreateReservationRequest request = new CreateReservationRequest();
        request.setOrderId(orderId);
        request.setItems(List.of(item));
        return request;
    }
}

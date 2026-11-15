package org.pss.pss_solutions;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        try (PostgreSQLContainer container = new PostgreSQLContainer("postgres:16-alpine")
                .withDatabaseName("postgres")
                .withUsername("sa")
                .withPassword("password")) {
            return container;
        }
    }
}

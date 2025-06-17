package org.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class IntegrationTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init_script.sql");

    public static final WireMockServer WIREMOCK = new WireMockServer(WireMockSpring.options().dynamicPort());

    static {
        WIREMOCK.start();
        postgresContainer.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.driver-class-name=",
                "spring.jpa.properties.hibernate.dialect=",
                "spring.jpa.properties.hibernate.default_schema=chat_schema",
                "spring.datasource.url=%s".formatted(postgresContainer.getJdbcUrl()),
                "spring.datasource.driverClassName=org.postgresql.Driver",
                "spring.datasource.username=test",
                "spring.datasource.password=test",
                "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                "spring.jpa.hibernate.ddl-auto=create",
                "spring.jpa.show-sql=true",
                "spring.jpa.defer-datasource-initialization=true",
                "spring.sql.init.mode=always",
                "user.service.url=http://localhost:" + WIREMOCK.port()
        ).applyTo(applicationContext);
    }
}

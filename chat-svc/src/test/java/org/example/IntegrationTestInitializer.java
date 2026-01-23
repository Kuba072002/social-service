package org.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
public class IntegrationTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init_script.sql");

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"))
            .withEnv("RABBITMQ_DEFAULT_USER", "rabbit")
            .withEnv("RABBITMQ_DEFAULT_PASS", "rabbit")
            .withClasspathResourceMapping(
                    "definitions.json",
                    "/etc/rabbitmq/definitions.json",
                    BindMode.READ_ONLY
            )
            .withClasspathResourceMapping(
                    "rabbitmq.conf",
                    "/etc/rabbitmq/rabbitmq.conf",
                    BindMode.READ_ONLY
            )
            .withExposedPorts(5672)
            .waitingFor(Wait.forListeningPort());

    public static final WireMockServer WIREMOCK = new WireMockServer(WireMockSpring.options().dynamicPort());

    static {
        WIREMOCK.start();
        postgresContainer.start();
        rabbitMQContainer.start();
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
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getMappedPort(5672),
                "user.service.url=http://localhost:" + WIREMOCK.port()
        ).applyTo(applicationContext);
    }
}

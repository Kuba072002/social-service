package org.example;

import com.datastax.oss.driver.api.core.CqlSession;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.scylladb.ScyllaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.InetSocketAddress;
import java.time.Duration;

@SpringBootTest
@Testcontainers
public class IntegrationTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Container
    static ScyllaDBContainer scyllaDBContainer = new ScyllaDBContainer(DockerImageName.parse("scylladb/scylla:6.2"))
            .withCommand("--smp 1 --memory 512M --overprovisioned 1")
            .withStartupTimeout(Duration.ofSeconds(120));


    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"))
            .withEnv("RABBITMQ_DEFAULT_USER", "rabbit")
            .withEnv("RABBITMQ_DEFAULT_PASS", "rabbit")
            .withClasspathResourceMapping(
                    "rabbitmq.conf",
                    "/etc/rabbitmq/rabbitmq.conf",
                    BindMode.READ_ONLY
            )
            .withClasspathResourceMapping(
                    "definitions.json",
                    "/etc/rabbitmq/definitions.json",
                    BindMode.READ_ONLY
            )
            .withClasspathResourceMapping(
                    "enabled_plugins",
                    "/etc/rabbitmq/enabled_plugins",
                    BindMode.READ_ONLY
            )
            .withExposedPorts(5672, 61613, 15672)
            .waitingFor(Wait.forListeningPort());

    @Container
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:latest"))
            .withCommand(
                    "redis-server",
                    "--appendonly", "yes",
                    "--requirepass", "redis_password"
            );

    public static final WireMockServer WIREMOCK = new WireMockServer(WireMockSpring.options().dynamicPort());

    static {
        WIREMOCK.start();
        scyllaDBContainer.start();
        rabbitMQContainer.start();
        redisContainer.start();

        var session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(scyllaDBContainer.getHost(), scyllaDBContainer.getMappedPort(9042)))
                .withLocalDatacenter("datacenter1")
                .build();

        session.execute("CREATE KEYSPACE IF NOT EXISTS message_keyspace WITH replication = "
                + "{'class': 'NetworkTopologyStrategy', 'datacenter1': 1}");
        session.execute("USE message_keyspace");

        session.close();
    }


    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.cassandra.contact-points=" +
                        scyllaDBContainer.getHost() + ":" +
                        scyllaDBContainer.getMappedPort(9042),
                "spring.cassandra.local-datacenter=datacenter1",
                "spring.cassandra.keyspace-name=message_keyspace",
                "spring.data.redis.host=" + redisContainer.getHost(),
                "spring.data.redis.port=" + redisContainer.getRedisPort(),
                "user.service.url=http://localhost:" + WIREMOCK.port(),
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getMappedPort(5672),
                "stomp.rabbitmq.port=" + rabbitMQContainer.getMappedPort(61613),
                "jwt.secret=long_and_secure_jwt_secret_for_development",
                "logging.level.root=WARN",
                "logging.level.org.springframework.web=DEBUG",
                "logging.level.org.springframework.messaging=DEBUG",
                "logging.level.org.springframework.web.socket=DEBUG",
                "logging.level.org.springframework.web.socket.messaging=DEBUG"
        ).applyTo(applicationContext.getEnvironment());
    }
}

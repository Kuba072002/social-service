package org.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
public class IntegrationTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final WireMockServer WIREMOCK = new WireMockServer(WireMockSpring.options().dynamicPort());

    static {
        WIREMOCK.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "user.service.url=http://localhost:" + WIREMOCK.port() + "/user-svc",
                "chat.service.url=http://localhost:" + WIREMOCK.port() + "/chat-svc",
                "message.service.url=http://localhost:" + WIREMOCK.port() + "/message-svc",
                "jwt.secret=long_and_secure_jwt_secret_for_development",
                "jwt.expiration=3600000"
        ).applyTo(applicationContext.getEnvironment());
    }
}

package org.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class IntegrationTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final WireMockServer WIREMOCK = new WireMockServer(WireMockSpring.options().dynamicPort());

    static {
        WIREMOCK.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.driver-class-name=",
                "spring.jpa.properties.hibernate.dialect=",
                "spring.jpa.properties.hibernate.default_schema=chat_schema",
                "spring.datasource.url=jdbc:h2:mem:public;INIT=CREATE SCHEMA IF NOT EXISTS chat_schema",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=password",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create",
                "spring.jpa.show-sql=true",
                "spring.jpa.defer-datasource-initialization=true",
                "spring.sql.init.mode=always",
                "user.service.url=http://localhost:" + WIREMOCK.port()
        ).applyTo(applicationContext);
    }
}

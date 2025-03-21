package org.example;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class IntegrationTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.driver-class-name=",
                "spring.jpa.properties.hibernate.dialect=",
                "spring.jpa.properties.hibernate.default_schema=",
                "spring.datasource.url=jdbc:h2:mem:public",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=password",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create",
                "spring.jpa.show-sql=true",
                "spring.jpa.defer-datasource-initialization=true",
                "spring.sql.init.mode=always"
        ).applyTo(applicationContext);
    }
}

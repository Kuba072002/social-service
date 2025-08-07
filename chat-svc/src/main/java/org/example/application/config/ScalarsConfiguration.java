package org.example.application.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.data.federation.FederationSchemaFactory;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class ScalarsConfiguration {

    @Bean
    public RuntimeWiringConfigurer wiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.DateTime); // ISO-8601 format
    }

    @Bean
    public GraphQlSourceBuilderCustomizer customizer(FederationSchemaFactory factory) {
        return builder -> builder.schemaFactory(factory::createGraphQLSchema);
    }

    @Bean
    public FederationSchemaFactory schemaFactory() {
        return new FederationSchemaFactory();
    }
}

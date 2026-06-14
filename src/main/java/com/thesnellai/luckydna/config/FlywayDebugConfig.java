package com.thesnellai.luckydna.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayDebugConfig {

    @Bean
    CommandLineRunner flywayDebug(Flyway flyway) {
        return args -> {
            System.out.println("FLYWAY DEBUG: Flyway bean exists");
            System.out.println("FLYWAY DEBUG: Pending migrations = " + flyway.info().pending().length);
            System.out.println("FLYWAY DEBUG: Applied migrations = " + flyway.info().applied().length);
        };
    }
}
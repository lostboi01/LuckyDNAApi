package com.thesnellai.luckydna.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSourceProperties dataSourceProperties) {
        return Flyway.configure()
                .dataSource(
                        dataSourceProperties.getUrl(),
                        dataSourceProperties.getUsername(),
                        dataSourceProperties.getPassword()
                )
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .baselineDescription("Existing production schema")
                .locations("classpath:db/migration")
                .load();
    }
}
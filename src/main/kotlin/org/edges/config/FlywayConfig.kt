package org.edges.config

import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayConfig {
    @Bean
    fun flyway(dataSource: DataSource): Flyway =
        Flyway
            .configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .load()
            .also { it.migrate() }
}

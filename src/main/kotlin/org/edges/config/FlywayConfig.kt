package org.edges.config

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

@Configuration
class FlywayConfig {
    @Value("\${spring.flyway.locations}")
    lateinit var locations: String

    @Bean
    @Profile("prod")
    fun flywayProd(dataSource: DataSource): Flyway =
        Flyway
            .configure()
            .locations(locations)
            .dataSource(dataSource)
            .load()
            .apply { migrate() }

    @Bean
    @Profile("test")
    fun flywayTest(dataSource: DataSource): Flyway =
        Flyway
            .configure()
            .locations(locations)
            .dataSource(dataSource)
            .load()
            .apply { migrate() }
}

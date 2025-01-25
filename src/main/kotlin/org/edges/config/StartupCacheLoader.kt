package org.edges.config

import org.edges.utils.cache.PreloadCacheUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StartupCacheLoader {
    private val log: Logger = LoggerFactory.getLogger(StartupCacheLoader::class.java)

    @Bean
    fun preloadCacheOnStartup(preloadCacheUtils: PreloadCacheUtils): ApplicationRunner =
        ApplicationRunner {
            log.info("Initializing cache on application startup...")
            preloadCacheUtils.preloadCache()
        }
}

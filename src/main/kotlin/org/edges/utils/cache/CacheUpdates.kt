package org.edges.utils.cache

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component

@Component
class CacheUpdates(
    private val cacheManager: CacheManager,
) {
    private val log: Logger = LoggerFactory.getLogger(CacheUpdates::class.java)

    @CacheEvict(value = ["validateParentsCache"], allEntries = true)
    fun clearCacheOnEdgeChange() {
        log.info("Clearing validateParentsCache due to edge modification")
        cacheManager.getCache("validateParentsCache")?.clear()
    }
}

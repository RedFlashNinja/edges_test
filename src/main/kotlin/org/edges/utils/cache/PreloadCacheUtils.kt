package org.edges.utils.cache

import org.edges.entity.EdgeEntity
import org.edges.service.EdgeService
import org.jooq.DSLContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PreloadCacheUtils(
    private val dsl: DSLContext,
    private val edgeService: EdgeService,
) {
    private val log: Logger = LoggerFactory.getLogger(PreloadCacheUtils::class.java)

    fun preloadCache() {
        log.info("Preloading edges into cache...")

        val nodeIds =
            dsl
                .selectDistinct()
                .from(EdgeEntity.TABLE)
                .fetch(EdgeEntity.FROM_ID)
        nodeIds.forEach { nodeId ->
            edgeService.getTree(nodeId)
        }

        val allTrees = edgeService.getAllTrees()
        log.info("Preloaded forest (allTrees): {}", allTrees)

        log.info("Preloading complete.")
    }
}

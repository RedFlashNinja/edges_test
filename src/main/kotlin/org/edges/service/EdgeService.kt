package org.edges.service

import org.edges.entity.EdgeEntity
import org.edges.models.Edge
import org.edges.models.TreeNode
import org.edges.utils.cache.CacheUpdates
import org.edges.utils.validation.EdgeValidationUtils
import org.jooq.DSLContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class EdgeService(
    private val dsl: DSLContext,
    private val edgeValidationUtils: EdgeValidationUtils,
    private val cacheUpdates: CacheUpdates,
) {
    private val log: Logger = LoggerFactory.getLogger(EdgeService::class.java)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(value = ["edgesCache"], allEntries = true)
    fun addEdge(edge: Edge) {
        edgeValidationUtils.validateMaxConnections(dsl, edge)

        dsl
            .insertInto(EdgeEntity.TABLE)
            .columns(EdgeEntity.FROM_ID, EdgeEntity.TO_ID)
            .values(edge.fromId, edge.toId)
            .execute()

        cacheUpdates.clearCacheOnEdgeChange()
        log.info("Edge added successfully: {} -> {}", edge.fromId, edge.toId)
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = ["edgesCache"], allEntries = true)
    fun deleteEdge(edge: Edge): Boolean {
        val deleted =
            dsl
                .deleteFrom(EdgeEntity.TABLE)
                .where(EdgeEntity.FROM_ID.eq(edge.fromId).and(EdgeEntity.TO_ID.eq(edge.toId)))
                .execute() > 0

        if (deleted) {
            cacheUpdates.clearCacheOnEdgeChange()
            log.info("Edge deleted successfully: {} -> {}", edge.fromId, edge.toId)
        } else {
            log.warn("Edge deletion failed: {} -> {}", edge.fromId, edge.toId)
        }

        return deleted
    }

    @Cacheable(value = ["edgesCache"], key = "#nodeId")
    fun getTree(
        nodeId: Int,
        visitedNodes: MutableSet<Int> = mutableSetOf(),
    ): TreeNode? {
        if (visitedNodes.contains(nodeId)) {
            log.error("Cycle detected in graph at nodeId: {}", nodeId)
            throw IllegalStateException("Cycle detected in graph at nodeId: $nodeId")
        }

        if (nodeId == 0) {
            log.warn("Skipping nodeId = 0 to prevent infinite recursion.")
            return null
        }

        visitedNodes.add(nodeId)

        val result =
            dsl
                .select()
                .from(EdgeEntity.TABLE)
                .where(EdgeEntity.FROM_ID.eq(nodeId))
                .fetch()

        log.debug("Processing nodeId: {}, Found children: {}", nodeId, result.map { it[EdgeEntity.TO_ID] })

        if (result.isEmpty()) {
            return TreeNode(nodeId = nodeId, children = emptyList())
        }

        val childrenNodes =
            result.mapNotNull { record ->
                val childNodeId = record[EdgeEntity.TO_ID]
                val childTree = getTree(childNodeId, visitedNodes)
                log.debug("Mapping child {} -> {}", childNodeId, childTree)
                childTree
            }

        val tree = TreeNode(nodeId = nodeId, children = childrenNodes)
        log.debug("Final TreeNode: {}", tree)

        return tree
    }

    @Cacheable(value = ["edgesCache"], key = "'allTrees'")
    fun getAllTrees(): List<TreeNode> {
        val rootIds =
            dsl
                .selectDistinct(EdgeEntity.FROM_ID)
                .from(EdgeEntity.TABLE)
                .where(
                    EdgeEntity.FROM_ID.notIn(
                        dsl.select(EdgeEntity.TO_ID).from(EdgeEntity.TABLE),
                    ),
                ).fetchInto(Int::class.java)

        val forest = mutableListOf<TreeNode>()
        for (rootId in rootIds) {
            val subtree = getTree(rootId, visitedNodes = mutableSetOf())
            if (subtree != null) {
                forest.add(subtree)
            }
        }
        return forest
    }
}

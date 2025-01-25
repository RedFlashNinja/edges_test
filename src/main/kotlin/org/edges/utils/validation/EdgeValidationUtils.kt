package org.edges.utils.validation

import org.edges.entity.EdgeEntity
import org.edges.models.Edge
import org.jooq.DSLContext
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class EdgeValidationUtils {
    @Cacheable("validateConnectionsCache")
    fun validateMaxConnections(
        dsl: DSLContext,
        edge: Edge,
    ) {
        val childrenCount =
            dsl
                .selectCount()
                .from(EdgeEntity.TABLE)
                .where(EdgeEntity.FROM_ID.eq(edge.fromId))
                .fetchOne(0, Int::class.java) ?: 0

        if (childrenCount >= 2) {
            throw IllegalArgumentException(
                "Node ${edge.fromId} already has $childrenCount children. " +
                    "Cannot add a new edge from ${edge.fromId} to ${edge.toId} " +
                    "because that would exceed the limit of 2 children per node.",
            )
        }

        val parentsCount =
            dsl
                .selectCount()
                .from(EdgeEntity.TABLE)
                .where(EdgeEntity.TO_ID.eq(edge.toId))
                .fetchOne(0, Int::class.java) ?: 0

        if (parentsCount >= 1) {
            throw IllegalArgumentException(
                "Node ${edge.toId} already has a parent. " +
                    "Cannot add a new edge from ${edge.fromId} to ${edge.toId}, " +
                    "because that would exceed the limit of 1 parent per node.",
            )
        }
    }
}

package org.edges.entity

class EdgeEntity {
    companion object {
        val TABLE =
            org.jooq.impl.DSL
                .table("edge")
        val FROM_ID =
            org.jooq.impl.DSL
                .field("from_id", Int::class.java)
        val TO_ID =
            org.jooq.impl.DSL
                .field("to_id", Int::class.java)
    }
}

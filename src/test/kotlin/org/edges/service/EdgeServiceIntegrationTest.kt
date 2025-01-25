package org.edges.service

import org.edges.entity.EdgeEntity
import org.edges.models.Edge
import org.edges.models.TreeNode
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation::class)
class EdgeServiceIntegrationTest {
    @Autowired
    lateinit var dsl: DSLContext

    @Autowired
    lateinit var edgeService: EdgeService

    @BeforeEach
    fun setup() {
        dsl.deleteFrom(EdgeEntity.TABLE).execute()
    }

    @Test
    @Order(1)
    fun `testAddEdge - success`() {
        val edge = Edge(fromId = 1, toId = 2)
        edgeService.addEdge(edge)

        val count =
            dsl
                .selectCount()
                .from(EdgeEntity.TABLE)
                .where(EdgeEntity.FROM_ID.eq(1).and(EdgeEntity.TO_ID.eq(2)))
                .fetchOne(0, Int::class.java)
        assertEquals(1, count)

        val tree: TreeNode? = edgeService.getTree(1)
        assertNotNull(tree)
        assertEquals(1, tree.nodeId)
        assertEquals(1, tree.children.size)
        assertEquals(2, tree.children[0].nodeId)
    }

    @Test
    @Order(2)
    fun `testAddEdge - fails if node has 2 children already`() {
        edgeService.addEdge(Edge(4, 5))
        edgeService.addEdge(Edge(4, 6))

        try {
            edgeService.addEdge(Edge(4, 7))
            fail("Expected an IllegalArgumentException due to exceeding 2 children.")
        } catch (ex: IllegalArgumentException) {
            assertTrue(ex.message!!.contains("Node 4 already has 2 children"))
        }
    }

    @Test
    @Order(3)
    fun `testAddEdge - fails if node already has 1 parent`() {
        edgeService.addEdge(Edge(8, 9))

        try {
            edgeService.addEdge(Edge(10, 9))
            fail("Expected an IllegalArgumentException due to node 9 already having a parent.")
        } catch (ex: IllegalArgumentException) {
            assertTrue(ex.message!!.contains("Node 9 already has a parent"))
        }
    }

    @Test
    @Order(4)
    fun `testDeleteEdge - success`() {
        edgeService.addEdge(Edge(2, 3))

        val beforeDeleteCount =
            dsl
                .selectCount()
                .from(EdgeEntity.TABLE)
                .where(EdgeEntity.FROM_ID.eq(2).and(EdgeEntity.TO_ID.eq(3)))
                .fetchOne(0, Int::class.java)
        assertEquals(1, beforeDeleteCount)

        val deleted = edgeService.deleteEdge(Edge(2, 3))
        assertTrue(deleted)

        val afterDeleteCount =
            dsl
                .selectCount()
                .from(EdgeEntity.TABLE)
                .where(EdgeEntity.FROM_ID.eq(2).and(EdgeEntity.TO_ID.eq(3)))
                .fetchOne(0, Int::class.java)
        assertEquals(0, afterDeleteCount)
    }

    @Test
    @Order(5)
    fun `testGetTree - returns null if nodeId=0 or if node not in DB`() {
        val treeZero = edgeService.getTree(0)
        Assertions.assertNull(treeZero)

        val tree = edgeService.getTree(999)
        assertNotNull(tree)
        Assertions.assertEquals(tree.nodeId, 999)
        Assertions.assertTrue(tree.children.isEmpty())
    }

    @Test
    @Order(6)
    fun `testGetAllTrees - returns multiple roots`() {
        edgeService.addEdge(Edge(1, 2))
        edgeService.addEdge(Edge(3, 4))
        edgeService.addEdge(Edge(3, 5))

        val forest = edgeService.getAllTrees()
        assertEquals(2, forest.size)

        val root1 = forest.find { it.nodeId == 1 }
        val root3 = forest.find { it.nodeId == 3 }
        assertNotNull(root1)
        assertNotNull(root3)

        assertEquals(1, root1.children.size)
        assertEquals(2, root1.children[0].nodeId)

        assertEquals(2, root3.children.size)
    }
}

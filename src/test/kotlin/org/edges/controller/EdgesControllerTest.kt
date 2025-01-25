package org.edges.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.edges.controller.edge.EdgesController
import org.edges.controller.edge.requests.EdgeAddingRequestDto
import org.edges.controller.edge.requests.EdgeDeletingRequestDto
import org.edges.mapper.EdgeMapper
import org.edges.models.TreeNode
import org.edges.service.EdgeService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(EdgesController::class)
@ActiveProfiles("test")
class EdgesControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var edgeService: EdgeService

    @MockBean
    lateinit var edgeMapper: EdgeMapper

    @Test
    fun `createEdge - success`() {
        val requestDto = EdgeAddingRequestDto(fromId = 1, toId = 2)
        whenever(edgeMapper.addRequestToDto(any())).thenReturn(org.edges.models.Edge(1, 2))

        mockMvc
            .perform(
                post("/api/edges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)),
            ).andExpect(status().isOk)
            .andExpect(content().string("Edge added successfully"))
    }

    @Test
    fun `createEdge - bad request if fromId or toId is 0`() {
        val requestDto = EdgeAddingRequestDto(fromId = 0, toId = 2)

        mockMvc
            .perform(
                post("/api/edges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)),
            ).andExpect(status().isBadRequest)
            .andExpect(content().string("Invalid Edge: fromId and toId must be greater than 0"))
    }

    @Test
    fun `createEdge - bad request if fromId equals toId`() {
        val requestDto = EdgeAddingRequestDto(fromId = 3, toId = 3)

        mockMvc
            .perform(
                post("/api/edges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)),
            ).andExpect(status().isBadRequest)
            .andExpect(content().string("Invalid Edge: fromId and toId cannot be the same"))
    }

    @Test
    fun `createEdge - service throws IllegalArgumentException`() {
        val requestDto = EdgeAddingRequestDto(fromId = 1, toId = 2)
        whenever(edgeMapper.addRequestToDto(any())).thenReturn(org.edges.models.Edge(1, 2))
        whenever(edgeService.addEdge(any())).thenThrow(IllegalArgumentException("Test error"))

        mockMvc
            .perform(
                post("/api/edges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)),
            ).andExpect(status().isBadRequest)
            .andExpect(content().string("Test error"))
    }

    @Test
    fun `getAllTrees - returns 200 with forest`() {
        val treeNodeA = TreeNode(nodeId = 1, children = emptyList())
        val treeNodeB = TreeNode(nodeId = 15, children = emptyList())
        whenever(edgeService.getAllTrees()).thenReturn(listOf(treeNodeA, treeNodeB))

        mockMvc
            .perform(get("/api/edges/all"))
            .andExpect(status().isOk)
            .andExpect {
                val responseContent = it.response.contentAsString
                println("Response: $responseContent")
                // You could parse JSON if you want with Jackson, or just do substring checks
            }
    }

    @Test
    fun `getTree - success`() {
        val treeNode = TreeNode(nodeId = 4, children = listOf(TreeNode(nodeId = 5, children = emptyList())))
        whenever(edgeService.getTree(4)).thenReturn(treeNode)

        mockMvc
            .perform(get("/api/edges/tree/4"))
            .andExpect(status().isOk)
            .andExpect {
                val responseJson = it.response.contentAsString
                println("Response: $responseJson")
            }
    }

    @Test
    fun `getTree - node not found`() {
        whenever(edgeService.getTree(999)).thenReturn(null)

        mockMvc
            .perform(get("/api/edges/tree/999"))
            .andExpect(status().isBadRequest)
            .andExpect(content().string("nodeId was not found"))
    }

    @Test
    fun `deleteEdge - success`() {
        val requestDto = EdgeDeletingRequestDto(fromId = 2, toId = 3)
        whenever(edgeMapper.delRequestToDto(any())).thenReturn(org.edges.models.Edge(2, 3))
        whenever(edgeService.deleteEdge(any())).thenReturn(true)

        mockMvc
            .perform(
                delete("/api/edges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)),
            ).andExpect(status().isOk)
            .andExpect(content().string("Edge deleted successfully"))
    }

    @Test
    fun `deleteEdge - edge does not exist`() {
        val requestDto = EdgeDeletingRequestDto(fromId = 2, toId = 3)
        whenever(edgeMapper.delRequestToDto(any())).thenReturn(org.edges.models.Edge(2, 3))
        whenever(edgeService.deleteEdge(any())).thenReturn(false)

        mockMvc
            .perform(
                delete("/api/edges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)),
            ).andExpect(status().isBadRequest)
            .andExpect(content().string("Edge does not exist"))
    }
}

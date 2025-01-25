package org.edges.controller.edge

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.edges.controller.edge.requests.EdgeAddingRequestDto
import org.edges.controller.edge.requests.EdgeDeletingRequestDto
import org.edges.mapper.EdgeMapper
import org.edges.models.TreeNode
import org.edges.service.EdgeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/edges")
@Tag(name = "Edges API", description = "API for managing edges in the graph")
class EdgesController(
    private val edgeService: EdgeService,
    private val edgeMapper: EdgeMapper,
) {
    @PostMapping
    @Operation(summary = "Add a new edge", description = "Creates a new edge between two nodes")
    fun createEdge(
        @RequestBody request: EdgeAddingRequestDto,
    ): ResponseEntity<String> {
        if (request.fromId <= 0 || request.toId <= 0) {
            return ResponseEntity.badRequest().body("Invalid Edge: fromId and toId must be greater than 0")
        }
        if (request.fromId == request.toId) {
            return ResponseEntity.badRequest().body("Invalid Edge: fromId and toId cannot be the same")
        }

        return try {
            edgeService.addEdge(edgeMapper.addRequestToDto(request))
            ResponseEntity.ok("Edge added successfully")
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @GetMapping("/all")
    @Operation(
        summary = "Get all trees",
        description = "Returns all disconnected tree subgraphs in the system as a forest.",
    )
    fun getAllTrees(): ResponseEntity<List<TreeNode>> {
        val forest = edgeService.getAllTrees()
        return ResponseEntity.ok(forest)
    }

    @GetMapping("/tree/{nodeId}")
    @Operation(summary = "Get tree structure", description = "Retrieves the tree structure for a given node ID")
    fun getTree(
        @PathVariable nodeId: Int,
    ): ResponseEntity<Any> {
        val tree = edgeService.getTree(nodeId)

        return if (tree == null) {
            ResponseEntity.badRequest().body("nodeId was not found")
        } else {
            ResponseEntity.ok(tree)
        }
    }

    @DeleteMapping
    @Operation(summary = "Delete an edge", description = "Deletes a given Edge")
    fun deleteEdge(
        @RequestBody request: EdgeDeletingRequestDto,
    ): ResponseEntity<String> =
        if (edgeService.deleteEdge(edgeMapper.delRequestToDto(request))) {
            ResponseEntity.ok("Edge deleted successfully")
        } else {
            ResponseEntity.badRequest().body("Edge does not exist")
        }
}

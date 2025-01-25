package org.edges.mapper

import org.edges.controller.edge.requests.EdgeAddingRequestDto
import org.edges.controller.edge.requests.EdgeDeletingRequestDto
import org.edges.models.Edge
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface EdgeMapper {
    @Mapping(source = "fromId", target = "fromId")
    @Mapping(source = "toId", target = "toId")
    fun addRequestToDto(request: EdgeAddingRequestDto): Edge

    @Mapping(source = "fromId", target = "fromId")
    @Mapping(source = "toId", target = "toId")
    fun delRequestToDto(request: EdgeDeletingRequestDto): Edge
}

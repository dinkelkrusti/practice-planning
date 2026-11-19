package io.github.dinkelkrusti.practiceplanning.piece

import io.github.dinkelkrusti.practiceplanning.api.PiecesApi
import io.github.dinkelkrusti.practiceplanning.api.model.Piece
import io.github.dinkelkrusti.practiceplanning.api.model.PieceCreate
import io.github.dinkelkrusti.practiceplanning.api.model.PieceList
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.ZoneOffset
import java.util.UUID

/** Implements the interface generated from api/openapi.yaml. No API shape is defined here. */
@RestController
class PieceController(private val service: PieceService) : PiecesApi {

    override fun listPieces(): ResponseEntity<PieceList> =
        ResponseEntity.ok(PieceList(items = service.list().map { it.toApi() }))

    override fun getPiece(pieceId: UUID): ResponseEntity<Piece> {
        val piece = service.find(pieceId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No piece with id $pieceId")
        return ResponseEntity.ok(piece.toApi())
    }

    override fun createPiece(pieceCreate: PieceCreate): ResponseEntity<Piece> {
        val piece = service.create(pieceCreate.title, pieceCreate.composer)
        val location = ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(piece.id)
            .toUri()
        return ResponseEntity.created(location).body(piece.toApi())
    }
}

private fun PieceEntity.toApi() = Piece(
    id = id,
    title = title,
    composer = composer,
    createdAt = createdAt.atOffset(ZoneOffset.UTC),
)

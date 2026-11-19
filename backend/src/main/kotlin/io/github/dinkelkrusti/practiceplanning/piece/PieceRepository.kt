package io.github.dinkelkrusti.practiceplanning.piece

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PieceRepository : JpaRepository<PieceEntity, UUID> {
    fun findAllByOrderByCreatedAtDesc(): List<PieceEntity>
}

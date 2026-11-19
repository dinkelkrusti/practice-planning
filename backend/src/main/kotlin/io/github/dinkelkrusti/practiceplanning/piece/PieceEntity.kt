package io.github.dinkelkrusti.practiceplanning.piece

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "piece")
class PieceEntity(
    @Id
    val id: UUID,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(length = 200)
    var composer: String?,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
)

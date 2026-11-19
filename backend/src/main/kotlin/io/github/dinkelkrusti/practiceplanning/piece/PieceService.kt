package io.github.dinkelkrusti.practiceplanning.piece

import io.github.dinkelkrusti.practiceplanning.config.UuidV7
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
@Transactional(readOnly = true)
class PieceService(
    private val repository: PieceRepository,
    private val clock: Clock,
) {
    fun list(): List<PieceEntity> = repository.findAllByOrderByCreatedAtDesc()

    fun find(id: UUID): PieceEntity? = repository.findById(id).orElse(null)

    @Transactional
    fun create(title: String, composer: String?): PieceEntity {
        // Postgres stores microseconds; truncate so the response matches what is stored.
        val now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS)
        return repository.save(
            PieceEntity(
                id = UuidV7.at(now),
                title = title.trim(),
                composer = composer?.trim()?.ifEmpty { null },
                createdAt = now,
            ),
        )
    }
}

package io.github.dinkelkrusti.practiceplanning

import io.github.dinkelkrusti.practiceplanning.config.UuidV7
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class UuidV7Test {
    @Test
    fun `has version 7, RFC variant and the timestamp in the first 48 bits`() {
        val instant = Instant.parse("2026-11-19T12:00:00.123Z")
        val uuid = UuidV7.at(instant)
        assertEquals(7, uuid.version())
        assertEquals(2, uuid.variant())
        assertEquals(instant.toEpochMilli(), uuid.mostSignificantBits ushr 16)
    }

    @Test
    fun `later ids sort after earlier ids`() {
        val earlier = UuidV7.at(Instant.parse("2026-11-19T12:00:00Z"))
        val later = UuidV7.at(Instant.parse("2026-11-19T12:00:01Z"))
        assertTrue(earlier.toString() < later.toString())
    }
}

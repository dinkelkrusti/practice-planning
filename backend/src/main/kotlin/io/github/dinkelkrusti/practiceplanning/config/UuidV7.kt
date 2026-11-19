package io.github.dinkelkrusti.practiceplanning.config

import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

/**
 * Time-ordered UUIDs (RFC 9562 version 7). Ids created later sort later, which
 * keeps B-tree index inserts at the end of the index instead of random pages.
 * JDK 25 has no built-in v7 generator yet.
 */
object UuidV7 {
    private val random = SecureRandom()

    fun at(instant: Instant): UUID {
        val millis = instant.toEpochMilli()
        val randA = random.nextInt(1 shl 12).toLong()
        val msb = (millis shl 16) or (0x7L shl 12) or randA
        val lsb = (random.nextLong() and 0x3FFF_FFFF_FFFF_FFFFL) or Long.MIN_VALUE // variant 10
        return UUID(msb, lsb)
    }
}

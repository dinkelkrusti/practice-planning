package io.github.dinkelkrusti.practiceplanning.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class ClockConfig {
    /** Injected instead of calling Instant.now() directly, so tests can control time. */
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}

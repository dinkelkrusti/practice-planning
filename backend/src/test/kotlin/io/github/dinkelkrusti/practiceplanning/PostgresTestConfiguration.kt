package io.github.dinkelkrusti.practiceplanning

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/** Real Postgres for tests, same image as compose.yaml (kept in sync by Renovate). */
@TestConfiguration(proxyBeanMethods = false)
class PostgresTestConfiguration {
    @Bean
    @ServiceConnection
    fun postgres(): PostgreSQLContainer =
        // The digest-pinned reference is the official image; Testcontainers just cannot tell from the name.
        PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE).asCompatibleSubstituteFor("postgres"))

    companion object {
        const val POSTGRES_IMAGE =
            "postgres:18.7-alpine@sha256:ff64c9e3bdf8e2120107d3559de8db314e8305f7e38c5105903c6295876738ff"
    }
}

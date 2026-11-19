package io.github.dinkelkrusti.practiceplanning.piece

import io.github.dinkelkrusti.practiceplanning.PostgresTestConfiguration
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

/** End to end through HTTP, Spring, Hibernate and Liquibase against a real Postgres. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration::class)
class PieceApiTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `creates a piece and reads it back`() {
        val location = mockMvc.post("/api/v1/pieces") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "  Ballade No. 1  ", "composer": "Frédéric Chopin"}"""
        }.andExpect {
            status { isCreated() }
            header { string("Location", org.hamcrest.Matchers.containsString("/api/v1/pieces/")) }
            jsonPath("$.title") { value("Ballade No. 1") }
            jsonPath("$.composer") { value("Frédéric Chopin") }
        }.andReturn().response.getHeader("Location")!!

        mockMvc.get(location).andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Ballade No. 1") }
            jsonPath("$.createdAt") { value(endsWith("Z")) }
        }

        mockMvc.get("/api/v1/pieces").andExpect {
            status { isOk() }
            jsonPath("$.items[0].title") { value("Ballade No. 1") }
        }
    }

    @Test
    fun `composer is explicitly null when not given`() {
        mockMvc.post("/api/v1/pieces") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Prelude in C"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.composer") { value(null as Any?) }
        }
    }

    @Test
    fun `rejects an invalid piece with problem details`() {
        mockMvc.post("/api/v1/pieces") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": ""}"""
        }.andExpect {
            status { isBadRequest() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
        }
    }

    @Test
    fun `unknown piece is 404 with problem details`() {
        mockMvc.get("/api/v1/pieces/00000000-0000-7000-8000-000000000000").andExpect {
            status { isNotFound() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(404) }
        }
    }
}

package io.github.balaelangovan.spring.core.dto

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.PageRequest
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

class PageableDtoTest :
    DescribeSpec({

        val objectMapper = JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .build()

        describe("PageableDto") {
            describe("constructor with content only") {
                it("should create a single page with all elements") {
                    val content = listOf("a", "b", "c")
                    val page = PageableDto(content)

                    page.content shouldBe content
                    page.totalElements shouldBe 3
                    page.totalPages shouldBe 1
                    page.number shouldBe 0
                    page.size shouldBe 3
                    page.numberOfElements shouldBe 3
                }

                it("should handle empty content") {
                    val page = PageableDto(emptyList<String>())

                    page.content shouldBe emptyList()
                    page.totalElements shouldBe 0
                    page.totalPages shouldBe 1
                    page.numberOfElements shouldBe 0
                }
            }

            describe("constructor with pageable") {
                it("should create a paginated response") {
                    val content = listOf("a", "b")
                    val pageable = PageRequest.of(0, 2)
                    val page = PageableDto(content, pageable, 10)

                    page.content shouldBe content
                    page.totalElements shouldBe 10
                    page.totalPages shouldBe 5
                    page.number shouldBe 0
                    page.size shouldBe 2
                    page.numberOfElements shouldBe 2
                }

                it("should handle middle page correctly") {
                    val content = listOf("c", "d")
                    val pageable = PageRequest.of(1, 2)
                    val page = PageableDto(content, pageable, 10)

                    page.number shouldBe 1
                    page.totalPages shouldBe 5
                }

                it("should handle last page with fewer elements") {
                    val content = listOf("e")
                    val pageable = PageRequest.of(4, 2)
                    val page = PageableDto(content, pageable, 9)

                    page.number shouldBe 4
                    page.totalPages shouldBe 5
                    page.numberOfElements shouldBe 1
                    page.totalElements shouldBe 9
                }
            }

            describe("JSON serialization") {
                it("should use snake_case property names") {
                    val content = listOf("a", "b")
                    val pageable = PageRequest.of(0, 10)
                    val page = PageableDto(content, pageable, 100)

                    val json = objectMapper.writeValueAsString(page)

                    json.contains("total_pages") shouldBe true
                    json.contains("total_elements") shouldBe true
                    json.contains("per_page") shouldBe true
                    json.contains("number_of_elements") shouldBe true
                    json.contains("page") shouldBe true
                }

                it("should not include ignored properties") {
                    val content = listOf("a")
                    val page = PageableDto(content)

                    val json = objectMapper.writeValueAsString(page)

                    json.contains("\"sort\"") shouldBe false
                    json.contains("\"last\"") shouldBe false
                    json.contains("\"first\"") shouldBe false
                    json.contains("\"pageable\"") shouldBe false
                    json.contains("\"empty\"") shouldBe false
                }
            }

            describe("inherited methods") {
                it("should correctly report first/last page status") {
                    val firstPage = PageableDto(listOf("a"), PageRequest.of(0, 1), 3)
                    val lastPage = PageableDto(listOf("c"), PageRequest.of(2, 1), 3)

                    firstPage.isFirst shouldBe true
                    firstPage.isLast shouldBe false

                    lastPage.isFirst shouldBe false
                    lastPage.isLast shouldBe true
                }

                it("should correctly report empty status") {
                    val emptyPage = PageableDto(emptyList<String>(), PageRequest.of(0, 10), 0)
                    val nonEmptyPage = PageableDto(listOf("a"), PageRequest.of(0, 10), 1)

                    emptyPage.isEmpty shouldBe true
                    nonEmptyPage.isEmpty shouldBe false
                }
            }
        }
    })

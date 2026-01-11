package io.github.balaelangovan.spring.webmvc.client

import io.github.balaelangovan.spring.core.constants.HeaderConstants
import io.github.balaelangovan.spring.core.constants.MdcKeys
import io.github.balaelangovan.spring.core.exception.ResourceNotFoundException
import io.github.balaelangovan.spring.core.exception.ValidationException
import io.github.balaelangovan.spring.webmvc.client.oauth.OAuthTokenManager
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.slf4j.MDC
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

class AbstractRestClientTest :
    DescribeSpec({

        lateinit var restTemplate: RestTemplate
        lateinit var oAuthTokenManager: OAuthTokenManager

        // Concrete implementation for testing
        class TestRestClient(
            restTemplate: RestTemplate,
            oAuthTokenManager: OAuthTokenManager? = null,
            private val baseUrl: String = "https://api.example.com",
        ) : AbstractRestClient(restTemplate, oAuthTokenManager) {
            override fun getBaseUrl(): String = baseUrl

            // Expose protected methods for testing
            fun testGet(path: String, responseType: Class<String>): ResponseEntity<String> = get(path, responseType)

            fun testPost(path: String, body: Any?, responseType: Class<String>): ResponseEntity<String> = post(path, body, responseType)

            fun testPut(path: String, body: Any?, responseType: Class<String>): ResponseEntity<String> = put(path, body, responseType)

            fun testDelete(path: String, responseType: Class<String>): ResponseEntity<String> = delete(path, responseType)
        }

        beforeEach {
            restTemplate = mockk()
            oAuthTokenManager = mockk()
            MDC.clear()
        }

        afterEach {
            MDC.clear()
        }

        describe("AbstractRestClient") {
            describe("GET requests") {
                it("should make GET request and return response") {
                    val requestSlot = slot<HttpEntity<*>>()

                    every {
                        restTemplate.exchange(
                            eq("https://api.example.com/users"),
                            eq(HttpMethod.GET),
                            capture(requestSlot),
                            eq(String::class.java),
                        )
                    } returns ResponseEntity("user data", HttpStatus.OK)

                    val client = TestRestClient(restTemplate)
                    val response = client.testGet("/users", String::class.java)

                    response.statusCode shouldBe HttpStatus.OK
                    response.body shouldBe "user data"
                    requestSlot.captured.headers.contentType shouldBe MediaType.APPLICATION_JSON
                }

                it("should include OAuth token in header when token manager is provided") {
                    val requestSlot = slot<HttpEntity<*>>()

                    every { oAuthTokenManager.getToken() } returns "oauth-token-123"
                    every {
                        restTemplate.exchange(
                            any<String>(),
                            eq(HttpMethod.GET),
                            capture(requestSlot),
                            eq(String::class.java),
                        )
                    } returns ResponseEntity("data", HttpStatus.OK)

                    val client = TestRestClient(restTemplate, oAuthTokenManager)
                    client.testGet("/users", String::class.java)

                    requestSlot.captured.headers.getFirst(HeaderConstants.AUTHORIZATION) shouldBe "Bearer oauth-token-123"
                }

                it("should propagate transaction ID from MDC") {
                    val requestSlot = slot<HttpEntity<*>>()

                    MDC.put(MdcKeys.TRANSACTION_ID, "tx-abc-123")
                    every {
                        restTemplate.exchange(
                            any<String>(),
                            eq(HttpMethod.GET),
                            capture(requestSlot),
                            eq(String::class.java),
                        )
                    } returns ResponseEntity("data", HttpStatus.OK)

                    val client = TestRestClient(restTemplate)
                    client.testGet("/users", String::class.java)

                    requestSlot.captured.headers.getFirst(HeaderConstants.X_TRANSACTION_ID) shouldBe "tx-abc-123"
                }

                it("should throw ResourceNotFoundException for 404 response") {
                    every {
                        restTemplate.exchange(
                            any<String>(),
                            eq(HttpMethod.GET),
                            any<HttpEntity<*>>(),
                            eq(String::class.java),
                        )
                    } throws HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        ByteArray(0),
                        null,
                    )

                    val client = TestRestClient(restTemplate)

                    val exception = shouldThrow<ResourceNotFoundException> {
                        client.testGet("/users/999", String::class.java)
                    }

                    exception.message shouldContain "Resource not found"
                }

                it("should throw ValidationException for 400 response") {
                    every {
                        restTemplate.exchange(
                            any<String>(),
                            eq(HttpMethod.GET),
                            any<HttpEntity<*>>(),
                            eq(String::class.java),
                        )
                    } throws HttpClientErrorException.BadRequest.create(
                        HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        HttpHeaders.EMPTY,
                        ByteArray(0),
                        null,
                    )

                    val client = TestRestClient(restTemplate)

                    val exception = shouldThrow<ValidationException> {
                        client.testGet("/users?invalid=true", String::class.java)
                    }

                    exception.message shouldContain "Invalid request"
                }

                it("should throw RuntimeException for other HTTP client errors") {
                    every {
                        restTemplate.exchange(
                            any<String>(),
                            eq(HttpMethod.GET),
                            any<HttpEntity<*>>(),
                            eq(String::class.java),
                        )
                    } throws HttpClientErrorException.create(
                        HttpStatus.FORBIDDEN,
                        "Forbidden",
                        HttpHeaders.EMPTY,
                        ByteArray(0),
                        null,
                    )

                    val client = TestRestClient(restTemplate)

                    val exception = shouldThrow<RuntimeException> {
                        client.testGet("/admin", String::class.java)
                    }

                    exception.message shouldContain "HTTP request failed"
                }
            }

            describe("POST requests") {
                it("should make POST request with body") {
                    val requestSlot = slot<HttpEntity<*>>()

                    every {
                        restTemplate.exchange(
                            eq("https://api.example.com/users"),
                            eq(HttpMethod.POST),
                            capture(requestSlot),
                            eq(String::class.java),
                        )
                    } returns ResponseEntity("created", HttpStatus.CREATED)

                    val client = TestRestClient(restTemplate)
                    val body = mapOf("name" to "John", "email" to "john@example.com")
                    val response = client.testPost("/users", body, String::class.java)

                    response.statusCode shouldBe HttpStatus.CREATED
                    response.body shouldBe "created"
                    requestSlot.captured.body shouldBe body
                }

                it("should make POST request with null body") {
                    val requestSlot = slot<HttpEntity<*>>()

                    every {
                        restTemplate.exchange(
                            any<String>(),
                            eq(HttpMethod.POST),
                            capture(requestSlot),
                            eq(String::class.java),
                        )
                    } returns ResponseEntity("ok", HttpStatus.OK)

                    val client = TestRestClient(restTemplate)
                    client.testPost("/trigger", null, String::class.java)

                    requestSlot.captured.body shouldBe null
                }
            }

            describe("PUT requests") {
                it("should make PUT request with body") {
                    val requestSlot = slot<HttpEntity<*>>()

                    every {
                        restTemplate.exchange(
                            eq("https://api.example.com/users/123"),
                            eq(HttpMethod.PUT),
                            capture(requestSlot),
                            eq(String::class.java),
                        )
                    } returns ResponseEntity("updated", HttpStatus.OK)

                    val client = TestRestClient(restTemplate)
                    val body = mapOf("name" to "John Updated")
                    val response = client.testPut("/users/123", body, String::class.java)

                    response.statusCode shouldBe HttpStatus.OK
                    response.body shouldBe "updated"
                    requestSlot.captured.body shouldBe body
                }
            }

            describe("DELETE requests") {
                it("should make DELETE request") {
                    every {
                        restTemplate.exchange(
                            eq("https://api.example.com/users/123"),
                            eq(HttpMethod.DELETE),
                            any<HttpEntity<*>>(),
                            eq(String::class.java),
                        )
                    } returns ResponseEntity("deleted", HttpStatus.NO_CONTENT)

                    val client = TestRestClient(restTemplate)
                    val response = client.testDelete("/users/123", String::class.java)

                    response.statusCode shouldBe HttpStatus.NO_CONTENT
                }
            }

            describe("base URL handling") {
                it("should correctly combine base URL and path") {
                    every {
                        restTemplate.exchange(
                            eq("https://custom-api.example.com/api/v1/resources"),
                            eq(HttpMethod.GET),
                            any<HttpEntity<*>>(),
                            eq(String::class.java),
                        )
                    } returns ResponseEntity("data", HttpStatus.OK)

                    val client = TestRestClient(
                        restTemplate,
                        baseUrl = "https://custom-api.example.com/api/v1",
                    )
                    client.testGet("/resources", String::class.java)

                    verify {
                        restTemplate.exchange(
                            eq("https://custom-api.example.com/api/v1/resources"),
                            eq(HttpMethod.GET),
                            any<HttpEntity<*>>(),
                            eq(String::class.java),
                        )
                    }
                }
            }

            describe("headers without OAuth or MDC") {
                it("should only set content type when no OAuth or transaction ID") {
                    val requestSlot = slot<HttpEntity<*>>()

                    every {
                        restTemplate.exchange(
                            any<String>(),
                            eq(HttpMethod.GET),
                            capture(requestSlot),
                            eq(String::class.java),
                        )
                    } returns ResponseEntity("data", HttpStatus.OK)

                    val client = TestRestClient(restTemplate)
                    client.testGet("/data", String::class.java)

                    requestSlot.captured.headers.contentType shouldBe MediaType.APPLICATION_JSON
                    requestSlot.captured.headers.getFirst(HeaderConstants.AUTHORIZATION) shouldBe null
                    requestSlot.captured.headers.getFirst(HeaderConstants.X_TRANSACTION_ID) shouldBe null
                }
            }
        }
    })

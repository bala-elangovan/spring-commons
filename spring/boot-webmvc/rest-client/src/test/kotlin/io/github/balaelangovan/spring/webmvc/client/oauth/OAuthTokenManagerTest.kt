package io.github.balaelangovan.spring.webmvc.client.oauth

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class OAuthTokenManagerTest :
    DescribeSpec({

        lateinit var restTemplate: RestTemplate
        lateinit var oAuthProperties: OAuthProperties

        beforeEach {
            restTemplate = mockk()
            oAuthProperties = OAuthProperties(
                enabled = true,
                tokenUrl = "https://auth.example.com/oauth/token",
                clientId = "test-client",
                clientSecret = "test-secret",
                grantType = "client_credentials",
                scope = "read write",
            )
        }

        afterEach {
            clearAllMocks()
        }

        describe("OAuthTokenManager") {
            describe("getToken") {
                describe("when Authorization header is present in request context") {
                    it("should return token from Authorization header") {
                        mockkStatic(RequestContextHolder::class)
                        try {
                            val request = mockk<HttpServletRequest>()
                            val requestAttributes = mockk<ServletRequestAttributes>()

                            every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                            every { requestAttributes.request } returns request
                            every { request.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer existing-token-123"

                            val tokenManager = OAuthTokenManager(restTemplate, oAuthProperties)
                            val token = tokenManager.getToken()

                            token shouldBe "existing-token-123"
                        } finally {
                            unmockkAll()
                        }
                    }

                    it("should fetch new token when Authorization header is missing") {
                        mockkStatic(RequestContextHolder::class)
                        try {
                            val request = mockk<HttpServletRequest>()
                            val requestAttributes = mockk<ServletRequestAttributes>()

                            every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                            every { requestAttributes.request } returns request
                            every { request.getHeader(HttpHeaders.AUTHORIZATION) } returns null

                            val responseBody = mapOf(
                                "access_token" to "new-token-456",
                                "expires_in" to 3600,
                            )
                            every {
                                restTemplate.postForEntity(
                                    eq("https://auth.example.com/oauth/token"),
                                    any(),
                                    eq(Map::class.java),
                                )
                            } returns ResponseEntity(responseBody, HttpStatus.OK)

                            val tokenManager = OAuthTokenManager(restTemplate, oAuthProperties)
                            val token = tokenManager.getToken()

                            token shouldBe "new-token-456"
                        } finally {
                            unmockkAll()
                        }
                    }

                    it("should fetch new token when Authorization header does not start with Bearer") {
                        mockkStatic(RequestContextHolder::class)
                        try {
                            val request = mockk<HttpServletRequest>()
                            val requestAttributes = mockk<ServletRequestAttributes>()

                            every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                            every { requestAttributes.request } returns request
                            every { request.getHeader(HttpHeaders.AUTHORIZATION) } returns "Basic dXNlcjpwYXNz"

                            val responseBody = mapOf(
                                "access_token" to "new-token-789",
                                "expires_in" to 3600,
                            )
                            every {
                                restTemplate.postForEntity(
                                    eq("https://auth.example.com/oauth/token"),
                                    any(),
                                    eq(Map::class.java),
                                )
                            } returns ResponseEntity(responseBody, HttpStatus.OK)

                            val tokenManager = OAuthTokenManager(restTemplate, oAuthProperties)
                            val token = tokenManager.getToken()

                            token shouldBe "new-token-789"
                        } finally {
                            unmockkAll()
                        }
                    }

                    it("should fetch new token when request context is null") {
                        mockkStatic(RequestContextHolder::class)
                        try {
                            every { RequestContextHolder.getRequestAttributes() } returns null

                            val responseBody = mapOf(
                                "access_token" to "context-null-token",
                                "expires_in" to 3600,
                            )
                            every {
                                restTemplate.postForEntity(
                                    eq("https://auth.example.com/oauth/token"),
                                    any(),
                                    eq(Map::class.java),
                                )
                            } returns ResponseEntity(responseBody, HttpStatus.OK)

                            val tokenManager = OAuthTokenManager(restTemplate, oAuthProperties)
                            val token = tokenManager.getToken()

                            token shouldBe "context-null-token"
                        } finally {
                            unmockkAll()
                        }
                    }
                }

                describe("token caching") {
                    it("should cache token and reuse on subsequent calls") {
                        mockkStatic(RequestContextHolder::class)
                        try {
                            every { RequestContextHolder.getRequestAttributes() } returns null

                            val responseBody = mapOf(
                                "access_token" to "cached-token",
                                "expires_in" to 3600,
                            )
                            every {
                                restTemplate.postForEntity(
                                    eq("https://auth.example.com/oauth/token"),
                                    any(),
                                    eq(Map::class.java),
                                )
                            } returns ResponseEntity(responseBody, HttpStatus.OK)

                            val tokenManager = OAuthTokenManager(restTemplate, oAuthProperties)

                            val token1 = tokenManager.getToken()
                            val token2 = tokenManager.getToken()

                            token1 shouldBe "cached-token"
                            token2 shouldBe "cached-token"
                        } finally {
                            unmockkAll()
                        }
                    }
                }

                describe("error handling") {
                    it("should throw exception when token URL is null") {
                        mockkStatic(RequestContextHolder::class)
                        try {
                            every { RequestContextHolder.getRequestAttributes() } returns null

                            val propertiesWithoutUrl = OAuthProperties(
                                enabled = true,
                                tokenUrl = null,
                                clientId = "client",
                                clientSecret = "secret",
                            )

                            val tokenManager = OAuthTokenManager(restTemplate, propertiesWithoutUrl)

                            val exception = shouldThrow<RuntimeException> {
                                tokenManager.getToken()
                            }

                            exception.message shouldContain "OAuth token refresh failed"
                        } finally {
                            unmockkAll()
                        }
                    }

                    it("should throw exception when token fetch fails") {
                        mockkStatic(RequestContextHolder::class)
                        try {
                            every { RequestContextHolder.getRequestAttributes() } returns null
                            every {
                                restTemplate.postForEntity(
                                    eq("https://auth.example.com/oauth/token"),
                                    any(),
                                    eq(Map::class.java),
                                )
                            } throws RuntimeException("Connection failed")

                            val tokenManager = OAuthTokenManager(restTemplate, oAuthProperties)

                            val exception = shouldThrow<RuntimeException> {
                                tokenManager.getToken()
                            }

                            exception.message shouldBe "OAuth token refresh failed"
                        } finally {
                            unmockkAll()
                        }
                    }
                }

                describe("token without scope") {
                    it("should work without scope") {
                        mockkStatic(RequestContextHolder::class)
                        try {
                            every { RequestContextHolder.getRequestAttributes() } returns null

                            val propertiesWithoutScope = OAuthProperties(
                                enabled = true,
                                tokenUrl = "https://auth.example.com/oauth/token",
                                clientId = "client",
                                clientSecret = "secret",
                                scope = null,
                            )

                            val responseBody = mapOf(
                                "access_token" to "no-scope-token",
                                "expires_in" to 3600,
                            )
                            every {
                                restTemplate.postForEntity(
                                    eq("https://auth.example.com/oauth/token"),
                                    any(),
                                    eq(Map::class.java),
                                )
                            } returns ResponseEntity(responseBody, HttpStatus.OK)

                            val tokenManager = OAuthTokenManager(restTemplate, propertiesWithoutScope)
                            val token = tokenManager.getToken()

                            token shouldBe "no-scope-token"
                        } finally {
                            unmockkAll()
                        }
                    }
                }
            }
        }
    })

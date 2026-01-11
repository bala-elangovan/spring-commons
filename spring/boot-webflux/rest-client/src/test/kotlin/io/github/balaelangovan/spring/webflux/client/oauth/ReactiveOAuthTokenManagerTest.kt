package io.github.balaelangovan.spring.webflux.client.oauth

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ReactiveOAuthTokenManagerTest :
    DescribeSpec({

        lateinit var webClient: WebClient
        lateinit var oAuthProperties: OAuthProperties

        beforeEach {
            webClient = mockk()
            oAuthProperties = OAuthProperties(
                enabled = true,
                tokenUrl = "https://auth.example.com/oauth/token",
                clientId = "test-client",
                clientSecret = "test-secret",
                grantType = "client_credentials",
                scope = "read write",
            )
        }

        describe("ReactiveOAuthTokenManager") {
            describe("getToken") {
                describe("when Authorization header is present in exchange") {
                    it("should return token from Authorization header") {
                        val exchange = mockk<ServerWebExchange>()
                        val request = mockk<ServerHttpRequest>()
                        val headers = HttpHeaders()

                        headers.add(HttpHeaders.AUTHORIZATION, "Bearer existing-token-123")

                        every { exchange.request } returns request
                        every { request.headers } returns headers

                        val tokenManager = ReactiveOAuthTokenManager(webClient, oAuthProperties)
                        val result = tokenManager.getToken(exchange)

                        StepVerifier.create(result)
                            .expectNext("existing-token-123")
                            .verifyComplete()
                    }

                    it("should refresh token when Authorization header does not start with Bearer") {
                        val exchange = mockk<ServerWebExchange>()
                        val request = mockk<ServerHttpRequest>()
                        val headers = HttpHeaders()

                        headers.add(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")

                        every { exchange.request } returns request
                        every { request.headers } returns headers

                        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
                        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
                        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
                        val responseSpec = mockk<WebClient.ResponseSpec>()

                        val responseBody = mapOf(
                            "access_token" to "new-token-789",
                            "expires_in" to 3600,
                        )

                        every { webClient.post() } returns requestBodyUriSpec
                        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
                        every { requestBodySpec.headers(any()) } returns requestBodySpec
                        every { requestBodySpec.body(any()) } returns requestHeadersSpec
                        every { requestHeadersSpec.retrieve() } returns responseSpec
                        every { responseSpec.bodyToMono(Map::class.java) } returns Mono.just(responseBody)

                        val tokenManager = ReactiveOAuthTokenManager(webClient, oAuthProperties)
                        val result = tokenManager.getToken(exchange)

                        StepVerifier.create(result)
                            .expectNext("new-token-789")
                            .verifyComplete()
                    }
                }

                describe("when exchange is null") {
                    it("should refresh token when no exchange provided") {
                        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
                        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
                        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
                        val responseSpec = mockk<WebClient.ResponseSpec>()

                        val responseBody = mapOf(
                            "access_token" to "new-token-456",
                            "expires_in" to 3600,
                        )

                        every { webClient.post() } returns requestBodyUriSpec
                        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
                        every { requestBodySpec.headers(any()) } returns requestBodySpec
                        every { requestBodySpec.body(any()) } returns requestHeadersSpec
                        every { requestHeadersSpec.retrieve() } returns responseSpec
                        every { responseSpec.bodyToMono(Map::class.java) } returns Mono.just(responseBody)

                        val tokenManager = ReactiveOAuthTokenManager(webClient, oAuthProperties)
                        val result = tokenManager.getToken(null)

                        StepVerifier.create(result)
                            .expectNext("new-token-456")
                            .verifyComplete()
                    }
                }

                describe("token caching") {
                    it("should cache token and reuse on subsequent calls") {
                        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
                        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
                        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
                        val responseSpec = mockk<WebClient.ResponseSpec>()

                        val responseBody = mapOf(
                            "access_token" to "cached-token",
                            "expires_in" to 3600,
                        )

                        every { webClient.post() } returns requestBodyUriSpec
                        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
                        every { requestBodySpec.headers(any()) } returns requestBodySpec
                        every { requestBodySpec.body(any()) } returns requestHeadersSpec
                        every { requestHeadersSpec.retrieve() } returns responseSpec
                        every { responseSpec.bodyToMono(Map::class.java) } returns Mono.just(responseBody)

                        val tokenManager = ReactiveOAuthTokenManager(webClient, oAuthProperties)

                        // First call should fetch token
                        StepVerifier.create(tokenManager.getToken(null))
                            .expectNext("cached-token")
                            .verifyComplete()

                        // Second call should use cached token
                        StepVerifier.create(tokenManager.getToken(null))
                            .expectNext("cached-token")
                            .verifyComplete()
                    }
                }

                describe("error handling") {
                    it("should propagate error when token fetch fails") {
                        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
                        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
                        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
                        val responseSpec = mockk<WebClient.ResponseSpec>()

                        every { webClient.post() } returns requestBodyUriSpec
                        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
                        every { requestBodySpec.headers(any()) } returns requestBodySpec
                        every { requestBodySpec.body(any()) } returns requestHeadersSpec
                        every { requestHeadersSpec.retrieve() } returns responseSpec
                        every { responseSpec.bodyToMono(Map::class.java) } returns Mono.error(RuntimeException("Connection failed"))

                        val tokenManager = ReactiveOAuthTokenManager(webClient, oAuthProperties)
                        val result = tokenManager.getToken(null)

                        StepVerifier.create(result)
                            .expectError(RuntimeException::class.java)
                            .verify()
                    }

                    it("should throw when token URL is null") {
                        val propertiesWithoutUrl = OAuthProperties(
                            enabled = true,
                            tokenUrl = null,
                            clientId = "client",
                            clientSecret = "secret",
                        )

                        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()

                        // Mock webClient.post() - the requireNotNull will throw before uri() is called
                        every { webClient.post() } returns requestBodyUriSpec

                        val tokenManager = ReactiveOAuthTokenManager(webClient, propertiesWithoutUrl)
                        val result = tokenManager.getToken(null)

                        StepVerifier.create(result)
                            .expectError(IllegalArgumentException::class.java)
                            .verify()
                    }
                }

                describe("token without scope") {
                    it("should work without scope") {
                        val propertiesWithoutScope = OAuthProperties(
                            enabled = true,
                            tokenUrl = "https://auth.example.com/oauth/token",
                            clientId = "client",
                            clientSecret = "secret",
                            scope = null,
                        )

                        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
                        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
                        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
                        val responseSpec = mockk<WebClient.ResponseSpec>()

                        val responseBody = mapOf(
                            "access_token" to "no-scope-token",
                            "expires_in" to 3600,
                        )

                        every { webClient.post() } returns requestBodyUriSpec
                        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
                        every { requestBodySpec.headers(any()) } returns requestBodySpec
                        every { requestBodySpec.body(any()) } returns requestHeadersSpec
                        every { requestHeadersSpec.retrieve() } returns responseSpec
                        every { responseSpec.bodyToMono(Map::class.java) } returns Mono.just(responseBody)

                        val tokenManager = ReactiveOAuthTokenManager(webClient, propertiesWithoutScope)
                        val result = tokenManager.getToken(null)

                        StepVerifier.create(result)
                            .expectNext("no-scope-token")
                            .verifyComplete()
                    }
                }
            }
        }
    })

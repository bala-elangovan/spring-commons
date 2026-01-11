package io.github.balaelangovan.spring.webflux.starter.filter

import io.github.balaelangovan.spring.core.constants.HeaderConstants
import io.github.balaelangovan.spring.core.constants.MdcKeys
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.InetSocketAddress
import java.net.URI

class LoggingFilterTest :
    DescribeSpec({

        val filter = LoggingFilter()

        describe("LoggingFilter") {
            describe("filter") {
                it("should use transaction ID from header when provided") {
                    val exchange = mockk<ServerWebExchange>()
                    val request = mockk<ServerHttpRequest>()
                    val response = mockk<ServerHttpResponse>()
                    val headers = HttpHeaders()
                    val responseHeaders = HttpHeaders()
                    val chain = mockk<WebFilterChain>()

                    headers.add(HeaderConstants.X_TRANSACTION_ID, "custom-tx-id")

                    every { exchange.request } returns request
                    every { exchange.response } returns response
                    every { request.headers } returns headers
                    every { request.method } returns HttpMethod.GET
                    every { request.uri } returns URI.create("http://localhost/api/test")
                    every { request.remoteAddress } returns InetSocketAddress("192.168.1.1", 8080)
                    every { response.headers } returns responseHeaders
                    every { response.statusCode } returns null
                    every { chain.filter(exchange) } returns Mono.empty()

                    val result = filter.filter(exchange, chain)

                    StepVerifier.create(result)
                        .verifyComplete()

                    responseHeaders.getFirst(HeaderConstants.X_TRANSACTION_ID) shouldBe "custom-tx-id"
                    verify { chain.filter(exchange) }
                }

                it("should generate transaction ID when not provided in header") {
                    val exchange = mockk<ServerWebExchange>()
                    val request = mockk<ServerHttpRequest>()
                    val response = mockk<ServerHttpResponse>()
                    val headers = HttpHeaders()
                    val responseHeaders = HttpHeaders()
                    val chain = mockk<WebFilterChain>()

                    every { exchange.request } returns request
                    every { exchange.response } returns response
                    every { request.headers } returns headers
                    every { request.method } returns HttpMethod.GET
                    every { request.uri } returns URI.create("http://localhost/api/test")
                    every { request.remoteAddress } returns InetSocketAddress("192.168.1.1", 8080)
                    every { response.headers } returns responseHeaders
                    every { response.statusCode } returns null
                    every { chain.filter(exchange) } returns Mono.empty()

                    val result = filter.filter(exchange, chain)

                    StepVerifier.create(result)
                        .verifyComplete()

                    val transactionId = responseHeaders.getFirst(HeaderConstants.X_TRANSACTION_ID)
                    transactionId shouldNotBe null
                    transactionId!! shouldMatch Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
                }

                it("should populate context with client transaction ID when provided") {
                    val exchange = mockk<ServerWebExchange>()
                    val request = mockk<ServerHttpRequest>()
                    val response = mockk<ServerHttpResponse>()
                    val headers = HttpHeaders()
                    val responseHeaders = HttpHeaders()
                    val chain = mockk<WebFilterChain>()

                    headers.add(HeaderConstants.X_TRANSACTION_ID, "tx-123")
                    headers.add(HeaderConstants.X_CLIENT_TRANSACTION_ID, "client-tx-456")

                    every { exchange.request } returns request
                    every { exchange.response } returns response
                    every { request.headers } returns headers
                    every { request.method } returns HttpMethod.GET
                    every { request.uri } returns URI.create("http://localhost/api/test")
                    every { request.remoteAddress } returns InetSocketAddress("192.168.1.1", 8080)
                    every { response.headers } returns responseHeaders
                    every { response.statusCode } returns null

                    var capturedClientTxId: String? = null
                    every { chain.filter(exchange) } returns Mono.deferContextual { ctx ->
                        capturedClientTxId = ctx.getOrDefault(MdcKeys.CLIENT_TRANSACTION_ID, null)
                        Mono.empty()
                    }

                    val result = filter.filter(exchange, chain)

                    StepVerifier.create(result)
                        .verifyComplete()

                    capturedClientTxId shouldBe "client-tx-456"
                }

                it("should handle missing remote address") {
                    val exchange = mockk<ServerWebExchange>()
                    val request = mockk<ServerHttpRequest>()
                    val response = mockk<ServerHttpResponse>()
                    val headers = HttpHeaders()
                    val responseHeaders = HttpHeaders()
                    val chain = mockk<WebFilterChain>()

                    every { exchange.request } returns request
                    every { exchange.response } returns response
                    every { request.headers } returns headers
                    every { request.method } returns HttpMethod.GET
                    every { request.uri } returns URI.create("http://localhost/api/test")
                    every { request.remoteAddress } returns null
                    every { response.headers } returns responseHeaders
                    every { response.statusCode } returns null

                    var capturedClientIp: String? = null
                    every { chain.filter(exchange) } returns Mono.deferContextual { ctx ->
                        capturedClientIp = ctx.getOrDefault(MdcKeys.CLIENT_IP, null)
                        Mono.empty()
                    }

                    val result = filter.filter(exchange, chain)

                    StepVerifier.create(result)
                        .verifyComplete()

                    capturedClientIp shouldBe "unknown"
                }

                it("should propagate request method and URL in context") {
                    val exchange = mockk<ServerWebExchange>()
                    val request = mockk<ServerHttpRequest>()
                    val response = mockk<ServerHttpResponse>()
                    val headers = HttpHeaders()
                    val responseHeaders = HttpHeaders()
                    val chain = mockk<WebFilterChain>()

                    every { exchange.request } returns request
                    every { exchange.response } returns response
                    every { request.headers } returns headers
                    every { request.method } returns HttpMethod.POST
                    every { request.uri } returns URI.create("http://localhost/api/users")
                    every { request.remoteAddress } returns InetSocketAddress("10.0.0.1", 8080)
                    every { response.headers } returns responseHeaders
                    every { response.statusCode } returns null

                    var capturedMethod: String? = null
                    var capturedUrl: String? = null
                    every { chain.filter(exchange) } returns Mono.deferContextual { ctx ->
                        capturedMethod = ctx.getOrDefault(MdcKeys.REQUEST_METHOD, null)
                        capturedUrl = ctx.getOrDefault(MdcKeys.REQUEST_URL, null)
                        Mono.empty()
                    }

                    val result = filter.filter(exchange, chain)

                    StepVerifier.create(result)
                        .verifyComplete()

                    capturedMethod shouldBe "POST"
                    capturedUrl shouldBe "http://localhost/api/users"
                }

                it("should handle errors from filter chain") {
                    val exchange = mockk<ServerWebExchange>()
                    val request = mockk<ServerHttpRequest>()
                    val response = mockk<ServerHttpResponse>()
                    val headers = HttpHeaders()
                    val responseHeaders = HttpHeaders()
                    val chain = mockk<WebFilterChain>()

                    every { exchange.request } returns request
                    every { exchange.response } returns response
                    every { request.headers } returns headers
                    every { request.method } returns HttpMethod.GET
                    every { request.uri } returns URI.create("http://localhost/api/error")
                    every { request.remoteAddress } returns InetSocketAddress("192.168.1.1", 8080)
                    every { response.headers } returns responseHeaders
                    every { response.statusCode } returns null
                    every { chain.filter(exchange) } returns Mono.error(RuntimeException("Test error"))

                    val result = filter.filter(exchange, chain)

                    StepVerifier.create(result)
                        .expectError(RuntimeException::class.java)
                        .verify()
                }

                it("should extract client IP from remote address") {
                    val exchange = mockk<ServerWebExchange>()
                    val request = mockk<ServerHttpRequest>()
                    val response = mockk<ServerHttpResponse>()
                    val headers = HttpHeaders()
                    val responseHeaders = HttpHeaders()
                    val chain = mockk<WebFilterChain>()

                    every { exchange.request } returns request
                    every { exchange.response } returns response
                    every { request.headers } returns headers
                    every { request.method } returns HttpMethod.GET
                    every { request.uri } returns URI.create("http://localhost/api/test")
                    every { request.remoteAddress } returns InetSocketAddress("203.0.113.45", 12345)
                    every { response.headers } returns responseHeaders
                    every { response.statusCode } returns null

                    var capturedClientIp: String? = null
                    every { chain.filter(exchange) } returns Mono.deferContextual { ctx ->
                        capturedClientIp = ctx.getOrDefault(MdcKeys.CLIENT_IP, null)
                        Mono.empty()
                    }

                    val result = filter.filter(exchange, chain)

                    StepVerifier.create(result)
                        .verifyComplete()

                    capturedClientIp shouldBe "203.0.113.45"
                }
            }
        }
    })

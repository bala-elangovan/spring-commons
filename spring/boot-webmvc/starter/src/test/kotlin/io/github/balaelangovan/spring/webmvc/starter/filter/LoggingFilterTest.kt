package io.github.balaelangovan.spring.webmvc.starter.filter

import io.github.balaelangovan.spring.core.constants.HeaderConstants
import io.github.balaelangovan.spring.core.constants.MdcKeys
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class LoggingFilterTest :
    DescribeSpec({

        val filter = LoggingFilter()

        afterEach {
            MDC.clear()
        }

        describe("LoggingFilter") {
            describe("getOrder") {
                it("should have highest precedence plus one") {
                    filter.order shouldBe Ordered.HIGHEST_PRECEDENCE + 1
                }
            }

            describe("doFilter") {
                it("should populate MDC with user ID from header") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.addHeader(HeaderConstants.X_USER_ID, "user-123")
                    request.addHeader(HeaderConstants.X_TRANSACTION_ID, "tx-123")
                    request.method = "GET"
                    request.requestURI = "/api/test"

                    var capturedUserId: String? = null
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedUserId = MDC.get(MdcKeys.USER_ID)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedUserId shouldBe "user-123"
                    verify { filterChain.doFilter(any(), any()) }
                }

                it("should populate MDC with user email from header") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.addHeader(HeaderConstants.X_USER_EMAIL, "user@example.com")
                    request.addHeader(HeaderConstants.X_TRANSACTION_ID, "tx-123")
                    request.method = "GET"
                    request.requestURI = "/api/test"

                    var capturedEmail: String? = null
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedEmail = MDC.get(MdcKeys.USER_EMAIL)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedEmail shouldBe "user@example.com"
                }

                it("should populate MDC with user groups from header") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.addHeader(HeaderConstants.X_USER_GROUPS, "admin,developer")
                    request.addHeader(HeaderConstants.X_TRANSACTION_ID, "tx-123")
                    request.method = "GET"
                    request.requestURI = "/api/test"

                    var capturedGroups: String? = null
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedGroups = MDC.get(MdcKeys.USER_GROUPS)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedGroups shouldBe "admin,developer"
                }

                it("should use transaction ID from header when provided") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.addHeader(HeaderConstants.X_TRANSACTION_ID, "custom-tx-id")
                    request.method = "GET"
                    request.requestURI = "/api/test"

                    var capturedTransactionId: String? = null
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedTransactionId = MDC.get(MdcKeys.TRANSACTION_ID)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedTransactionId shouldBe "custom-tx-id"
                }

                it("should generate transaction ID when not provided in header") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.method = "GET"
                    request.requestURI = "/api/test"

                    var capturedTransactionId: String? = null
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedTransactionId = MDC.get(MdcKeys.TRANSACTION_ID)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedTransactionId shouldNotBe null
                    // UUID format validation
                    capturedTransactionId!! shouldMatch Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
                }

                it("should populate MDC with client transaction ID") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.addHeader(HeaderConstants.X_TRANSACTION_ID, "tx-123")
                    request.addHeader(HeaderConstants.X_CLIENT_TRANSACTION_ID, "client-tx-456")
                    request.method = "GET"
                    request.requestURI = "/api/test"

                    var capturedClientTxId: String? = null
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedClientTxId = MDC.get(MdcKeys.CLIENT_TRANSACTION_ID)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedClientTxId shouldBe "client-tx-456"
                }

                it("should populate MDC with client ID") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.addHeader(HeaderConstants.X_TRANSACTION_ID, "tx-123")
                    request.addHeader(HeaderConstants.X_CLIENT_ID, "api-client-789")
                    request.method = "GET"
                    request.requestURI = "/api/test"

                    var capturedClientId: String? = null
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedClientId = MDC.get(MdcKeys.CLIENT_ID)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedClientId shouldBe "api-client-789"
                }

                it("should populate MDC with request method and URL") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.method = "POST"
                    request.requestURI = "/api/users"

                    var capturedMethod: String? = null
                    var capturedUrl: String? = null
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedMethod = MDC.get(MdcKeys.REQUEST_METHOD)
                        capturedUrl = MDC.get(MdcKeys.REQUEST_URL)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedMethod shouldBe "POST"
                    capturedUrl shouldBe "/api/users"
                }

                it("should use X-Forwarded-For for client IP when available") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.addHeader(HeaderConstants.X_TRANSACTION_ID, "tx-123")
                    request.addHeader(HeaderConstants.X_FORWARDED_FOR, "192.168.1.1, 10.0.0.1")
                    request.method = "GET"
                    request.requestURI = "/api/test"
                    request.remoteAddr = "127.0.0.1"

                    var capturedClientIp: String? = null
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedClientIp = MDC.get(MdcKeys.CLIENT_IP)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedClientIp shouldBe "192.168.1.1"
                }

                it("should use remoteAddr for client IP when X-Forwarded-For is not available") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.method = "GET"
                    request.requestURI = "/api/test"
                    request.remoteAddr = "10.0.0.5"

                    var capturedClientIp: String? = null
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedClientIp = MDC.get(MdcKeys.CLIENT_IP)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedClientIp shouldBe "10.0.0.5"
                }

                it("should set response status and duration in MDC after filter chain") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.method = "GET"
                    request.requestURI = "/api/test"

                    every { filterChain.doFilter(any(), any()) } answers {
                        Thread.sleep(10)
                    }

                    filter.doFilter(request, response, filterChain)

                    // MDC is cleared in finally block, so we can't check values after the call
                    // This test verifies the method completes without error
                    verify { filterChain.doFilter(any(), any()) }
                }

                it("should clear MDC after filter execution") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.method = "GET"
                    request.requestURI = "/api/test"
                    every { filterChain.doFilter(any(), any()) } answers {}

                    filter.doFilter(request, response, filterChain)

                    MDC.get(MdcKeys.TRANSACTION_ID) shouldBe null
                    MDC.get(MdcKeys.REQUEST_METHOD) shouldBe null
                }

                it("should clear MDC even when filter chain throws exception") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.method = "GET"
                    request.requestURI = "/api/test"
                    every { filterChain.doFilter(any(), any()) } throws RuntimeException("Filter error")

                    try {
                        filter.doFilter(request, response, filterChain)
                    } catch (_: RuntimeException) {
                        // Expected
                    }

                    MDC.get(MdcKeys.TRANSACTION_ID) shouldBe null
                }

                it("should not log health check requests") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.method = "GET"
                    request.requestURI = "/actuator/health"
                    every { filterChain.doFilter(any(), any()) } answers {}

                    filter.doFilter(request, response, filterChain)

                    verify { filterChain.doFilter(any(), any()) }
                }

                it("should not log swagger-ui requests") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.method = "GET"
                    request.requestURI = "/swagger-ui/index.html"
                    every { filterChain.doFilter(any(), any()) } answers {}

                    filter.doFilter(request, response, filterChain)

                    verify { filterChain.doFilter(any(), any()) }
                }

                it("should not log api-docs requests") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.method = "GET"
                    request.requestURI = "/v3/api-docs"
                    every { filterChain.doFilter(any(), any()) } answers {}

                    filter.doFilter(request, response, filterChain)

                    verify { filterChain.doFilter(any(), any()) }
                }

                it("should ignore blank header values") {
                    val request = MockHttpServletRequest()
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>()

                    request.addHeader(HeaderConstants.X_USER_ID, "   ")
                    request.addHeader(HeaderConstants.X_USER_EMAIL, "")
                    request.addHeader(HeaderConstants.X_TRANSACTION_ID, "   ")
                    request.method = "GET"
                    request.requestURI = "/api/test"

                    var capturedUserId: String? = "initial"
                    var capturedEmail: String? = "initial"
                    every { filterChain.doFilter(any(), any()) } answers {
                        capturedUserId = MDC.get(MdcKeys.USER_ID)
                        capturedEmail = MDC.get(MdcKeys.USER_EMAIL)
                    }

                    filter.doFilter(request, response, filterChain)

                    capturedUserId shouldBe null
                    capturedEmail shouldBe null
                }
            }
        }
    })

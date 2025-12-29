package io.github.balaelangovan.spring.webmvc.starter.filter

import io.github.balaelangovan.spring.core.constants.HeaderConstants
import io.github.balaelangovan.spring.core.constants.MdcKeys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Servlet filter that populates MDC with request context and logs request/response information.
 * Executes early in the filter chain to ensure MDC is available for all subsequent processing.
 */
@Component
class LoggingFilter :
    OncePerRequestFilter(),
    Ordered {
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val stopWatch = StopWatch()

        try {
            populateMdc(request)

            val requestUri = request.requestURI
            if (shouldLog(requestUri)) {
                log.info(TRANSACTION_START, request.method, requestUri)
            }

            stopWatch.start()
            filterChain.doFilter(request, response)
            stopWatch.stop()

            MDC.put(MdcKeys.RESPONSE_STATUS, response.status.toString())
            MDC.put(MdcKeys.REQUEST_DURATION_MS, stopWatch.totalTimeMillis.toString())

            if (shouldLog(requestUri)) {
                log.info(
                    TRANSACTION_END,
                    request.method,
                    requestUri,
                    response.status,
                    stopWatch.totalTimeMillis,
                )
            }
        } finally {
            MDC.clear()
        }
    }

    /**
     * Populates MDC with request context from headers and request attributes.
     *
     * @param request the HTTP servlet request
     */
    private fun populateMdc(request: HttpServletRequest) {
        request
            .getHeader(HeaderConstants.X_USER_ID)
            ?.takeIf { StringUtils.isNotBlank(it) }
            ?.let { MDC.put(MdcKeys.USER_ID, it) }

        request
            .getHeader(HeaderConstants.X_USER_EMAIL)
            ?.takeIf { StringUtils.isNotBlank(it) }
            ?.let { MDC.put(MdcKeys.USER_EMAIL, it) }

        request
            .getHeader(HeaderConstants.X_USER_GROUPS)
            ?.takeIf { StringUtils.isNotBlank(it) }
            ?.let { MDC.put(MdcKeys.USER_GROUPS, it) }

        val transactionId =
            request
                .getHeader(HeaderConstants.X_TRANSACTION_ID)
                ?.takeIf { StringUtils.isNotBlank(it) }
                ?: UUID.randomUUID().toString()
        MDC.put(MdcKeys.TRANSACTION_ID, transactionId)

        request
            .getHeader(HeaderConstants.X_CLIENT_TRANSACTION_ID)
            ?.takeIf { StringUtils.isNotBlank(it) }
            ?.let { MDC.put(MdcKeys.CLIENT_TRANSACTION_ID, it) }

        request
            .getHeader(HeaderConstants.X_CLIENT_ID)
            ?.takeIf { StringUtils.isNotBlank(it) }
            ?.let { MDC.put(MdcKeys.CLIENT_ID, it) }

        MDC.put(MdcKeys.REQUEST_METHOD, request.method)
        MDC.put(MdcKeys.REQUEST_URL, request.requestURI)
        MDC.put(MdcKeys.CLIENT_IP, getClientIp(request))
    }

    /**
     * Determines if the request should be logged based on URI.
     *
     * @param uri the request URI
     * @return true if the request should be logged
     */
    private fun shouldLog(uri: String): Boolean = !uri.contains(ACTUATOR_HEALTH_PATH) &&
        !uri.contains(HEALTH_PATH) &&
        !uri.contains(SWAGGER_UI_PATH) &&
        !uri.contains(API_DOCS_PATH)

    /**
     * Extracts client IP address, checking X-Forwarded-For header first for proxied requests.
     *
     * @param request the HTTP servlet request
     * @return the client IP address
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader(HeaderConstants.X_FORWARDED_FOR)
        return if (StringUtils.isNotBlank(xForwardedFor)) {
            xForwardedFor.split(",")[0].trim()
        } else {
            request.remoteAddr
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(LoggingFilter::class.java)

        private const val TRANSACTION_START = "Transaction Start: {} {}"
        private const val TRANSACTION_END = "Transaction End: {} {} - Status: {}, Duration: {}ms"

        private const val ACTUATOR_HEALTH_PATH = "/actuator/health"
        private const val HEALTH_PATH = "/health"
        private const val SWAGGER_UI_PATH = "/swagger-ui"
        private const val API_DOCS_PATH = "/api-docs"
    }
}

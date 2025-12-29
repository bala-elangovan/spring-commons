package io.github.balaelangovan.filter

import io.github.balaelangovan.constants.HeaderConstants
import io.github.balaelangovan.constants.MdcKeys
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

/**
 * WebFlux filter that adds request logging with MDC support for reactive applications.
 * Uses Reactor Context to propagate trace information through the reactive chain.
 */
@Component
class LoggingFilter : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val request = exchange.request
        val startTime = Instant.now()

        val transactionId =
            request.headers.getFirst(HeaderConstants.X_TRANSACTION_ID)
                ?: UUID.randomUUID().toString()

        val clientTransactionId = request.headers.getFirst(HeaderConstants.X_CLIENT_TRANSACTION_ID)
        val clientIp = request.remoteAddress?.address?.hostAddress ?: UNKNOWN_IP

        val contextMap =
            mutableMapOf<String, String>().apply {
                put(MdcKeys.TRANSACTION_ID, transactionId)
                clientTransactionId?.let { put(MdcKeys.CLIENT_TRANSACTION_ID, it) }
                put(MdcKeys.REQUEST_METHOD, request.method.name())
                put(MdcKeys.REQUEST_URL, request.uri.toString())
                put(MdcKeys.CLIENT_IP, clientIp)
            }

        withMdc(contextMap) {
            log.info(
                INCOMING_REQUEST,
                request.method,
                request.uri,
                clientIp,
            )
        }

        exchange.response.headers.add(HeaderConstants.X_TRANSACTION_ID, transactionId)

        return chain
            .filter(exchange)
            .doOnSuccess {
                val duration = Instant.now().toEpochMilli() - startTime.toEpochMilli()
                contextMap[MdcKeys.REQUEST_DURATION_MS] = duration.toString()
                contextMap[MdcKeys.RESPONSE_STATUS] = exchange.response.statusCode
                    ?.value()
                    ?.toString() ?: UNKNOWN_STATUS

                withMdc(contextMap) {
                    log.info(
                        COMPLETED_REQUEST,
                        request.method,
                        request.uri,
                        exchange.response.statusCode,
                        duration,
                    )
                }
            }.doOnError { error ->
                val duration = Instant.now().toEpochMilli() - startTime.toEpochMilli()
                contextMap[MdcKeys.REQUEST_DURATION_MS] = duration.toString()
                contextMap[MdcKeys.RESPONSE_STATUS] = exchange.response.statusCode
                    ?.value()
                    ?.toString() ?: ERROR_STATUS

                withMdc(contextMap) {
                    log.error(
                        FAILED_REQUEST,
                        request.method,
                        request.uri,
                        duration,
                        error,
                    )
                }
            }.contextWrite { context ->
                var newContext = context
                contextMap.forEach { (key, value) ->
                    newContext = newContext.put(key, value)
                }
                newContext
            }
    }

    /**
     * Temporarily sets MDC for the current thread during block execution.
     *
     * @param T the return type
     * @param contextMap the context data to set in MDC
     * @param block the code block to execute
     * @return the result of the block
     */
    private fun <T> withMdc(
        contextMap: Map<String, String>,
        block: () -> T,
    ): T {
        try {
            contextMap.forEach { (key, value) -> MDC.put(key, value) }
            return block()
        } finally {
            contextMap.keys.forEach { MDC.remove(it) }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(LoggingFilter::class.java)

        private const val INCOMING_REQUEST = "Incoming request: {} {} from {}"
        private const val COMPLETED_REQUEST = "Completed request: {} {} - Status: {} - Duration: {}ms"
        private const val FAILED_REQUEST = "Failed request: {} {} - Duration: {}ms"
        private const val UNKNOWN_IP = "unknown"
        private const val UNKNOWN_STATUS = "unknown"
        private const val ERROR_STATUS = "500"
    }
}

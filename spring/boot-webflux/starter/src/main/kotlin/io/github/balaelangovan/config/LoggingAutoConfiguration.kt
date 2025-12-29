package io.github.balaelangovan.config

import io.github.balaelangovan.filter.LoggingFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Autoconfiguration for WebFlux logging with MDC-like context support.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(
    prefix = "platform.logging",
    name = ["enabled"],
    matchIfMissing = true,
)
class LoggingAutoConfiguration {
    /**
     * Provides a LoggingFilter bean for logging request/response information.
     *
     * @return LoggingFilter bean.
     */
    @Bean
    fun loggingFilter(): LoggingFilter = LoggingFilter()
}

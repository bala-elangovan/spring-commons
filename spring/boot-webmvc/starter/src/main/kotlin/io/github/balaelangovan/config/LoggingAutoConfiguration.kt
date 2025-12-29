package io.github.balaelangovan.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Autoconfiguration for logging features.
 * Enables LoggingFilter to populate MDC and log request/response information.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "platform.logging",
    name = ["enabled"],
    matchIfMissing = true,
)
@ComponentScan("io.github.balaelangovan.filter")
class LoggingAutoConfiguration

package io.github.balaelangovan.spring.webmvc.starter.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Autoconfiguration for exception handling.
 * Enables GlobalExceptionHandler for standardized error responses.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "platform.exception",
    name = ["enabled"],
    matchIfMissing = true,
)
@ComponentScan("io.github.balaelangovan.spring.webmvc.starter.exception")
class ExceptionAutoConfiguration

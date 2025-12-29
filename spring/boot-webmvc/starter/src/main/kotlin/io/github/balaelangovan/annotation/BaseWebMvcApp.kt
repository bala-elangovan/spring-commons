package io.github.balaelangovan.annotation

import io.github.balaelangovan.config.AsyncAutoConfiguration
import io.github.balaelangovan.config.ExceptionAutoConfiguration
import io.github.balaelangovan.config.LoggingAutoConfiguration
import io.github.balaelangovan.config.SecurityAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * Meta-annotation that enables all platform features for a web application.
 * Importing this annotation on your @SpringBootApplication class will automatically configure:
 * - Logging with MDC
 * - Method-level authorization
 * - Global exception handling
 * - Async support with MDC propagation
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(
    LoggingAutoConfiguration::class,
    SecurityAutoConfiguration::class,
    ExceptionAutoConfiguration::class,
    AsyncAutoConfiguration::class,
)
annotation class BaseWebMvcApp

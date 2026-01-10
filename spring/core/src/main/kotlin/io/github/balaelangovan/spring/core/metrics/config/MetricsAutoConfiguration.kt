package io.github.balaelangovan.spring.core.metrics.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Autoconfiguration for metrics collection.
 * Enables Micrometer-based metrics collector and Actuator endpoints.
 *
 * This configuration is framework-agnostic and works with both:
 * - Spring Boot WebMVC (servlet-based)
 * - Spring Boot WebFlux (reactive)
 */
@Configuration
@ConditionalOnProperty(
    prefix = "modules.metrics",
    name = ["enabled"],
    matchIfMissing = true,
)
@ComponentScan("io.github.balaelangovan.spring.core.metrics")
class MetricsAutoConfiguration

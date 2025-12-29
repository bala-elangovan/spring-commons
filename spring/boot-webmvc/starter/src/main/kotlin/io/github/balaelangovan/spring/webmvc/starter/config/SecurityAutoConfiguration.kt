package io.github.balaelangovan.spring.webmvc.starter.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Autoconfiguration for security features.
 * Enables AuthorizationAspect for method-level authorization checks.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "platform.security",
    name = ["enabled"],
    matchIfMissing = true,
)
@ComponentScan("io.github.balaelangovan.spring.webmvc.starter.security")
class SecurityAutoConfiguration

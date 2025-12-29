package io.github.balaelangovan.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

/**
 * Autoconfiguration for WebFlux security features.
 * Enables AuthorizationAspect for method-level authorization checks in reactive applications.
 *
 * This configuration:
 * - Scans and registers the reactive AuthorizationAspect
 * - Enables AOP proxy support for @Authorization annotation
 * - Only activates for reactive (WebFlux) applications
 * - Can be disabled via platform.security.enabled=false
 */
@Configuration
@EnableAspectJAutoProxy
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(
    prefix = "platform.security",
    name = ["enabled"],
    matchIfMissing = true,
)
@ComponentScan("io.github.balaelangovan.security")
class SecurityAutoConfiguration

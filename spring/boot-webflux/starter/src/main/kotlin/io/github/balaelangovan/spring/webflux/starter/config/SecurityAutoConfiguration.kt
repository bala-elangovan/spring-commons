package io.github.balaelangovan.spring.webflux.starter.config

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
 * - Can be disabled via modules.security.enabled=false
 */
@Configuration
@EnableAspectJAutoProxy
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(
    prefix = "modules.security",
    name = ["enabled"],
    matchIfMissing = true,
)
@ComponentScan("io.github.balaelangovan.spring.webflux.starter.security")
class SecurityAutoConfiguration

package io.github.balaelangovan.test

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * Reactive test security configuration that disables security for integration tests.
 * Import this configuration in your test classes when you need to bypass security.
 *
 * This is the WebFlux (reactive) version using ServerHttpSecurity.
 */
@TestConfiguration
@EnableWebFluxSecurity
class TestSecurityConfiguration {
    /**
     * Configures security to permit all requests for testing in reactive applications.
     *
     * @param http The HTTP security configuration to configure.
     * @return The configured security filter chain.
     */
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http
        .authorizeExchange { exchanges ->
            exchanges.anyExchange().permitAll()
        }.csrf { it.disable() }
        .build()
}

package io.github.balaelangovan.spring.webmvc.test

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Test security configuration that permits all requests.
 * Import this in test classes to bypass authentication.
 */
@TestConfiguration
@EnableWebSecurity
class TestSecurityConfiguration {
    /**
     * Configures permissive security for testing.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .authorizeHttpRequests { auth -> auth.anyRequest().permitAll() }
        .csrf { it.disable() }
        .build()
}

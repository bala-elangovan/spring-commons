package io.github.balaelangovan.client.config

import io.github.balaelangovan.client.oauth.OAuthProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * Autoconfiguration for WebFlux REST client with OAuth support.
 */
@Configuration
@ComponentScan("io.github.balaelangovan.client")
@EnableConfigurationProperties(OAuthProperties::class)
class RestClientAutoConfiguration {
    /**
     * Provides a WebClient bean for HTTP client operations.
     */
    @Bean
    @ConditionalOnMissingBean
    fun webClient(): WebClient = WebClient.builder().build()
}

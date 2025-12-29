package io.github.balaelangovan.client.config

import io.github.balaelangovan.client.oauth.OAuthProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

/**
 * Auto-configuration for REST client features.
 * Provides RestTemplate bean and enables OAuth token management.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "platform.rest-client",
    name = ["enabled"],
    matchIfMissing = true,
)
@EnableConfigurationProperties(OAuthProperties::class)
@ComponentScan("io.github.balaelangovan.client")
class RestClientAutoConfiguration {
    /**
     * Provides a RestTemplate bean for HTTP client operations.
     */
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}

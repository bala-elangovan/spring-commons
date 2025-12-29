package io.github.balaelangovan.spring.webmvc.client.config

import io.github.balaelangovan.spring.webmvc.client.oauth.OAuthProperties
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
@ComponentScan("io.github.balaelangovan.spring.webmvc.client")
class RestClientAutoConfiguration {
    /**
     * Provides a RestTemplate bean for HTTP client operations.
     */
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}

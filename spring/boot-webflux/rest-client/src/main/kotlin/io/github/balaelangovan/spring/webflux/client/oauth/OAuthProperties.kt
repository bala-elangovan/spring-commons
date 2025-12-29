package io.github.balaelangovan.spring.webflux.client.oauth

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for OAuth2 client credentials flow (WebFlux).
 */
@ConfigurationProperties(prefix = "platform.rest-client.oauth")
data class OAuthProperties(
    var enabled: Boolean = false,
    var tokenUrl: String? = null,
    var clientId: String? = null,
    var clientSecret: String? = null,
    var grantType: String = "client_credentials",
    var scope: String? = null,
)
